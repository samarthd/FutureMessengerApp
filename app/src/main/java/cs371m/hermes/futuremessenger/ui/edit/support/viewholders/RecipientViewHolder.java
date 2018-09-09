package cs371m.hermes.futuremessenger.ui.edit.support.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public class RecipientViewHolder extends RecyclerView.ViewHolder {
    public View listedRecipientLayout;

    public RecipientViewHolder(View listedRecipientLayout) {
        super(listedRecipientLayout);
        this.listedRecipientLayout = listedRecipientLayout;
    }
}
