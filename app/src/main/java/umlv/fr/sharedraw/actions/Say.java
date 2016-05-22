package umlv.fr.sharedraw.actions;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Say implements Action {
    private static final String CLASS_NAME = Say.class.getCanonicalName();
    private String author;
    private String message;
    private int id;
    private long timestamp;

    private Say() {
    }

    static Say createSayAction(JSONObject jsonObject) {
        Say say = new Say();
        try {
            say.id = jsonObject.getInt("id");
            say.author = jsonObject.getString("author");
            say.timestamp = jsonObject.getLong("timestamp") * 1000;
            say.message = jsonObject.getJSONObject("message").toString();
        } catch (JSONException e) {
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
    public int getId() {
        return id;
    }

    public String getContent() {
        try {
            JSONObject say = new JSONObject(message);
            JSONObject data = say.getJSONObject("say");
            return data.getString("content");
        } catch (JSONException e) {
            Log.e(CLASS_NAME, "Cannot read JSON");
            return null;
        }
    }

    public String getTime() {
        Date dateMessage = new Date(timestamp);
        Date today = new Date();
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(dateMessage);
        cal2.setTime(today);
        boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);

        String format = (sameDay) ?  "HH:mm" : "dd/MM";

        DateFormat formatter = new SimpleDateFormat(format);

        return formatter.format(dateMessage);
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
    }

    private Say(Parcel in) {
        this.getFromParcel(in);
    }

    private void getFromParcel(Parcel in) {
        author = in.readString();
        message = in.readString();
        timestamp = in.readLong();
    }
}
