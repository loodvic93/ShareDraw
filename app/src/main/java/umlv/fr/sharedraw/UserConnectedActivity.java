package umlv.fr.sharedraw;

import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import umlv.fr.sharedraw.actions.Admin;
import umlv.fr.sharedraw.notifier.NotifyAdmin;
import umlv.fr.sharedraw.notifier.NotifyService;

public class UserConnectedActivity extends Fragment implements NotifyService, NotifyAdmin {
    private ArrayAdapter<String> arrayAdapter;
    private List<String> connected;

    public UserConnectedActivity() {

    }

    public static UserConnectedActivity newInstance() {
        return new UserConnectedActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_user_connected, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (connected == null && MainFragmentActivity.HTTP_SERVICE != null) {
            notifyServiceConnected();
        }
    }

    @SuppressWarnings("all")
    private void initVariable(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            connected = savedInstanceState.getStringArrayList("connected");
        }
    }

    @SuppressWarnings("all")
    private void updateAndSetAdapter(List<String> items) {
        ListView lv = (ListView) getActivity().findViewById(R.id.listView_users);
        assert lv != null;

        ListAdapter adapter = lv.getAdapter();
        if (adapter == null) {
            adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    if (convertView == null) {

                        convertView = getActivity().getLayoutInflater().inflate(R.layout.activity_user_connected_item_list_view, null);
                    }
                    TextView title = (TextView) convertView.findViewById(R.id.title);
                    title.setText(getItem(position));
                    return convertView;
                }
            };
            lv.setAdapter(adapter);
            arrayAdapter = (ArrayAdapter<String>) adapter;
        }
        arrayAdapter.clear();
        arrayAdapter.addAll(items);
    }

    @Override
    public void notifyServiceConnected() {
        connected = MainFragmentActivity.HTTP_SERVICE.getListOfUsersConnected();
        updateAndSetAdapter(connected);
        MainFragmentActivity.HTTP_SERVICE.delegateAdminActivity(this);
    }

    @Override
    public void notifyUsers(final Admin admin) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (admin.isJoining()) {
                    Toast.makeText(getContext(), getString(R.string.is_connecting, admin.getAuthor()), Toast.LENGTH_SHORT).show();
                    arrayAdapter.add(admin.getAuthor());
                } else {
                    Toast.makeText(getContext(), getString(R.string.is_disconnecting, admin.getAuthor()), Toast.LENGTH_SHORT).show();
                    arrayAdapter.remove(admin.getAuthor());
                }
                arrayAdapter.notifyDataSetChanged();
            }
        });
    }
}
