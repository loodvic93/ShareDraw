package umlv.fr.sharedraw;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import java.util.ArrayList;

public class DashboardActivity extends AppCompatActivity {
    private String username;
    private String title;
    private ArrayList<String> connectedUser = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initVariable(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("username", username);
        outState.putString("title", title);
        outState.putStringArrayList("connectedUser", connectedUser);
    }

    @Override
    public void onBackPressed() {
        System.out.println("BUTTON");
        Intent intent = new Intent();
        intent.putExtra("username", username);
        intent.putExtra("title", title);
        setResult(Activity.RESULT_OK, intent);
        finish();
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
        setTitle(title);
    }
}