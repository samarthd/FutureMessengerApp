package cs371m.hermes.futuremessenger.tasks;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import cs371m.hermes.futuremessenger.persistence.AppDatabase;
import cs371m.hermes.futuremessenger.persistence.entities.Message;
import cs371m.hermes.futuremessenger.persistence.entities.Recipient;
import cs371m.hermes.futuremessenger.persistence.pojo.StatusDetails;

import static cs371m.hermes.futuremessenger.persistence.entities.embedded.Status.FAILED;
import static cs371m.hermes.futuremessenger.persistence.entities.embedded.Status.SENT;
import static cs371m.hermes.futuremessenger.support.SchedulingSupport.getContentTextForMessageFromSentResults;
import static cs371m.hermes.futuremessenger.support.SchedulingSupport.showOrUpdateSentNotificationForMessage;

/**
 * When a message part's sent result comes in, this task is called to update the status of the
 * message.
 */
public class UpdateMessageStatusWithResultCodeOfMessagePart extends AsyncTask<Void, Integer, Void> {

    private BroadcastReceiver.PendingResult mPendingResult;
    private AppDatabase mDb;
    private Recipient mRecipient;
    private Long mMessageID;
    private Context mContext;
    private int mMessagePartIndex;
    private int mSentResultCode;

    public void setArguments(Context context,
                             BroadcastReceiver.PendingResult pendingResult,
                             AppDatabase db, Long messageID, Recipient recipient, int messagePartIndex,
                             int sentResultCode) {
        mPendingResult = pendingResult;
        mContext = context.getApplicationContext();
        mDb = db;
        mRecipient = recipient;
        mMessagePartIndex = messagePartIndex;
        mSentResultCode = sentResultCode;
        mMessageID = messageID;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if (mDb == null) {
            return null;
        }

        boolean wasSendingSuccessful = mSentResultCode == Activity.RESULT_OK;
        updateMessageStatusAndNotify(wasSendingSuccessful);
        if (mPendingResult != null) {
            mPendingResult.finish();
        }
        return null;
    }

    private void updateMessageStatusAndNotify(boolean wasSendingSuccessful) {
        mDb.runInTransaction(() -> {
            Message updatedMessage = updateMessage(wasSendingSuccessful);
            CharSequence notificationContentText = getContentTextForMessageFromSentResults(mContext, updatedMessage);
            showOrUpdateSentNotificationForMessage(mContext, updatedMessage, notificationContentText);
        });
    }

    private Message updateMessage(boolean wasSendingSuccessful) {
        Message foundMessage = mDb.messageDao().findMessage(mMessageID);
        if (foundMessage == null) {
            return null;
        }

        StatusDetails statusDetails = Objects.requireNonNull(foundMessage.getStatus().getDetails());
        SortedMap<Recipient, SortedMap<Integer, Integer>> statusDetailsMap =
                statusDetails.getDetailsMap();

        statusDetailsMap = ObjectUtils.defaultIfNull(statusDetailsMap, new TreeMap<>());


        SortedMap<Integer, Integer> resultMapForRecipient =
                ObjectUtils.defaultIfNull(statusDetailsMap.get(mRecipient), new TreeMap<>());

        resultMapForRecipient.put(mMessagePartIndex, mSentResultCode);
        statusDetailsMap.put(mRecipient, resultMapForRecipient);
        statusDetails.setDetailsMap(statusDetailsMap);
        foundMessage.getStatus().setDetails(statusDetails);

        if (wasSendingSuccessful) {
            // only set it as successful if the message wasn't already marked failed
            // by some other part
            if (ObjectUtils.notEqual(foundMessage.getStatus().getCode(), FAILED)) {
                foundMessage.getStatus().setCode(SENT);
            }
        } else {
            foundMessage.getStatus().setCode(FAILED);
        }
        mDb.messageDao().updateMessage(foundMessage);
        return foundMessage;
    }

}
