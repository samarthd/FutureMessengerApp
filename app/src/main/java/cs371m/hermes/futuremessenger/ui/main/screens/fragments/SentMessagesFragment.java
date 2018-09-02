package cs371m.hermes.futuremessenger.ui.main.screens.fragments;


import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cs371m.hermes.futuremessenger.R;
import cs371m.hermes.futuremessenger.ui.main.MainViewModel;
import cs371m.hermes.futuremessenger.ui.main.adapters.message.MessageAdapter;
import cs371m.hermes.futuremessenger.ui.main.adapters.message.ScheduledMessageAdapter;
import jp.wasabeef.recyclerview.animators.ScaleInAnimator;

/**
 * A simple {@link Fragment} subclass.
 */
public class SentMessagesFragment extends Fragment {

    private MainViewModel mModel;
    private MessageAdapter mMessageAdapter;

    public SentMessagesFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the ViewModel from the activity, which means each fragment will have the same
        mModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);

        // TODO change this to SentMessagesAdapter
        mMessageAdapter = new ScheduledMessageAdapter(getActivity().getSupportFragmentManager());

        // Add the current fragment as an observer to any changes in stored messages
        mModel.getSentMessagesWithRecipients().observe(this,
                sentMessages -> mMessageAdapter.updateMessageList(sentMessages));
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View fragmentView =
                inflater.inflate(R.layout.fragment_sent_messages, container, false);
        // Get a MessageAdapter to populate the RecyclerView
        RecyclerView recyclerView = fragmentView.findViewById(R.id.sent_messages_recycler_view);
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
