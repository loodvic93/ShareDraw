package umlv.fr.sharedraw;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import umlv.fr.sharedraw.http.AsyncTaskResponse;
import umlv.fr.sharedraw.http.HttpRequest;

public class SelectionBoard extends AppCompatActivity implements AsyncTaskResponse{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection_board);

        ListView listViewBoard = (ListView) findViewById(R.id.listView_board);

        //Création de la ArrayList qui nous permettra de remplir la listView
        ArrayList<HashMap<String, String>> listItem = new ArrayList<HashMap<String, String>>();

        //On refait la manip plusieurs fois avec des données différentes pour former les items de notre ListView
        addNewBoard(listItem,"A Titre Board 1","Description");

        addNewBoard(listItem,"C Titre Board 2","Description");

        addNewBoard(listItem,"Titre Board 3","Description");

        //Création d'un SimpleAdapter qui se chargera de mettre les items présents dans notre list (listItem) dans la vue affichageitem
        SimpleAdapter mSchedule = new SimpleAdapter (this.getBaseContext(), listItem, R.layout.affichage_item_list_view,
                new String[] {"img", "titre", "description"}, new int[] {R.id.img, R.id.titre, R.id.description});

        //On attribue à notre listView l'adapter que l'on vient de créer
        listViewBoard.setAdapter(mSchedule);

        //Enfin on met un écouteur d'évènement sur notre listView
        listViewBoard.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {

            }
        });

        /* Test AsyncTask
        HttpRequest httpRequest = HttpRequest.createHttpRequest();
        httpRequest.setDelegate(this);
        httpRequest.execute("getListOfDashboard", "127.0.0.1:12345"); */
    }

    private void addNewBoard(ArrayList listItem,String titre,String description){
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("titre", titre);
        map.put("description", description);
        String nameOfImage = titre.substring(0,1);
        nameOfImage =  nameOfImage.toLowerCase();
        map.put("img", String.valueOf(getResources().getIdentifier(nameOfImage, "drawable", this.getPackageName())));
        listItem.add(map);
    }

    @Override
    public void onAsyncTaskFinished(String result) {
        // TODO: Call a method which draw the list view
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
