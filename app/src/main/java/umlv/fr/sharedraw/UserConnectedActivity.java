package umlv.fr.sharedraw;

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

import java.util.List;

public class UserConnectedActivity extends Fragment implements NotifyService {
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
            System.out.println("TEST");
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
        }
        ArrayAdapter<String> arrayAdapter = (ArrayAdapter<String>) adapter;
        System.out.println("ADAPTER");
        arrayAdapter.clear();
        arrayAdapter.addAll(items);
        arrayAdapter.setNotifyOnChange(true);
    }

    @Override
    public void notifyServiceConnected() {
        connected = MainFragmentActivity.HTTP_SERVICE.getListOfUsersConnected();
        System.out.println("CONNECTED = " + connected);
        updateAndSetAdapter(connected);
    }
}
