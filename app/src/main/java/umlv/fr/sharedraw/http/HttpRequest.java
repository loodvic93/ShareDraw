package umlv.fr.sharedraw.http;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Ludovic
 * @version 1.0
 * This class permit to do HTTP Request to a web service in a AsyncTask
 * You must redefine onPreCreate and onPostExecute in your code
 */
public class HttpRequest extends AsyncTask<String, Integer, String> {
    private final Map<String, Method> methods;

    private HttpRequest() {
        methods = new HashMap<>();
    }

    public HttpRequest createHttpRequest() {
        HttpRequest httpRequest = new HttpRequest();
        for (Method m : this.getClass().getMethods()) {
            if (m.isAnnotationPresent(HttpRequestProperty.class)) {
                String method = m.getAnnotation(HttpRequestProperty.class).value();
                this.methods.put(method, m);
            }
        }


        return httpRequest;
    }

    @HttpRequestProperty(value = "getListOfDashboard")
    private String getListOfDashboard(String... params) {
        URL url = getURL(params[0]);
        if (url == null) return null;
        try {
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        } catch (IOException e) {
            e.printStackTrace();
        }


        return "";
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
    protected String doInBackground(@NonNull String... params) {
        if (params.length < 1) return null;
        try {
            return (String) methods.get(params[0]).invoke(params);
        } catch (IllegalAccessException e) {
            Log.e("Cannot access to : ", params[0]);
            return null;
        } catch (InvocationTargetException e) {
            Log.e("Invalid method : ", params[0]);
            return null;
        }
    }
}
