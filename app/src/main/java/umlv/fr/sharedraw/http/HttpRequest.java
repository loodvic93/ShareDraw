package umlv.fr.sharedraw.http;

import android.os.AsyncTask;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ludovic
 * @version 1.0
 * This class permit to do HTTP Request to a web service in a AsyncTask
 * You must redefine onPreCreate and onPostExecute in your code
 */
public class HttpRequest extends AsyncTask<String, Integer, String> {
    private final Map<String, Method> methods;
    private AsyncTaskResponse delegate;

    private HttpRequest() {
        methods = new HashMap<>();
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

    @HttpRequestProperty(value = "getListOfDashboard")
    private String getListOfDashboard(String... params) {
        URL url = getURL(params[1]);
        /*URL url = getURL(params[0]);
        if (url == null) return null;
        try {
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        } catch (IOException e) {
            e.printStackTrace();
        }*/


        return "Hello";
    }

    private URL getURL(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            Log.d("URL is malformed : ", url);
            return null;
        }
    }

    /**
     * Execute the HTTP Request
     * @param params, must be not null and must have this format: Method to call, parameters to give to the method
     * List of methods:
     * <ul>
     *  <li>getListOfDashboard</li>
     *  <li>...</li>
     * </ul>
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

    public void setDelegate(AsyncTaskResponse delegate) {
        this.delegate = delegate;
    }
}
