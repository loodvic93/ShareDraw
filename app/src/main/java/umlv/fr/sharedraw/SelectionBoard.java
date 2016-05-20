package umlv.fr.sharedraw;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import umlv.fr.sharedraw.drawer.CircularTextView;
import umlv.fr.sharedraw.http.HttpService;
import umlv.fr.sharedraw.http.HttpService.HttpBinder;

public class SelectionBoard extends AppCompatActivity {
    private static final String[] COLOR = {"#F49AC2", "#CB99C9", "#C23B22", "#FFD1DC", "#DEA5A4", "#AEC6CF", "#77DD77", "#CFCFC4", "#B39EB5", "#FFB347", "#B19CD9", "#FF6961", "#03C03C", "#FDFD96", "#836953", "#779ECB", "#966FD6"};
    private final ArrayList<Dashboard> dashboards = new ArrayList<>();
    private HttpService httpService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.select_dashboard));

        setContentView(R.layout.activity_selection_board);
        final ListView listViewBoard = (ListView) findViewById(R.id.listView_board);
        assert listViewBoard != null;
        listViewBoard.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                String title = dashboards.get(position).name;
                createAndLaunchDialogBox(title);
            }
        });

        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.SwipeRefreshLayoutSelectionBoard);
        assert swipeRefreshLayout != null;
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getListOfDashboard();
            }
        });
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            HttpBinder binder = (HttpBinder) service;
            httpService = binder.getService();
            getListOfDashboard();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            httpService = null;
        }
    };

    @Override
    protected void onResume() {
        Intent intent = new Intent(this, HttpService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        super.onResume();
    }

    @Override
    protected void onPause() {
        unbindService(connection);
        super.onPause();
    }

    private void getListOfDashboard() {
        List<String> names = httpService.getListOfDashboard(getString(R.string.server));
        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.SwipeRefreshLayoutSelectionBoard);
        assert swipeRefreshLayout != null;
        swipeRefreshLayout.setRefreshing(false);
        resultToBoard(names);
    }

    private void resultToBoard(List<String> names) {
        if (names == null) return;
        for (String dashboard : names) {
            Dashboard d = new Dashboard(dashboard, COLOR[new Random().nextInt(COLOR.length)]);
            if (!dashboards.contains(d)) {
                dashboards.add(d);
            }
        }
        Collections.sort(dashboards, new Comparator<Dashboard>() {
            @Override
            public int compare(Dashboard lhs, Dashboard rhs) {
                return lhs.name.compareToIgnoreCase(rhs.name);
            }
        });
        updateAndSetAdapter(dashboards);
    }


    @SuppressWarnings("all")
    private void updateAndSetAdapter(List<Dashboard> items) {
        ListView lv = (ListView) findViewById(R.id.listView_board);
        assert lv != null;

        ListAdapter adapter = lv.getAdapter();
        if (adapter == null) {
            adapter = new ArrayAdapter<Dashboard>(this, android.R.layout.simple_list_item_1) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    if (convertView == null) {
                        convertView = getLayoutInflater().inflate(R.layout.activity_selection_board_item_list_view, null);
                    }
                    TextView title = (TextView) convertView.findViewById(R.id.title);
                    title.setText(getItem(position).name.replaceAll("_", " "));
                    CircularTextView firstLetter = (CircularTextView) convertView.findViewById(R.id.firstLetter);
                    firstLetter.setBackgroundColor(Color.parseColor(getItem(position).color));
                    firstLetter.setText(getItem(position).name.substring(0, 1).toUpperCase());
                    return convertView;
                }
            };
            lv.setAdapter(adapter);
        }
        ArrayAdapter<Dashboard> arrayAdapter = (ArrayAdapter<Dashboard>) adapter;
        arrayAdapter.clear();
        arrayAdapter.addAll(items);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                List<Dashboard> filterDashboard = new ArrayList<>();
                if (query.isEmpty()) {
                    updateAndSetAdapter(dashboards);
                    return false;
                }
                for (Dashboard dashboard : dashboards) {
                    if (dashboard.name.toLowerCase().contains(query.toLowerCase())) {
                        filterDashboard.add(dashboard);
                    }
                }
                updateAndSetAdapter(filterDashboard);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<Dashboard> filterDashboard = new ArrayList<>();
                if (newText.isEmpty()) {
                    updateAndSetAdapter(dashboards);
                    return false;
                }
                for (Dashboard dashboard : dashboards) {
                    if (dashboard.name.toLowerCase().contains(newText.toLowerCase())) {
                        filterDashboard.add(dashboard);
                    }
                }
                updateAndSetAdapter(filterDashboard);
                return false;
            }
        });


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

    @SuppressWarnings("all")
    private void createAndLaunchDialogBox(String currantTitle) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //On instancie notre layout en tant que View
        LayoutInflater factory = LayoutInflater.from(this);
        final View dialogView = factory.inflate(R.layout.activity_selection_board_dialog_layout, null);
        builder.setView(dialogView);
        builder.setTitle(getString(R.string.selection_board_new_title));
        // Set up the buttons
        builder.setPositiveButton(getString(R.string.selection_board_new_dashboard_validate), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText title = (EditText) dialogView.findViewById(R.id.title);
                EditText userName = (EditText) dialogView.findViewById(R.id.userName);
                launchNextActivity(userName.getText().toString().replaceAll(" ", "_"), title.getText().toString().replaceAll(" ", "_"));
            }
        });

        builder.setNegativeButton(getString(R.string.selection_board_new_dashboard_cancel), new DialogInterface.OnClickListener() {
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

        if (currantTitle != null) {
            title.setText(currantTitle);
            title.setEnabled(false);
        } else {
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

    private void launchNextActivity(String username, String title) {
        Intent intent = new Intent(SelectionBoard.this, MainFragmentActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("title", title);
        startActivity(intent);
        //startActivityForResult(intent, RESULT);
    }

    private class Dashboard {
        private final String name;
        private final String color;

        Dashboard(String name, String color) {
            this.name = name;
            this.color = color;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Dashboard dashboard = (Dashboard) o;

            return name != null ? name.equals(dashboard.name) : dashboard.name == null;

        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }
    }
}