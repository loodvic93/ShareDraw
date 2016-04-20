package umlv.fr.sharedraw;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;

import umlv.fr.sharedraw.http.AsyncTaskResponse;
import umlv.fr.sharedraw.http.HttpRequest;

public class SelectionBoard extends AppCompatActivity implements AsyncTaskResponse {
    private final ArrayList<HashMap<String, String>> listItem = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection_board);

        HttpRequest httpRequest = HttpRequest.createHttpRequest();
        httpRequest.setDelegate(this);
        httpRequest.execute("getListOfDashboard", "192.168.1.87:7777");

        ListView listViewBoard = (ListView) findViewById(R.id.listView_board);

        assert listViewBoard != null;
        listViewBoard.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                //TODO methode pour lancer un board
            }
        });
    }

    private void addNewBoard(ArrayList<HashMap<String, String>> listItem, String titre, String description) {
        HashMap<String, String> map = new HashMap<>();
        map.put("titre", titre);
        map.put("description", description);
        String nameOfImage = titre.substring(0, 1);
        nameOfImage = nameOfImage.toLowerCase();
        map.put("img", String.valueOf(getResources().getIdentifier(nameOfImage, "drawable", this.getPackageName())));
        listItem.add(map);
    }

    private void updateAndSetAdapter() {
        ListView listViewBoard = (ListView) findViewById(R.id.listView_board);
        if (listViewBoard == null) return;
        SimpleAdapter adapter = new SimpleAdapter(this.getBaseContext(), listItem, R.layout.affichage_item_list_view,
                new String[]{"img", "titre", "description"}, new int[]{R.id.img, R.id.titre, R.id.author});
        listViewBoard.setAdapter(adapter);
    }

    private void resultToBoard(JSONArray json) {
        try {
            for (int i = 0; i < json.length(); i++) {
                String titre = json.getString(i);
                addNewBoard(listItem, titre, "");
            }
            updateAndSetAdapter();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAsyncTaskFinished(String result) {
        if (result == null) return;
        try {
            resultToBoard(new JSONArray(result));
        } catch (JSONException e) {
            //
        }
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
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                //On instancie notre layout en tant que View
                LayoutInflater factory = LayoutInflater.from(this);
                final View dialogView = factory.inflate(R.layout.dialog_layout, null);
                builder.setView(dialogView);
                builder.setTitle("Please write informations");
                // Set up the buttons
                builder.setPositiveButton("OK", new DialogListenerClickNewDashboard(dialogView));

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });


                AlertDialog dialog = builder.create();
                dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        EditText title = (EditText) dialogView.findViewById(R.id.title);
                        EditText author = (EditText) dialogView.findViewById(R.id.author);
                        System.out.println("TITRE = " + title.getText().toString());
                        if (!title.getText().toString().isEmpty() && !author.getText().toString().isEmpty()) {
                            ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        } else {
                            ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                        }
                        return true;
                    }
                });

                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    }
                });
                dialog.show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class DialogListenerClickNewDashboard implements DialogInterface.OnClickListener, AsyncTaskResponse {
        private View dialogView;
        private String author;
        private String title;

        DialogListenerClickNewDashboard(View dialogView) {
            this.dialogView = dialogView;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            EditText title = (EditText) dialogView.findViewById(R.id.title);
            EditText author = (EditText) dialogView.findViewById(R.id.author);
            this.title = title.getText().toString();
            this.author = author.getText().toString();

            Toast.makeText(getApplicationContext(), title.getText().toString(), Toast.LENGTH_SHORT).show();
            Toast.makeText(getApplicationContext(), author.getText().toString(), Toast.LENGTH_SHORT).show();

            HttpRequest httpRequest = HttpRequest.createHttpRequest();
            httpRequest.setDelegate(this);
            httpRequest.execute("postMessage", "192.168.1.87:7777", this.title, "&author=" + this.author + "&message={\""+ author +"\": \"join\"}");
        }

        @Override
        public void onAsyncTaskFinished(String result) {
            if (result == null) return;
            Intent intent = new Intent(SelectionBoard.this, DashboardActivity.class);
            intent.putExtra("author", author);
            intent.putExtra("title", title);
            finish();
            startActivity(intent);
        }
    }


}
