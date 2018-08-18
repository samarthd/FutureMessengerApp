package cs371m.hermes.futuremessenger.ui.main.adapters.message.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * ViewHolder for the full message details + options layout.
 */
public class MessageViewHolder extends RecyclerView.ViewHolder {

    public View fullMessageLayout;

    public MessageViewHolder(View fullMessageLayout) {
        super(fullMessageLayout);
        this.fullMessageLayout = fullMessageLayout;
    }
}
