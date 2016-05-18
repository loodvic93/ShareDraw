package umlv.fr.sharedraw.http;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import umlv.fr.sharedraw.actions.Action;
import umlv.fr.sharedraw.actions.Admin;
import umlv.fr.sharedraw.actions.Proxy;

@SuppressWarnings("ALL")
public class HttpService extends Service {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ExecutorService executor = Executors.newFixedThreadPool(5);
    private final List<Action> actions = new ArrayList<>();
    private final HttpRequest request = new HttpRequest();
    private String mDashboard;
    private String mServer;


    public class HttpBinder extends Binder {
        public HttpService getService() {
            return HttpService.this;
        }
    }

    private final HttpBinder binder = new HttpBinder();

    @Override
    public IBinder onBind(Intent intent) {
        mServer = intent.getStringExtra("server");
        mDashboard = intent.getStringExtra("title");
        if (mDashboard != null && mServer != null) {
            updateActions();
        }
        return binder;
    }

    @Override
    public void onRebind(Intent intent) {
        mServer = intent.getStringExtra("server");
        mDashboard = intent.getStringExtra("title");
        if (mDashboard != null && mServer != null) {
            updateActions();
        }
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        scheduler.shutdownNow();
        executor.shutdown();
        return super.onUnbind(intent);
    }

    private void updateActions() {
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                int nextID = actions.size();
                String response = request.request("getMessage", mServer, mDashboard, Integer.toString(nextID), Integer.toString(1));
                if (response != null) {
                    actions.add(Proxy.createAction(response));
                }
            }
        }, 0, 1, TimeUnit.MILLISECONDS);
    }

    // API

    /**
     * Get a list of dashboard's name
     *
     * @param server IP address of server
     * @return List of string name of dashboard
     */
    public List<String> getListOfDashboard(final String server) {
        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                return request.request("getListOfDashboard", server);
            }
        };
        try {
            Future<String> future = executor.submit(callable);
            JSONArray json = new JSONArray(future.get());
            List<String> dashboards = new ArrayList<>();
            for (int i = 0; i < json.length(); i++) {
                dashboards.add(json.getString(i));
            }
            return dashboards;
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * Used if service update automatically data
     *
     * @param id Id of message
     * @return Content of message
     */
    public String getMessage(final int id) {
        if (mServer != null && mDashboard != null) {
            return actions.get(id).getMessage();
        }
        return null;
    }

    /**
     * Get a specific message from dashboard
     *
     * @param server    IP address of server
     * @param dashboard Name of dashboard
     * @param id        Id of message
     * @param timeout   Timeout before cancel
     * @return Result in String as JSON
     */

    public String getMessage(final String server, final String dashboard, final int id, final int timeout) {
        // If we forgot, we launch service on specific dashboard
        if (mServer != null && mDashboard != null) {
            return actions.get(id).getMessage();
        } else {
            Callable<String> callable = new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return request.request("getMessage", server, dashboard, Integer.toString(id), Integer.toString(timeout));
                }
            };
            try {
                Future<String> future = executor.submit(callable);
                return future.get();
            } catch (Exception e) {
                return null;
            }
        }
    }

    /**
     * Used if service update automatically data
     *
     * @return List of all users connected on current dashboard
     */
    public List<String> getListOfUsersConnected() {
        List<String> users = new ArrayList<>();
        if (mDashboard != null && mServer != null) {
            for (Action action : actions) {
                if (action instanceof Admin) {
                    Admin admin = (Admin) action;
                    if (admin.isJoining()) {
                        users.add(admin.getAuthor());
                    } else {
                        users.remove(admin.getAuthor());
                    }
                }
            }
        }
        return users;
    }

    /**
     * Get list of all users connected
     *
     * @param server    IP address of server
     * @param dashboard Name of dashboard
     * @param timeout   Timeout before cancel
     * @return List of String as JSON
     */
    public List<String> getListOfUsersConnected(String server, String dashboard, int timeout) {
        List<String> users = new ArrayList<>();
        // If we forgot, we launch service on specific dashboard
        if (mDashboard != null && mServer != null) {
            for (Action action : actions) {
                if (action instanceof Admin) {
                    Admin admin = (Admin) action;
                    if (admin.isJoining()) {
                        users.add(admin.getAuthor());
                    } else {
                        users.remove(admin.getAuthor());
                    }
                }
            }
        } else {
            int id = 0;
            String result = getMessage(server, dashboard, id, timeout);
            while (result != null) {
                Action action = Proxy.createAction(result);
                if (action instanceof Admin) {
                    Admin admin = (Admin) action;
                    if (admin.isJoining()) {
                        users.add(admin.getAuthor());
                    } else {
                        users.remove(admin.getAuthor());
                    }
                }
                result = getMessage(server, dashboard, id++, timeout);
            }
        }
        return users;
    }

    /**
     * Used if service update automatically data
     *
     * @return List of all ACTIONS
     */
    public List<Action> getListOfAction() {
        if (mServer != null && mDashboard != null) {
            return actions;
        }
        return null;
    }

    /**
     * Get list of all previous ACTIONS
     *
     * @param server    IP Address of server
     * @param dashboard Name of dashboard
     * @param timeout   Timeout before cancel
     * @return List of all ACTIONS
     */
    public List<Action> getListOfAction(String server, String dashboard, int timeout) {
        // If we forgot, we launch service on specific dashboard
        if (mServer != null && mDashboard != null) {
            return actions;
        } else {
            List<Action> actions = new ArrayList<>();
            int id = 0;
            String result = getMessage(server, dashboard, id, timeout);
            while (result != null) {
                actions.add(Proxy.createAction(result));
                result = getMessage(server, dashboard, id++, timeout);
            }
            return actions;
        }
    }

    /**
     * Post a message to server and return its id
     *
     * @param server    IP Address of server
     * @param dashboard Name of dashboard
     * @param message   Message in JSON format
     */
    public void postMessage(final String server, final String dashboard, final String message) {
        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                return request.request("postMessage", server, dashboard, message);
            }
        };
        try {
            Future<String> future = executor.submit(callable);
            System.out.println("RESULT = " + future.get());
        } catch (Exception e) {
            // Do Nothing
        }
    }

    /**
     * Signal a dashboard to join it
     *
     * @param server    IP Address of server
     * @param dashboard Name of dashboard
     * @param username  Username connected
     */
    public void signalToJoinDashboard(String server, String dashboard, String username) {
        postMessage(server, dashboard, "&author=" + username + "&message={\"admin\": \"join\"}");
    }

    /**
     * Signal a dashboard to leave it
     *
     * @param server    IP Address of server
     * @param dashboard Name of dashboard
     * @param username  Username disconnected
     */
    public void signalToLeaveDashboard(String server, String dashboard, String username) {
        postMessage(server, dashboard, "&author=" + username + "&message={\"admin\": \"leave\"}");
    }
}
