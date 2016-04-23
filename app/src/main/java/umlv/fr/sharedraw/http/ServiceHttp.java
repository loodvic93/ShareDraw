package umlv.fr.sharedraw.http;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author Ludovic
 * @version 1.0
 *          This class permit to do HTTP Request to a web service
 */
public class ServiceHttp extends Service {
    private final static String CLASS_NAME = ServiceHttp.class.getCanonicalName();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Map<String, Method> methods = new HashMap<>();
    private final ArrayList<Messenger> mClients = new ArrayList<>();
    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_GET_LIST_DASHBOARD = 3;
    public static final int MSG_GET_MESSAGE = 4;
    public static final int MSG_POST_MESSAGE = 5;

    public ServiceHttp() {
        for (Method m : ServiceHttp.class.getDeclaredMethods()) {
            if (m.isAnnotationPresent(HttpRequestProperty.class)) {
                String method = m.getAnnotation(HttpRequestProperty.class).value();
                methods.put(method, m);
            }
        }
    }

    final Messenger myMessenger = new Messenger(new HttpHandler());

    @Override
    public IBinder onBind(Intent intent) {
        return myMessenger.getBinder();
    }

    class HttpHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                case MSG_GET_LIST_DASHBOARD:
                case MSG_GET_MESSAGE:
                case MSG_POST_MESSAGE:
                    doRequest(msg, msg.what);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Execute the HTTP Request
     *
     * @param msg must be not null and must have this format: Method to call, parameters to give to the method
     *            <br /><br />
     *            Methods list to give in first argument to executor:<br />
     *            <table>
     *            <tr>
     *            <th>Method</th>
     *            <th>Arguments</th>
     *            </tr>
     *            <tr>
     *            <td>getListOfDashboard</td>
     *            <td>server : String</td>
     *            </tr>
     *            <tr>
     *            <td>postMessage</td>
     *            <td>server    : String</td>
     *            <td>queueName : String</td>
     *            <td>message   : JSON as String</td>
     *            </tr>
     *            <tr>
     *            <td>getMessage</td>
     *            <td>server         : String</td>
     *            <td>queueName      : String</td>
     *            <td>idMessage      : String</td>
     *            <td>Timeout in sec : String</td>
     *            </tr>
     *            </table>
     *            <br />
     * @param delegate Method to call after response
     */
    private void doRequest(final Message msg, int delegate) {
        Bundle data = msg.getData();
        final String[] params = data.getStringArray("params");
        if (params == null) return;
        final Method m = methods.get(params[0]);
        if (m == null) return;
        sendResponseToClients(execute(m, params), msg.arg1, delegate);
    }

    private String execute(final Method m, final String[] params) {
        try {
            Future<String> stringFuture = executor.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return (String) m.invoke(null, new Object[]{params});
                }
            });
            return stringFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            Log.e(CLASS_NAME, "Request interrupted");
            return null;
        }
    }

    private void sendResponseToClients(String resp, int mValue, int delegate) {
        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {
                Message response = Message.obtain(null, delegate, mValue, 0);
                Bundle bundle = new Bundle();
                bundle.putString("response", resp);
                response.setData(bundle);
                mClients.get(i).send(response);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @HttpRequestProperty(value = "getListOfDashboard")
    protected static String getListOfDashboard(String... params) {
        if (params.length < 2) {
            Log.e(CLASS_NAME, "This method (getListOfDashboard) must have few arguments: server(String)");
            return null;
        }
        URL url = getURL("http://" + params[1]);
        if (url == null) return null;
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int code = connection.getResponseCode();
            if (code == 200) {
                return getStringFromInputStream(connection.getInputStream());
            } else {
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(CLASS_NAME, "Cannot connect to " + params[1]);
            return null;
        }
    }

    @HttpRequestProperty(value = "postMessage")
    protected static String postNewMessage(String... params) {
        if (params.length < 4) {
            Log.e(CLASS_NAME, "This method (postMessage) must have few arguments: server(String), queue(String), message(JSON as String)");
            return null;
        }
        URL url = getURL("http://" + params[1] + "/" + params[2]);
        if (url == null) return null;
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            connection.setDoInput(true);
            OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
            connection.getOutputStream().write(params[3].getBytes("UTF-8"));
            wr.flush();
            int code = connection.getResponseCode();
            if (code == 200) {
                return getStringFromInputStream(connection.getInputStream());
            }
            return null;
        } catch (IOException e) {
            Log.e(CLASS_NAME, "Cannot connect to " + params[1]);
            return null;
        }
    }

    @HttpRequestProperty(value = "getMessage")
    protected static String getMessage(String... params) {
        if (params.length < 5) {
            Log.e(CLASS_NAME, "This method (getMessage) must have few arguments: server(String), queue(String), idMessage(String), timeout(String in sec)");
            return null;
        }
        URL url = getURL("http://" + params[1] + "/" + params[2] + "/" + params[3] + "?timeout=" + params[4]);
        if (url == null) return null;
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int code = connection.getResponseCode();
            if (code == 200) {
                StringBuilder stringBuilder = new StringBuilder(getStringFromInputStream(connection.getInputStream()));
                stringBuilder.insert(1, "\"id\": " + Integer.valueOf(params[3]) + ", ");
                return stringBuilder.toString();
            }
            return null;
        } catch (IOException e) {
            Log.e(CLASS_NAME, "Cannot connect to " + params[1]);
            return null;
        }
    }

    protected static URL getURL(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            Log.e(CLASS_NAME, "URL is malformed" + url);
            return null;
        }
    }

    protected static String getStringFromInputStream(InputStream is) {
        String inputLine;
        StringBuilder response = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
        } catch (IOException e) {
            Log.e(CLASS_NAME, "Cannot read response from the server");
            return null;
        }
        return response.toString();
    }
}