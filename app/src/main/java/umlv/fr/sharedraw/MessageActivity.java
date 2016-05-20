package umlv.fr.sharedraw;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MessageActivity extends Fragment implements NotifyService {
    public MessageActivity() {

    }

    public static MessageActivity newInstance() {
        return new MessageActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_message, container, false);
    }

    @Override
    public void notifyServiceConnected() {

    }
}
