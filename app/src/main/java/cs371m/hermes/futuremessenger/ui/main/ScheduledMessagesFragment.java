package cs371m.hermes.futuremessenger.ui.main;


import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import cs371m.hermes.futuremessenger.R;
import jp.wasabeef.recyclerview.animators.LandingAnimator;
import jp.wasabeef.recyclerview.animators.ScaleInAnimator;
import jp.wasabeef.recyclerview.animators.ScaleInBottomAnimator;
import jp.wasabeef.recyclerview.animators.ScaleInLeftAnimator;
import jp.wasabeef.recyclerview.animators.ScaleInTopAnimator;
import jp.wasabeef.recyclerview.animators.SlideInDownAnimator;
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

/**
 * A simple {@link Fragment} subclass.
 */
public class ScheduledMessagesFragment extends Fragment {

    private MainViewModel mModel;
    private MessageAdapter mMessageAdapter;

    public ScheduledMessagesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the ViewModel from the activity, which means each fragment will have the same
        mModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);

        mMessageAdapter = new MessageAdapter();

        // Add the current fragment as an observer to any changes in stored messages
        mModel.getScheduledMessagesWithRecipients().observe(this,
                scheduledMessages -> mMessageAdapter.updateMessageList(scheduledMessages));
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View fragmentView =
                inflater.inflate(R.layout.fragment_scheduled_messages, container, false);
        // Get a MessageAdapter to populate the RecyclerView
        RecyclerView recyclerView = fragmentView.findViewById(R.id.scheduled_messages_recycler_view);
        recyclerView.setItemAnimator(new ScaleInAnimator());
        recyclerView.setAdapter(mMessageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

}
