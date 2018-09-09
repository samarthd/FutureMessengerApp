package cs371m.hermes.futuremessenger.tasks;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cs371m.hermes.futuremessenger.persistence.AppDatabase;
import cs371m.hermes.futuremessenger.persistence.entities.Message;
import cs371m.hermes.futuremessenger.persistence.entities.Recipient;
import cs371m.hermes.futuremessenger.persistence.entities.join.MessageRecipientJoin;
import cs371m.hermes.futuremessenger.persistence.pojo.MessageWithRecipients;
import cs371m.hermes.futuremessenger.persistence.repositories.isolated.MessageDao;
import cs371m.hermes.futuremessenger.persistence.repositories.isolated.RecipientDao;
import cs371m.hermes.futuremessenger.persistence.repositories.joined.MessageRecipientJoinDao;

public class SaveAndScheduleMessage extends AsyncTask<Void, Integer, Void> {

    private static final String TAG = SaveAndScheduleMessage.class.getName();

    private AppDatabase mDb;

    private MessageWithRecipients mMessageWithRecipients;

    private MessageDao mMessageDao;
    private RecipientDao mRecipientDao;
    private MessageRecipientJoinDao mMessageRecipientJoinDao;

    /**
     * Set the arguments for this task.
     * @param db an instance of the app's database
     * @param messageToSchedule the message to schedule with all recipients (should be validated prior to calling this)
     */
    public void setArguments(AppDatabase db, MessageWithRecipients messageToSchedule) {
        this.mDb = db;
        this.mMessageWithRecipients = messageToSchedule;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if (mDb == null || mMessageWithRecipients == null) {
            return null;
        }
        this.mMessageDao = mDb.messageDao();
        this.mRecipientDao = mDb.recipientDao();
        this.mMessageRecipientJoinDao = mDb.messageRecipientJoinDao();


        // TODO set alarm
        cs371m.hermes.futuremessenger.persistence.entities.embedded.Status scheduledStatus = new cs371m.hermes.futuremessenger.persistence.entities.embedded.Status();
        scheduledStatus.setCode(cs371m.hermes.futuremessenger.persistence.entities.embedded.Status.SCHEDULED);
        scheduledStatus.setDescription(cs371m.hermes.futuremessenger.persistence.entities.embedded.Status.SCHEDULED);
        mMessageWithRecipients.getMessage().setStatus(scheduledStatus);
        if (mMessageWithRecipients.getMessage().getId() == null) {
            // new message
            Log.d(TAG, "Creating new message as message ID was null.");
            mDb.runInTransaction(this::createNewMessage);
        }
        else {
            Log.d(TAG, "Updating existing message as message ID was not null.");
            // update of a previously scheduled message
            mDb.runInTransaction(this::updateExistingMessage);
        }
        return null;
    }

    private void createNewMessage() {
        Message messageToSchedule = mMessageWithRecipients.getMessage();
        Long messageID = mMessageDao.createOrUpdateMessage(messageToSchedule);
        createRecipientsAndAssociations(messageID, mMessageWithRecipients.getRecipients());
        // todo

    }

    private void updateExistingMessage() {
        Long messageID = mMessageDao.createOrUpdateMessage(mMessageWithRecipients.getMessage());

        List<Recipient> currentlyAssociatedRecipients = mMessageRecipientJoinDao.findRecipientsForMessage(messageID);

        Pair<List<Recipient>, List<Recipient>> organizedRecipients = determineRecipientsToPersistAndDissociate(currentlyAssociatedRecipients);

        List<Recipient> recipientsToPersist = organizedRecipients.first;
        List<Recipient> recipientsToDissociate = organizedRecipients.second;

        deleteRelationshipsWithRecipients(messageID, recipientsToDissociate);
        createRecipientsAndAssociations(messageID, recipientsToPersist);
    }

    private Pair<List<Recipient>, List<Recipient>> determineRecipientsToPersistAndDissociate(List<Recipient> currentlyAssociatedRecipients) {
        // safe to create a set as these should all be unique anyway
        Set<Recipient> recipientsToPersist = new HashSet<>(mMessageWithRecipients.getRecipients());
        List<Recipient> recipientsToDissociate = new ArrayList<>();

        Log.d(TAG, "Initial recipients to persist: " + recipientsToPersist.toString());
        Log.d(TAG, "Currently associated recipients: " + currentlyAssociatedRecipients.toString());

        for (Recipient currentlyAssociatedRecipient : currentlyAssociatedRecipients) {
            if (recipientsToPersist.contains(currentlyAssociatedRecipient)) {
                Log.d(TAG, "Currently associated recipient was found in the new list of recipients to persist");
                // the recipient that needs to be persisted is already persisted, so remove it from the set of new recipients to be added
                recipientsToPersist.remove(currentlyAssociatedRecipient);
            }
            else {
                Log.d(TAG, "Currently associated recipient was not found in the new list of recipients to persist, will be removed.");
                // if the new recipients set does not have this recipient, add it to the list that should be removed
                recipientsToDissociate.add(currentlyAssociatedRecipient);
            }
        }

        Log.d(TAG, "Final recipients to persist: " + recipientsToPersist.toString());
        Log.d(TAG, "Recipients to dissociate: " + recipientsToDissociate.toString());

        return new Pair<>( new ArrayList<>(recipientsToPersist), recipientsToDissociate);
    }

    private void createRecipientsAndAssociations(Long messageID, List<Recipient> recipientsToPersist) {
        for (Recipient recipientToPersist : recipientsToPersist) {
            Recipient foundRecipient =
                    mRecipientDao.findRecipientMatchingValues(recipientToPersist.getName(),
                            recipientToPersist.getPhoneNumber());
            Long recipientIDToAssociate;
            if (foundRecipient == null) {
                // create a recipient in the database, as one with the same name/number doesn't exist
                recipientIDToAssociate = mRecipientDao.createOrUpdateRecipient(recipientToPersist);
            }
            else {
                recipientIDToAssociate = foundRecipient.getId();
            }

            // create association
            MessageRecipientJoin association = new MessageRecipientJoin();
            association.setMessageID(messageID);
            association.setRecipientID(recipientIDToAssociate);

            mMessageRecipientJoinDao.insert(association);
        }
    }

    private void deleteRelationshipsWithRecipients(Long messageID, List<Recipient> recipients) {
        List<Long> recipientIds = extractRecipientIds(recipients);
        Log.d(TAG, "Deleting the following recipients: " + recipientIds.toString());

        int numDeleted = mMessageRecipientJoinDao.deleteRelationshipByJoinedIds(messageID, recipientIds);
        if (numDeleted != recipients.size()) {
            Log.e(TAG, "ERROR WHEN DELETING EXISTING RECIPIENTS");
        }

        deleteRecipientsWithNoOtherMessages(recipients);

    }

    private List<Long> extractRecipientIds(List<Recipient> recipients) {
        List<Long> recipientIds = new ArrayList<>(recipients.size());
        for (Recipient recipient : recipients) {
            recipientIds.add(recipient.getId());
        }
        return recipientIds;
    }

    private void deleteRecipientsWithNoOtherMessages(List<Recipient> recipients) {
        for (Recipient recipient : recipients) {
            if (mMessageRecipientJoinDao.findMessagesForRecipient(recipient.getId()).isEmpty()) {
                Log.d(TAG, "Found no messages for recipient " + recipient.getName() + " so deleting");
                mRecipientDao.deleteRecipient(recipient);
            }
        }
    }
}
