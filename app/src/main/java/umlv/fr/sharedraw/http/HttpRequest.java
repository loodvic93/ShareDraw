package umlv.fr.sharedraw.http;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ludovic
 * @version 1.0
 * This class permit to do HTTP Request to a web service in a AsyncTask
 * Don't forget to set the delegate to recover AsyncTask result
 */
public class HttpRequest extends AsyncTask<String, Integer, String> {
    private final Map<String, Method> methods;
    private AsyncTaskResponse delegate;

    private HttpRequest() {
        methods = new HashMap<>();
        delegate = null;
    }

    public static HttpRequest createHttpRequest() {
        HttpRequest httpRequest = new HttpRequest();
        for (Method m : HttpRequest.class.getDeclaredMethods()) {
            if (m.isAnnotationPresent(HttpRequestProperty.class)) {
                String method = m.getAnnotation(HttpRequestProperty.class).value();
                httpRequest.methods.put(method, m);
            }
        }
        return httpRequest;
    }

    /**
     * Execute the HTTP Request
     * @param params must be not null and must have this format: Method to call, parameters to give to the method<br /><br />
     * Methods list to give in first argument to executor:<br />
     * <table>
     *     <tr>
     *         <th>Method</th>
     *         <th>Arguments</th>
     *     </tr>
     *     <tr>
     *         <td>getListOfDashboard</td>
     *         <td>server : String</td>
     *     </tr>
     *     <tr>
     *         <td>postMessage</td>
     *         <td>server    : String</td>
     *         <td>queueName : String</td>
     *         <td>message   : String</td>
     *     </tr>
     *     <tr>
     *         <td>getMessage</td>
     *         <td>server         : String</td>
     *         <td>queueName      : String</td>
     *         <td>idMessage      : String</td>
     *         <td>Timeout in sec : String</td>
     *     </tr>
     * </table>
     * <br />
     * @return String at JSON Format if all is ok, null if not
     */
    @Override
    protected String doInBackground(String... params) {
        if (params.length < 1) return null;
        try {
            Method m = methods.get(params[0]);
            if (m == null) return null;
            return (String)m.invoke(HttpRequest.this, new Object[] { params });
        } catch (IllegalAccessException e) {
            Log.e("Cannot access to : ", params[0]);
            return null;
        } catch (InvocationTargetException e) {
            Log.e("Invalid method : ", params[0]);
            return null;
        }
    }

    /**
     * Delegate the result of AsyncTask to another method
     * @param result Result of HTTP Request in JSON.
     */
    @Override
    protected void onPostExecute(String result) {
        delegate.onAsyncTaskFinished(result);
    }

    @HttpRequestProperty(value = "getListOfDashboard")
    private String getListOfDashboard(String... params) {
        if (params.length < 1) return null;
        URL url = getURL("http://" + params[1]);
        try {
            assert url != null;
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int code = connection.getResponseCode();
            System.out.println("CODE = " + code);
            if (code == 200) {
                return getStringFromInputStream(connection.getInputStream());
            } else {
                return null;
            }

        } catch (IOException e) {
            Log.d("Cannot connect to ", params[1]);
            e.printStackTrace();
            return null;
        }
    }

    @HttpRequestProperty(value = "postMessage")
    private String postNewMessage(String... params) {
        if (params.length < 3) return null;
        URL url = getURL("http://" + params[1] + "/" + params[2]);
        if (url == null) return null;
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
            wr.write(params[3]);
            wr.flush();
            int code = connection.getResponseCode();
            if (code == 200) {
                return getStringFromInputStream(connection.getInputStream());
            }
            return null;
        } catch (IOException e) {
            Log.d("Cannot connect to ", params[1]);
            return null;
        }
    }

    @HttpRequestProperty(value = "getMessage")
    private String getMessage(String... params) {
        if (params.length < 4) return null;
        URL url = getURL("http://" + params[1] + "/" + params[2] + "/" + params[3] + "?timeout=" + params[4]);
        if (url == null) return null;
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
               int code = connection.getResponseCode();
            if (code == 200) {
                return getStringFromInputStream(connection.getInputStream());
            }
            return null;
        } catch (IOException e) {
            Log.d("Cannot connect to ", params[1]);
            return null;
        }
    }

    private URL getURL(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            Log.d("URL is malformed : ", url);
            return null;
        }
    }

    public void setDelegate(AsyncTaskResponse delegate) {
        this.delegate = delegate;
    }

    private String getStringFromInputStream(InputStream is) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }
}
