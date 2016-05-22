package umlv.fr.sharedraw;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import umlv.fr.sharedraw.actions.Say;
import umlv.fr.sharedraw.notifier.NotifyMessage;
import umlv.fr.sharedraw.notifier.NotifyService;

public class MessageActivity extends Fragment implements NotifyService, NotifyMessage {
    private ArrayList<Say> messages = new ArrayList<>();
    private ArrayAdapter<Say> arrayAdapter;

    public MessageActivity() {

    }

    public static MessageActivity newInstance() {
        return new MessageActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initVariable(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_message, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (MainFragmentActivity.HTTP_SERVICE != null) {
            notifyServiceConnected();
        }
        Button send = (Button) getActivity().findViewById(R.id.buttonSend);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) getActivity().findViewById(R.id.messageToSend);
                notifyNewMessage(editText.getText().toString());
                editText.setText("");
                final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("messages", messages);
    }

    private void initVariable(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            messages = savedInstanceState.getParcelableArrayList("messages");
        }
    }

    @SuppressWarnings("all")
    private void updateAndSetAdapter(List<Say> items) {
        ListView lv = (ListView) getActivity().findViewById(R.id.listView_Message);
        assert lv != null;

        ListAdapter adapter = lv.getAdapter();
        if (adapter == null) {
            adapter = new ArrayAdapter<Say>(getContext(), android.R.layout.simple_list_item_1) {


                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    Say say = getItem(position);
                    if (say.getAuthor().equals(MainFragmentActivity.mUsername)) {
                        convertView = getActivity().getLayoutInflater().inflate(R.layout.activity_message_send_item_list_view, null);
                    } else {
                        convertView = getActivity().getLayoutInflater().inflate(R.layout.activity_message_receive_item_list_view, null);
                    }

                    TextView author = (TextView) convertView.findViewById(R.id.author);
                    author.setText(say.getAuthor());
                    TextView message = (TextView) convertView.findViewById(R.id.message);
                    message.setText(say.getContent());
                    TextView time = (TextView) convertView.findViewById(R.id.time);
                    time.setText(say.getTime());
                    return convertView;
                }
            };
            lv.setAdapter(adapter);
            arrayAdapter = (ArrayAdapter<Say>) adapter;
        }
        arrayAdapter.clear();
        arrayAdapter.addAll(items);
        lv.setSelection(arrayAdapter.getCount() - 1);
        lv.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        lv.setStackFromBottom(true);
    }


    @Override
    public void notifyServiceConnected() {
        if (messages.isEmpty()) {
            this.messages.addAll(MainFragmentActivity.HTTP_SERVICE.getListOfMessages());
        }
        updateAndSetAdapter(messages);
        MainFragmentActivity.HTTP_SERVICE.delegateSayActivity(this);
    }

    public void notifyNewMessage(String text) {
        String JSON = "{\"say\": {\"content\": \"" + text + "\",\"destination\": \"\"}}";
        MainFragmentActivity.HTTP_SERVICE.postMessage(getString(R.string.server), MainFragmentActivity.mTitle, "&author=" + MainFragmentActivity.mUsername + "&message=" + JSON, false);
    }

    @Override
    public void notifyMessageReceive(final Say say) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messages.add(say);
                arrayAdapter.add(say);
                arrayAdapter.notifyDataSetChanged();
                ListView lv = (ListView) getActivity().findViewById(R.id.listView_Message);
                lv.setSelection(arrayAdapter.getCount() - 1);
            }
        });
    }
}
