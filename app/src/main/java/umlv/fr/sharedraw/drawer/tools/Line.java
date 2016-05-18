package umlv.fr.sharedraw.drawer.tools;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Line implements Brush {
    private final Paint paint;
    private float x;
    private float y;
    private float x2;
    private float y2;

    public Line(float x, float y, float x2, float y2, int color) {
        this.x = x;
        this.y = y;
        this.x2 = x2;
        this.y2 = y2;
        this.paint = new Paint();
        paint.setStrokeWidth(STROKE_WIDTH);
        paint.setColor(color);
    }

    public Line() {
        this.paint = new Paint();
        paint.setStrokeWidth(STROKE_WIDTH);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawLine(x, y, x2, y2, paint);
    }

    @Override
    public void changeColor(int color) {
        paint.setColor(color);
    }

    @Override
    public void start(float x, float y) {
        this.x = x;
        this.y = y;
        this.x2 = x;
        this.y2 = y;
    }

    @Override
    public void moveTo(float x, float y) {
        this.x2 = x;
        this.y2 = y;
    }

    @Override
    public void setStroke(boolean stroke) {
        // Do nothing
    }

    @Override
    public String getJson() {
        StringBuilder json = new StringBuilder();
        json.append("{")
                .append("\"draw\": {")
                .append("\"shape\": \"line\",")
                .append("\"x\":").append(x).append(",")
                .append("\"y\":").append(y).append(",")
                .append("\"x2\":").append(x2).append(",")
                .append("\"y2\":")
                .append(y2)
                .append("},")
                .append("\"options\": {")
                .append("\"color\":")
                .append(paint.getColor())
                .append("}")
                .append("}");
        return json.toString();
    }
}
