package cs371m.hermes.futuremessenger.ui.draft.adapters;

import android.support.v7.util.DiffUtil;

import java.util.List;

import cs371m.hermes.futuremessenger.persistence.entities.Recipient;

public class RecipientsDiffCallback extends DiffUtil.Callback {

    private List<Recipient> existingList;
    private List<Recipient> updatedList;

    public RecipientsDiffCallback(List<Recipient> existing, List<Recipient> updated) {
        this.existingList = existing;
        this.updatedList = updated;
    }

    @Override
    public int getOldListSize() {
        return existingList.size();
    }

    @Override
    public int getNewListSize() {
        return updatedList.size();
    }

    /**
     * Normally in this method we'd compare the item IDs themselves, but when recipients have not
     * yet been saved to the database, they don't all have IDs.
     *
     * So if both the old and new item have IDs, we will compare them, but otherwise we have to
     * compare their actual contents.
     */
    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        Recipient oldRecipient = existingList.get(oldItemPosition);
        Recipient newRecipient = updatedList.get(newItemPosition);
        if (oldRecipient.getId() != null && newRecipient.getId() != null) {
            return oldRecipient.getId().equals(newRecipient.getId());
        }
        else {
            return oldRecipient.equals(newRecipient);
        }
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        Recipient oldRecipient = existingList.get(oldItemPosition);
        Recipient newRecipient = updatedList.get(newItemPosition);
        return oldRecipient.equals(newRecipient);
    }
}
