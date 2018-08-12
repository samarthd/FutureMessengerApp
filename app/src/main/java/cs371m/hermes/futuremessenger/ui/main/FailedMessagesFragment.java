package cs371m.hermes.futuremessenger.ui.main;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cs371m.hermes.futuremessenger.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class FailedMessagesFragment extends Fragment {

    private MainViewModel mModel;
    private MessageAdapter mMessageAdapter;

    public FailedMessagesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the ViewModel from the activity, which means each fragment will have the same
        mModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);

        mMessageAdapter = new MessageAdapter();

        // Add the current fragment as an observer to any changes in stored messages
        mModel.getFailedMessagesWithRecipients().observe(this,
                failedMessages -> {
                        mMessageAdapter.updateMessageList(failedMessages);
                        if(failedMessages.size() == 0) {
                            Log.d("Failed Message Fragment", "Failed message update - empty list so setting visibility");
                            getActivity().findViewById(R.id.failed_messages_recycler_view).setVisibility(View.GONE);
                            getActivity().findViewById(R.id.failed_messages_empty_layout).setVisibility(View.VISIBLE);
                        }
                        else {
                            Log.d("Failed Message Fragment", "Failed message update - full list so setting visibility");
                            getActivity().findViewById(R.id.failed_messages_recycler_view).setVisibility(View.VISIBLE);
                            getActivity().findViewById(R.id.failed_messages_empty_layout).setVisibility(View.GONE);
                        }
                });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View fragmentView =
                inflater.inflate(R.layout.fragment_failed_messages, container, false);
        // Get a MessageAdapter to populate the RecyclerView
        RecyclerView recyclerView = fragmentView.findViewById(R.id.failed_messages_recycler_view);
        recyclerView.setAdapter(mMessageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return fragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
