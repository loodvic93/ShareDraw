package umlv.fr.sharedraw;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import umlv.fr.sharedraw.drawer.CircularTextView;
import umlv.fr.sharedraw.http.ServiceHttp;

public class SelectionBoard extends ServiceManager {
    private static final String[] COLOR = {"#F49AC2", "#CB99C9", "#C23B22", "#FFD1DC", "#DEA5A4", "#AEC6CF", "#77DD77", "#CFCFC4", "#B39EB5", "#FFB347", "#B19CD9", "#FF6961", "#03C03C", "#FDFD96", "#836953", "#779ECB", "#966FD6"};
    private final ArrayList<String> listItem = new ArrayList<>();
    private ArrayAdapter<String> arrayAdapter;
    private static final int RESULT = 1;
    private boolean mIsBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.select_dashboard));
        setContentView(R.layout.activity_selection_board);
        doBindService();
        final ListView listViewBoard = (ListView) findViewById(R.id.listView_board);
        assert listViewBoard != null;
        listViewBoard.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                String title = listItem.get(position);
                createAndLaunchDialogBox(title);
            }
        });
    }

    @Override
    protected void onDestroy() {
        doUnbindService();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        listItem.clear();
        doBindService();
        super.onResume();
    }

    @Override
    protected void onPause() {
        doUnbindService();
        super.onPause();
    }

    @Override
    protected void onServiceStarted() {
        doAfterBinding();
    }

    @Override
    protected void onMsgGetListDashboard(Message msg) {
        try {
            Bundle data = msg.getData();
            String result = data.getString("response");
            if (result == null) return;
            resultToBoard(new JSONArray(result));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doBindService() {
        bindService(new Intent(SelectionBoard.this, ServiceHttp.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    @Override
    protected void doUnbindService() {
        if (mIsBound) {
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, ServiceHttp.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException ignored) {

                }
            }
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    private void doAfterBinding() {
        try {
            Message msg = Message.obtain(null, ServiceHttp.MSG_GET_LIST_DASHBOARD, this.hashCode(), 0);
            Bundle bundle = new Bundle();
            bundle.putStringArray("params", new String[]{"getListOfDashboard", getString(R.string.server)});
            msg.setData(bundle);
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void updateAndSetAdapter() {
        ListView lv = (ListView) findViewById(R.id.listView_board);
        assert lv != null;

        ListAdapter adapter = lv.getAdapter();
        if (adapter == null) {
            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    if (convertView == null) {
                        convertView = getLayoutInflater().inflate(R.layout.activity_selection_board_item_list_view, null);
                    }
                    TextView title = (TextView) convertView.findViewById(R.id.title);
                    title.setText(getItem(position));
                    CircularTextView firstLetter = (CircularTextView)convertView.findViewById(R.id.firstLetter);
                    firstLetter.setBackgroundColor(Color.parseColor(COLOR[new Random().nextInt(COLOR.length)]));
                    firstLetter.setText(getItem(position).substring(0,1).toUpperCase());
                    return convertView;
                }
            };
            lv.setAdapter(adapter);
        }
        arrayAdapter = (ArrayAdapter<String>) adapter;
        arrayAdapter.clear();
        arrayAdapter.addAll(listItem);
    }

    private void resultToBoard(JSONArray json) {
        try {
            for (int i = 0; i < json.length(); i++) {
                String title = json.getString(i);
                title = title.replaceAll("_", " ");
                listItem.add(title);
            }
            Collections.sort(listItem, new Comparator<String>() {
                @Override
                public int compare(String lhs, String rhs) {
                    return lhs.compareToIgnoreCase(rhs);
                }
            });
            updateAndSetAdapter();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                createAndLaunchDialogBox(null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void createAndLaunchDialogBox(String currantTitle) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //On instancie notre layout en tant que View
        LayoutInflater factory = LayoutInflater.from(this);
        final View dialogView = factory.inflate(R.layout.activity_selection_board_dialog_layout, null);
        builder.setView(dialogView);
        builder.setTitle(getString(R.string.selection_board_new_title));
        // Set up the buttons
        builder.setPositiveButton(getString(R.string.selection_board_new_dashboard_validate), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText title = (EditText) dialogView.findViewById(R.id.title);
                EditText userName = (EditText) dialogView.findViewById(R.id.userName);
                launchNextActivity(userName.getText().toString().replaceAll(" ", "_"), title.getText().toString().replaceAll(" ", "_"));
            }
        });

        builder.setNegativeButton(getString(R.string.selection_board_new_dashboard_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });


        final AlertDialog dialog = builder.create();

        final EditText title = (EditText) dialogView.findViewById(R.id.title);
        final EditText userName = (EditText) dialogView.findViewById(R.id.userName);

        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        if (currantTitle != null) {
            title.setText(currantTitle);
            title.setEnabled(false);
        } else {
            title.setEnabled(true);
            title.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // DO NOTHING
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // DO NOTHING
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (allFieldsAreComplete(s, userName)) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    } else {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    }
                }
            });
        }

        userName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // DO NOTHING
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // DO NOTHING
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (allFieldsAreComplete(s, title)) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                } else {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }
        });
    }

    private boolean allFieldsAreComplete(Editable v1, EditText v2) {
        return !TextUtils.isEmpty(v1) && !TextUtils.isEmpty(v2.getText());
    }

    private void launchNextActivity(String username, String title) {
        Intent intent = new Intent(SelectionBoard.this, DashboardActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("title", title);
        startActivityForResult(intent, RESULT);
    }
}