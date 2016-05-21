package umlv.fr.sharedraw.drawer.tools;

import android.graphics.Canvas;
import android.os.Parcelable;

public interface Brush extends Parcelable {
    enum BrushType {LINE, CIRCLE, SQUARE, FREE}

    int FAULT_TOLERANT = 5;
    int STROKE_WIDTH = 25;

    void draw(Canvas canvas);

    void changeColor(int color);

    void start(float x, float y);

    void moveTo(float x, float y);

    void setStroke(boolean stroke);

    String getJson();
}
