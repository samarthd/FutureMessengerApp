package cs371m.hermes.futuremessenger.ui.main.screens.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import cs371m.hermes.futuremessenger.R;
import cs371m.hermes.futuremessenger.ui.main.adapters.message.viewholders.MessageViewHolder;

public class ScheduledMessageOptionsDialog extends Dialog implements View.OnClickListener {

    private MessageViewHolder mMessageViewHolder;

    public ScheduledMessageOptionsDialog(@NonNull MessageViewHolder holder) {
        super(holder.fullMessageLayout.getContext());
        this.mMessageViewHolder = holder;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        View dialogLayout = inflateAndReturnDialogLayout();
        setContentView(dialogLayout);
        setUpOptionsDialog();

        findViewById(R.id.edit_option).setOnClickListener(this);
        findViewById(R.id.delete_option).setOnClickListener(this);
    }

    private View inflateAndReturnDialogLayout() {
        View dialogLayout =
                getLayoutInflater().inflate(R.layout.options_dialog_scheduled_message, null);
        // set the dialog's width to be 90% of the width of the screen
        Double dialogWidth = getContext().getResources().getDisplayMetrics().widthPixels * 0.90;
        dialogLayout.setMinimumWidth(dialogWidth.intValue());
        return  dialogLayout;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.edit_option:
                dismiss();
                break;
            case R.id.delete_option:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
        // start target activity here
    }

    private void setUpOptionsDialog() {
        ViewGroup dialogMessageDetailsLayout = findViewById(R.id.message_details_layout);
        ViewGroup listMessageDetailsLayout = (ViewGroup) mMessageViewHolder.fullMessageLayout;
        /*
         * The message details layout itself does not have a background (for reusability),
         * and we want to separate it from our dialog's background.
         *
         * To give the layout a background, we could either create a wrapper layout that holds the
         * `<include layout="@layout/listed_message_details" />` and does nothing except specify
         * the background, but this is ugly and creates unnecessary nesting.
         *
         * To avoid this nesting, we just set the background programmatically here.
         */
        dialogMessageDetailsLayout.setBackground(
                dialogMessageDetailsLayout.getContext().getDrawable(
                        R.drawable.message_background_shape));
        populateAllTextViewsFromHolder(dialogMessageDetailsLayout, listMessageDetailsLayout);

    }

    private void populateAllTextViewsFromHolder(ViewGroup dialogMessageDetailsLayout,
                                                ViewGroup listMessageDetailsLayout) {
        populateTextViewById(dialogMessageDetailsLayout, listMessageDetailsLayout, R.id.message_content_tv);
        populateTextViewById(dialogMessageDetailsLayout, listMessageDetailsLayout, R.id.recipients_label_tv);
        populateTextViewById(dialogMessageDetailsLayout, listMessageDetailsLayout, R.id.recipients_tv);
        populateTextViewById(dialogMessageDetailsLayout, listMessageDetailsLayout, R.id.scheduled_day_tv);
        populateTextViewById(dialogMessageDetailsLayout, listMessageDetailsLayout, R.id.scheduled_date_tv);
        populateTextViewById(dialogMessageDetailsLayout, listMessageDetailsLayout, R.id.scheduled_time_tv);
    }

    private void populateTextViewById(ViewGroup dialogMessageDetailsLayout,
                                      ViewGroup listMessageDetailsLayout,
                                      int viewId) {
        TextView targetTv = dialogMessageDetailsLayout.findViewById(viewId);
        TextView sourceTv = listMessageDetailsLayout.findViewById(viewId);
        targetTv.setText(sourceTv.getText());
    }
}
