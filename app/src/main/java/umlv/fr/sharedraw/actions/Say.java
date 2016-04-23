package umlv.fr.sharedraw.actions;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Say implements Action {
    private static final String CLASS_NAME = Say.class.getCanonicalName();
    private String author;
    private String message;
    private long timestamp;
    private int id;
    private List<String> dest;

    private Say() { }

    static Say createSayAction(JSONObject jsonObject) {
        Say say = new Say();
        try {
            say.id = jsonObject.getInt("id");
            say.author = jsonObject.getString("author");
            say.timestamp = jsonObject.getLong("timestamp");
            say.message = jsonObject.getJSONObject("message").toString();

            say.dest = new ArrayList<>();
            JSONArray destination = jsonObject.getJSONObject("message").optJSONArray("destination");
            if (destination != null) {
                for (int i = 0; i < destination.length(); i++) {
                    say.dest.add(destination.getString(i));
                }
            }
        }  catch (JSONException e) {
            Log.e(CLASS_NAME, "Error while read JSON message");
        }
        return say;
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

    public List<String> getDestinations() {
        return Collections.unmodifiableList(dest);
    }

    @SuppressWarnings("rawtypes")
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Say createFromParcel(Parcel in) {
            return new Say(in);
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
        dest.writeStringList(this.dest);
    }

    private Say(Parcel in) {
        this.getFromParcel(in);
    }

    private void getFromParcel(Parcel in) {
        author = in.readString();
        message = in.readString();
        timestamp = in.readLong();
        id = in.readInt();
        dest = new ArrayList<>();
        in.readList(dest, String.class.getClassLoader());
    }
}
