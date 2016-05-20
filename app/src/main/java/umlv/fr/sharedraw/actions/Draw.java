package umlv.fr.sharedraw.actions;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import umlv.fr.sharedraw.drawer.tools.Circle;
import umlv.fr.sharedraw.drawer.tools.Free;
import umlv.fr.sharedraw.drawer.tools.Line;
import umlv.fr.sharedraw.drawer.tools.Square;
import umlv.fr.sharedraw.drawer.tools.point.Point;

public class Draw implements Action {
    private static final String CLASS_NAME = Draw.class.getCanonicalName();
    private String author;
    private String message;
    private int id;

    private Draw() { }

    static Draw createDrawAction(JSONObject jsonObject) {
        Draw draw = new Draw();
        try {
            draw.id = jsonObject.getInt("id");
            draw.author = jsonObject.getString("author");
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
    public int getId() {
        return id;
    }

    public umlv.fr.sharedraw.drawer.tools.Brush getBrush() {
        umlv.fr.sharedraw.drawer.tools.Brush brush = null;
        try {
            JSONObject json = new JSONObject(message);
            JSONObject shape = json.getJSONObject("draw");
            String brushName = shape.getString("shape");
            switch (brushName) {
                case "circle":
                    JSONArray center = shape.getJSONArray("center");
                    float x = (float)center.getDouble(0);
                    float y = (float)center.getDouble(1);
                    float radius = (float)shape.getDouble("radius");
                    JSONObject options = json.getJSONObject("options");
                    int color = options.getInt("color");
                    boolean stroke = options.getBoolean("stroke");
                    brush = new Circle(x, y, radius, color, stroke);
                    break;
                case "square":
                    x = (float)shape.getDouble("x");
                    y = (float)shape.getDouble("y");
                    float x2 = (float)shape.getDouble("x2");
                    float y2 = (float)shape.getDouble("y2");
                    options = json.getJSONObject("options");
                    color = options.getInt("color");
                    stroke = options.getBoolean("stroke");
                    brush = new Square(x, y, x2, y2, color, stroke);
                    break;
                case "line":
                    x = (float)shape.getDouble("x");
                    y = (float)shape.getDouble("y");
                    x2 = (float)shape.getDouble("x2");
                    y2 = (float)shape.getDouble("y2");
                    options = json.getJSONObject("options");
                    color = options.getInt("color");
                    brush = new Line(x, y, x2, y2, color);
                    break;
                case "free":
                    JSONArray coordinates = shape.getJSONArray("coordinates");
                    List<Point> points = new ArrayList<>();
                    for (int i = 0; i < coordinates.length(); i++) {
                        JSONArray point = coordinates.getJSONArray(i);
                        points.add(new Point((float)point.getDouble(0), (float)point.getDouble(1)));
                    }
                    options = json.getJSONObject("options");
                    color = options.getInt("color");
                    brush = new Free(points, color);
                    break;
                default:
                    return null;
            }
        } catch (JSONException e) {
            Log.e(CLASS_NAME, "Cannot create a brush");
        }

        return brush;
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
    }

    private Draw(Parcel in) {
        this.getFromParcel(in);
    }

    private void getFromParcel(Parcel in) {
        author = in.readString();
        message = in.readString();
    }
}
