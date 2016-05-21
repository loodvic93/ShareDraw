package umlv.fr.sharedraw.drawer.tools;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;

public class Clean implements Brush {
    @Override
    public void draw(Canvas canvas) {
        // Do nothing
    }

    @Override
    public void changeColor(int color) {
        // Do nothing
    }

    @Override
    public void start(float x, float y) {
        // Do nothing
    }

    @Override
    public void moveTo(float x, float y) {
        // Do nothing
    }

    @Override
    public void setStroke(boolean stroke) {
        // Do nothing
    }

    @Override
    public String getJson() {
        // Do nothing
        return null;
    }

    @SuppressWarnings("rawtypes")
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Clean createFromParcel(Parcel in) {
            return new Clean();
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
    }

    public Clean() {}
}
