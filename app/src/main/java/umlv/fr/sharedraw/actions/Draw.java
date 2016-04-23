package umlv.fr.sharedraw.actions;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class Draw implements Action {
    private static final String CLASS_NAME = Say.class.getCanonicalName();
    private String author;
    private String message;
    private long timestamp;
    private int id;

    private Draw() { }

    static Draw createDrawAction(JSONObject jsonObject) {
        Draw draw = new Draw();
        try {
            draw.id = jsonObject.getInt("id");
            draw.author = jsonObject.getString("author");
            draw.timestamp = jsonObject.getLong("timestamp");
            draw.message = jsonObject.getJSONObject("message").toString();
        }  catch (JSONException e) {
            Log.e(CLASS_NAME, "Error while read JSON message");
        }
        return draw;
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
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public int getIdMessage() {
        return id;
    }

    @SuppressWarnings("rawtypes")
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Draw createFromParcel(Parcel in) {
            return new Draw(in);
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
        dest.writeLong(timestamp);
        dest.writeInt(id);
    }

    private Draw(Parcel in) {
        this.getFromParcel(in);
    }

    private void getFromParcel(Parcel in) {
        author = in.readString();
        message = in.readString();
        timestamp = in.readLong();
        id = in.readInt();
    }
}
