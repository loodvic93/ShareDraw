package umlv.fr.sharedraw;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
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
        SimpleAdapter adapter = new SimpleAdapter (this.getBaseContext(), listItem, R.layout.affichage_item_list_view,
                new String[] {"img", "titre", "description"}, new int[] {R.id.img, R.id.titre, R.id.description});

        //On attribue à notre listView l'adapter que l'on vient de créer
        listViewBoard.setAdapter(adapter);

        //Enfin on met un écouteur d'évènement sur notre listView
        listViewBoard.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                //TODO methode pour lancer un board
            }
        });
    }

    private void addNewBoard(ArrayList<HashMap<String, String>> listItem,String titre,String description){
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
        HttpRequest httpRequest = HttpRequest.createHttpRequest();
        httpRequest.setDelegate(this);
        httpRequest.execute("getListOfDashboard", "127.0.0.1:12345");


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
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);

                //On instancie notre layout en tant que View
                LayoutInflater factory = LayoutInflater.from(this);
                final View dialogView = factory.inflate(R.layout.dialog_layout, null);
                dialog.setView(dialogView);
                dialog.setTitle("Please write informations");

                // Set up the buttons
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText title = (EditText) dialogView.findViewById(R.id.title);
                        EditText description = (EditText) dialogView.findViewById(R.id.description);

                        Toast.makeText(getApplicationContext(),title.getText().toString(),Toast.LENGTH_SHORT).show();
                        Toast.makeText(getApplicationContext(),description.getText().toString(),Toast.LENGTH_SHORT).show();

                        //TODO methode pour lancer un new board
                    }
                });

                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                dialog.show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
