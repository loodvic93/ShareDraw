package umlv.fr.sharedraw;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;

import umlv.fr.sharedraw.http.ServiceHttp;

public class ServiceManager extends AppCompatActivity {
    protected Messenger mService = null;
    protected final Messenger mMessenger = new Messenger(new HttpHandler());
    protected ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
            try {
                Message msg = Message.obtain(null, ServiceHttp.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
                onServiceStarted();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };



    protected class HttpHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ServiceHttp.MSG_GET_LIST_DASHBOARD:
                    onMsgGetListDashboard(msg);
                    break;
                case ServiceHttp.MSG_GET_MESSAGE:
                    onMsgGetMsg(msg);
                    break;
                case ServiceHttp.MSG_POST_MESSAGE:
                    onMsgPostMsg(msg);
                    break;
                default:
                    super.handleMessage(msg);
            }

        }
    }

    protected void onServiceStarted() {}
    protected void onMsgGetListDashboard(Message msg) {}
    protected void onMsgGetMsg(Message msg) {}
    protected void onMsgPostMsg(Message msg) {}
    protected void doBindService() {}
    protected void doUnbindService() {}
}
