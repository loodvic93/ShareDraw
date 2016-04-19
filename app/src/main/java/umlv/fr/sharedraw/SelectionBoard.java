package umlv.fr.sharedraw;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class SelectionBoard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection_board);

        ListView listViewBoard = (ListView) findViewById(R.id.listView_board);

        String[] items = { "Board1", "Board2", "Board3", "Board4", "Board5","Board6", "Board7", "Board8", "Board9", "Board10","Board11","Board12", "Board13", "Board14", "Board15", "Board16" };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, items);

        listViewBoard.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                Toast.makeText(getApplicationContext(),"Add new board... Please wait.",Toast.LENGTH_SHORT).show();
                // TODO
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
