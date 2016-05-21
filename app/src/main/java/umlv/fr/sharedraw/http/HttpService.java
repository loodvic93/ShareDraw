package umlv.fr.sharedraw.http;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import umlv.fr.sharedraw.actions.Action;
import umlv.fr.sharedraw.actions.Admin;
import umlv.fr.sharedraw.actions.Draw;
import umlv.fr.sharedraw.actions.Proxy;
import umlv.fr.sharedraw.actions.Say;
import umlv.fr.sharedraw.drawer.tools.Brush;
import umlv.fr.sharedraw.drawer.tools.Clean;
import umlv.fr.sharedraw.notifier.NotifyAdmin;
import umlv.fr.sharedraw.notifier.NotifyDraw;

public class HttpService extends Service {
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final static String CLASS_NAME = HttpService.class.getCanonicalName();
    private final Map<Class<?>, List<Action>> actions = new HashMap<>();
    private ExecutorService executor = Executors.newFixedThreadPool(5);
    private final HttpRequest request = new HttpRequest();
    private NotifyDraw delegateDrawer = null;
    private NotifyAdmin delegateAdmin = null;
    private volatile int nextID = 0;
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
        System.out.println("ON BIND");
        mServer = intent.getStringExtra("server");
        mDashboard = intent.getStringExtra("title");
        nextID = intent.getIntExtra("nextId", 0);
        if (mDashboard != null && mServer != null) {
            try {
                if (nextID == 0) {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String response = request.request("getMessage", mServer, mDashboard, Integer.toString(nextID), Integer.toString(0));
                            while (response != null) {
                                saveResponse(response);
                                nextID++;
                                response = request.request("getMessage", mServer, mDashboard, Integer.toString(nextID), Integer.toString(0));
                            }
                        }
                    });
                    thread.start();
                    thread.join();
                }
                updateActions();
            } catch (InterruptedException e) {
                Log.e(CLASS_NAME, "Cannot get previous actions");
            }

        }
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        scheduler.shutdownNow();
        executor.shutdown();
        return super.onUnbind(intent);
    }


    public void stopListener() {
        scheduler.shutdownNow();
        executor.shutdown();
    }

    public void restartListener() {
        scheduler = Executors.newScheduledThreadPool(1);
        executor = Executors.newFixedThreadPool(5);
        updateActions();
    }

    public int getNextID() {
        return nextID;
    }

    public void updateActions() {
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                String response = request.request("getMessage", mServer, mDashboard, Integer.toString(nextID), Integer.toString(0));
                if (response != null) {
                    nextID++;
                    saveResponse(response);
                }
            }
        }, 0, 2, TimeUnit.SECONDS);
    }

    private void saveResponse(String response) {
        Action action = Proxy.createAction(response);
        if (action instanceof Admin) {
            List<Action> adminActionList = actions.get(Admin.class);
            if (adminActionList == null) {
                adminActionList = new ArrayList<>();
                actions.put(Admin.class, adminActionList);
            }
            adminActionList.add(action);
            if (delegateAdmin != null) {
                delegateAdmin.notifyUsers((Admin) action);
            }
        } else if (action instanceof Draw) {
            List<Action> drawActionList = actions.get(Draw.class);
            if (drawActionList == null) {
                drawActionList = new ArrayList<>();
                actions.put(Draw.class, drawActionList);
            }
            Draw draw = (Draw) action;
            Brush brush = draw.getBrush();
            if (brush instanceof Clean) {
                drawActionList.clear();
            } else {
                drawActionList.add(action);
            }
            if (delegateDrawer != null) {
                delegateDrawer.notifyNewDraw(brush);
            }
        } else if (action instanceof Say) {
            List<Action> sayActionList = actions.get(Say.class);
            if (sayActionList == null) {
                sayActionList = new ArrayList<>();
                actions.put(Say.class, sayActionList);
            }
            sayActionList.add(action);
        }
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
     * @return List of all users connected on current dashboard
     */
    @SuppressWarnings("unchecked")
    public List<String> getListOfUsersConnected() {
        List<String> users = new ArrayList<>();
        if (mDashboard != null && mServer != null) {
            List<Action> actionList = actions.get(Admin.class);
            if (actionList != null) {
                for (Action admin : actionList) {
                    if (((Admin) admin).isJoining()) {
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
     * Used only if service update automatically data
     *
     * @return List of all DRAW ACTIONS
     */
    @SuppressWarnings("unchecked")
    public List<Action> getListOfDrawAction() {
        if (mServer != null && mDashboard != null) {
            return actions.get(Draw.class);
        }
        return null;
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
                nextID++;
                return request.request("postMessage", server, dashboard, message);
            }
        };
        try {
            executor.submit(callable);
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

    public void delegateDrawerActivity(NotifyDraw delegate) {
        this.delegateDrawer = delegate;
    }

    public void delegateAdminActivity(NotifyAdmin delegate) {
        this.delegateAdmin = delegate;
    }
}
