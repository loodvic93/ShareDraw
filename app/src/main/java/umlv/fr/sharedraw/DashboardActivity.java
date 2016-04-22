package umlv.fr.sharedraw;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;

import umlv.fr.sharedraw.drawer.FloatingWindow;
import umlv.fr.sharedraw.http.ServiceHttp;


public class DashboardActivity extends ServiceManager {
    private static final String CLASS_NAME = DashboardActivity.class.getCanonicalName();
    private String username;
    private String title;
    private ArrayList<String> connectedUser = new ArrayList<>();
    private boolean mIsBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doBindService();
        initVariable(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        startService(new Intent(DashboardActivity.this,FloatingWindow.class));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("username", username);
        outState.putString("title", title);
        outState.putStringArrayList("connectedUser", connectedUser);
    }

    @Override
    protected void onPause() {
        signalToQuitDashboard();
        doUnbindService();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        signalToQuitDashboard();
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
        signalToJoinDashboard();
    }

    @Override
    protected void onMsgPostMsg(Message msg) {
        // TODO When a response from service
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_OK);
        finish();
    }

    private void signalToJoinDashboard() {
        try {
            Message msg = Message.obtain(null, ServiceHttp.MSG_POST_MESSAGE, this.hashCode(), 0);
            Bundle bundle = new Bundle();
            bundle.putStringArray("params", new String[]{"postMessage", getString(R.string.server), title, "&author=" + username + "&message={\"" + username + "\": \"join\"}"});
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
            bundle.putStringArray("params", new String[]{"postMessage", getString(R.string.server), title, "&author=" + username + "&message={\"" + username + "\": \"leave\"}"});
            msg.setData(bundle);
            mService.send(msg);
        } catch (RemoteException e) {
            Log.e(CLASS_NAME, "There is a remote exception");
        }
    }

    private void initVariable(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            username = savedInstanceState.getString("username");
            title = savedInstanceState.getString("title");
            connectedUser = savedInstanceState.getStringArrayList("connectedUser");
        } else {
            Intent intent = getIntent();
            username = intent.getStringExtra("username");
            title = intent.getStringExtra("title");

        }
        setTitle(title.replaceAll("_", " "));
    }
}