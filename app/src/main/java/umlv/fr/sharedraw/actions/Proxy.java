package umlv.fr.sharedraw.actions;

import org.json.JSONException;
import org.json.JSONObject;

public class Proxy {
    public static Action createAction(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONObject message = jsonObject.getJSONObject("message");
            String adminMessage = message.optString("admin", null);
            if (adminMessage != null) {
                return Admin.createAdminAction(jsonObject);
            }
            String say = message.optString("say", null);
            if (say != null) {
                return Say.createSayAction(jsonObject);
            }
            return Draw.createDrawAction(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
