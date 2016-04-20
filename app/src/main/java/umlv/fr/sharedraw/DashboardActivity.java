package umlv.fr.sharedraw;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class DashboardActivity extends AppCompatActivity {
    private String author;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        author = savedInstanceState.getString("author");
        title = savedInstanceState.getString("title");
        setContentView(R.layout.activity_dashboard);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("author", author);
        outState.putString("title", title);
    }
}
