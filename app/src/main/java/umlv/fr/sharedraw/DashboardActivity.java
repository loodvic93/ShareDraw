package umlv.fr.sharedraw;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import umlv.fr.sharedraw.actions.Action;
import umlv.fr.sharedraw.actions.Admin;
import umlv.fr.sharedraw.actions.Proxy;
import umlv.fr.sharedraw.drawer.FloatingWindow;
import umlv.fr.sharedraw.http.ServiceHttp;


public class DashboardActivity extends ServiceManager {
    private static final String CLASS_NAME = DashboardActivity.class.getCanonicalName();
    private static final String TIMEOUT = "1";
    private boolean mIsBound;
    private ArrayList<String> connectedUser = new ArrayList<>();
    private ArrayList<Action> actions = new ArrayList<>();
    private ArrayList<Integer> actionForCurrentUser = new ArrayList<>();
    private HashMap<String, String> messagesReceived = new HashMap<>();
    private int currentAction = 0;
    private boolean dashboardIsCompleteInit = false;
    private String username;
    private String title;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doBindService();
        initVariable(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        startService(new Intent(DashboardActivity.this, FloatingWindow.class));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("username", username);
        outState.putString("title", title);
        outState.putStringArrayList("connectedUser", connectedUser);
        outState.putParcelableArrayList("actions", actions);
        outState.putIntegerArrayList("actionForCurrentUser", actionForCurrentUser);
        outState.putSerializable("messagesReceived", messagesReceived);
        outState.putInt("currentAction", currentAction);
    }

    @Override
    protected void onPause() {
        signalToQuitDashboard();
        doUnbindService();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        doUnbindService();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        doBindService();
        super.onResume();
    }

    @Override
    protected void doBindService() {

        bindService(new Intent(DashboardActivity.this, ServiceHttp.class), mConnection, Context.BIND_AUTO_CREATE);
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

    @Override
    protected void onServiceStarted() {
        if (actions.isEmpty()) {
            signalToJoinDashboard();
            try {
                getPreviousActionsFromServer(0);
            } catch (RemoteException e) {
                Log.e(CLASS_NAME, "Cannot get previous actions from the server for this dashboard");
            }
        }
    }

    @Override
    protected void onMsgPostMsg(Message msg) {
        Bundle data = msg.getData();
        String result = data.getString("response");
        if (result == null) return;
        try {
            JSONObject jsonObject = new JSONObject(result);
            actionForCurrentUser.add(jsonObject.getInt("id"));
        } catch (JSONException e) {
            Log.e(CLASS_NAME, "An error has happen when " + username + " post a message to a server");
        }
    }

    @Override
    protected void onMsgGetMsg(Message msg) {
        Bundle data = msg.getData();
        String result = data.getString("response");
        if (!dashboardIsCompleteInit) {
            try {
                initDashboard(result);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        // TODO Case if it's not an init
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_OK);
        finish();
    }

    private void initDashboard(String result) throws RemoteException {
        if (result == null) {
            dashboardIsCompleteInit = true;
            return;
        }
        Action action = Proxy.createAction(result);
        if (action != null && !actionForCurrentUser.contains(action.getIdMessage())) {
            actions.add(action);
            if (action instanceof Admin) {
                Admin adminAction = (Admin) action;
                if (adminAction.isJoining()) {
                    connectedUser.add(action.getAuthor());
                } else {
                    connectedUser.remove(action.getAuthor());
                }
            }
        }
        getPreviousActionsFromServer(action.getIdMessage() + 1);
    }

    private void getPreviousActionsFromServer(int id) throws RemoteException {
        Message msg = Message.obtain(null, ServiceHttp.MSG_GET_MESSAGE, this.hashCode(), 0);
        Bundle bundle = new Bundle();
        bundle.putStringArray("params", new String[]{"getMessage", getString(R.string.server), title, Integer.toString(id), TIMEOUT});
        msg.setData(bundle);
        mService.send(msg);
    }

    private void signalToJoinDashboard() {
        try {
            Message msg = Message.obtain(null, ServiceHttp.MSG_POST_MESSAGE, this.hashCode(), 0);
            Bundle bundle = new Bundle();
            bundle.putStringArray("params", new String[]{"postMessage", getString(R.string.server), title, "&author=" + username + "&message={\"admin\": \"join\"}"});
            msg.setData(bundle);
            mService.send(msg);
        } catch (RemoteException e) {
            Log.e(CLASS_NAME, "There is a remote exception");
        }
    }

    private void signalToQuitDashboard() {
        try {
            Message msg = Message.obtain(null, ServiceHttp.MSG_POST_MESSAGE, this.hashCode(), 0);
            Bundle bundle = new Bundle();
            bundle.putStringArray("params", new String[]{"postMessage", getString(R.string.server), title, "&author=" + username + "&message={\"admin\": \"leave\"}"});
            msg.setData(bundle);
            mService.send(msg);
        } catch (RemoteException e) {
            Log.e(CLASS_NAME, "There is a remote exception");
        }
    }

    @SuppressWarnings("all")
    private void initVariable(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            username = savedInstanceState.getString("username");
            title = savedInstanceState.getString("title");
            connectedUser = savedInstanceState.getStringArrayList("connectedUser");
            actions = savedInstanceState.getParcelableArrayList("actions");
            currentAction = savedInstanceState.getInt("currentAction");
            actionForCurrentUser = savedInstanceState.getIntegerArrayList("actionForCurrentUser");
            messagesReceived = (HashMap<String, String>) savedInstanceState.getSerializable("messagesReceived");
        } else {
            Intent intent = getIntent();
            username = intent.getStringExtra("username");
            title = intent.getStringExtra("title");
        }
        setTitle(title.replaceAll("_", " "));
    }
}