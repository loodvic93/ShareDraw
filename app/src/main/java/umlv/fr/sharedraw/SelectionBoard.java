package umlv.fr.sharedraw;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
        httpRequest.execute("getListOfDashboard", getString(R.string.server));

        final ListView listViewBoard = (ListView) findViewById(R.id.listView_board);


        assert listViewBoard != null;
        listViewBoard.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                HashMap<String,String> map = listItem.get(position);
                createAndLaunchDialogBox(map.get("titre"));
                //TODO methode pour lancer un board
            }
        });
    }

    private void addNewBoard(ArrayList<HashMap<String, String>> listItem, String titre, String userName) {
        HashMap<String, String> map = new HashMap<>();
        map.put("titre", titre);
        map.put("userName", userName);
        String nameOfImage = titre.substring(0, 1);
        nameOfImage = nameOfImage.toLowerCase();
        map.put("img", String.valueOf(getResources().getIdentifier(nameOfImage, "drawable", this.getPackageName())));
        listItem.add(map);
    }

    private void updateAndSetAdapter() {
        ListView listViewBoard = (ListView) findViewById(R.id.listView_board);
        if (listViewBoard == null) return;
        SimpleAdapter adapter = new SimpleAdapter(this.getBaseContext(), listItem, R.layout.affichage_item_list_view,
                new String[]{"img", "titre", "userName"}, new int[]{R.id.img, R.id.titre, R.id.userName});
        listViewBoard.setAdapter(adapter);
    }

    private void resultToBoard(JSONArray json) {
        try {
            for (int i = 0; i < json.length(); i++) {
                String titre = json.getString(i);
                addNewBoard(listItem, titre, "");
            }
            Collections.sort(listItem, new Comparator<HashMap<String, String>>() {
                @Override
                public int compare(HashMap<String, String> lhs, HashMap<String, String> rhs) {
                    return lhs.get("titre").compareTo(rhs.get("titre"));
                }
            });
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
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                createAndLaunchDialogBox(null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void createAndLaunchDialogBox(String currantTitle){
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


        final AlertDialog dialog = builder.create();

        final EditText title = (EditText) dialogView.findViewById(R.id.title);
        final EditText userName = (EditText) dialogView.findViewById(R.id.userName);

        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        if(currantTitle!=null){
            title.setText(currantTitle);
            title.setEnabled(false);
        }else{
            title.setEnabled(true);
            title.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // DO NOTHING
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // DO NOTHING
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (allFieldsAreComplete(s, userName)) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    } else {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    }
                }
            });
        }

        userName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // DO NOTHING
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // DO NOTHING
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (allFieldsAreComplete(s, title)) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                } else {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }
        });
    }

    private boolean allFieldsAreComplete(Editable v1, EditText v2) {
        return !TextUtils.isEmpty(v1) && !TextUtils.isEmpty(v2.getText());
    }

    private class DialogListenerClickNewDashboard implements DialogInterface.OnClickListener, AsyncTaskResponse {
        private View dialogView;
        private String userName;
        private String title;

        DialogListenerClickNewDashboard(View dialogView) {
            this.dialogView = dialogView;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            EditText title = (EditText) dialogView.findViewById(R.id.title);
            EditText userName = (EditText) dialogView.findViewById(R.id.userName);
            this.title = title.getText().toString();
            this.userName = userName.getText().toString();

            String titleName = this.title.replaceAll(" ","_");
            String userNameName = this.userName.replaceAll(" ","_");

            HttpRequest httpRequest = HttpRequest.createHttpRequest();
            httpRequest.setDelegate(this);
            httpRequest.execute("postMessage", getString(R.string.server), titleName, "&author=" + userNameName + "&message={\""+ userNameName +"\": \"join\"}");
        }

        @Override
        public void onAsyncTaskFinished(String result) {
            if (result == null) return;
            System.out.println("RESULT ="+result);
            Intent intent = new Intent(SelectionBoard.this, DashboardActivity.class);
            intent.putExtra("userName", userName);
            intent.putExtra("title", title);
            finish();
            startActivity(intent);
        }
    }


}
