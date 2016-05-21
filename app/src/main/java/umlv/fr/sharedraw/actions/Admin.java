package umlv.fr.sharedraw.actions;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class Admin implements Action {
    private static final String CLASS_NAME = Admin.class.getCanonicalName();
    private String author;
    private String message;
    private boolean joining;
    private int id;

    private Admin() {
    }

    static Admin createAdminAction(JSONObject jsonObject) {
        Admin admin = new Admin();
        try {
            admin.id = jsonObject.getInt("id");
            admin.author = jsonObject.getString("author");
            admin.message = jsonObject.getJSONObject("message").toString();
            admin.joining = jsonObject.getJSONObject("message").getString("admin").equalsIgnoreCase("join");
        } catch (JSONException e) {
            Log.e(CLASS_NAME, "Error while read JSON message");
        }
        return admin;
    }

    @Override
    public String getAuthor() {
        return author;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getId() {
        return id;
    }

    public boolean isJoining() {
        return joining;
    }

    @SuppressWarnings("rawtypes")
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Admin createFromParcel(Parcel in) {
            return new Admin(in);
        }

        @Override
        public Object[] newArray(int size) {
            return null;
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(author);
        dest.writeString(message);
        dest.writeByte((byte) (joining ? 1 : 0));
    }

    private Admin(Parcel in) {
        this.getFromParcel(in);
    }

    private void getFromParcel(Parcel in) {
        author = in.readString();
        message = in.readString();
        joining = in.readByte() != 0;
    }
}
