package umlv.fr.sharedraw.drawer.tools;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Square implements Brush {
    private final Paint paint;
    private float x;
    private float y;
    private float x2;
    private float y2;

    public Square(float x, float y, float x2, float y2, int color, boolean stroke) {
        this.x = x;
        this.y = y;
        this.x2 = x2;
        this.y2 = y2;
        this.paint = new Paint();
        this.paint.setStrokeWidth(STROKE_WIDTH);
        if (stroke) {
            this.paint.setStyle(Paint.Style.STROKE);
        } else {
            this.paint.setStyle(Paint.Style.FILL);
        }
        this.paint.setColor(color);
    }

    public Square() {
        this.paint = new Paint();
        this.paint.setStyle(Paint.Style.STROKE);
        this.paint.setStrokeWidth(STROKE_WIDTH);
    }

    @Override
    public void draw(Canvas canvas) {
        if (x <= x2 && y <= y2) {
            canvas.drawRect(x, y - FAULT_TOLERANT, x2, y2 - FAULT_TOLERANT, paint);
        } else if (x > x2 && y <= y2) {
            canvas.drawRect(x2, y - FAULT_TOLERANT, x, y2 - FAULT_TOLERANT, paint);
        } else if (x <= x2 && y > y2) {
            canvas.drawRect(x, y2 - FAULT_TOLERANT, x2, y - FAULT_TOLERANT, paint);
        } else {
            canvas.drawRect(x2, y2 - FAULT_TOLERANT, x, y - FAULT_TOLERANT, paint);
        }
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
        if (stroke) {
            this.paint.setStyle(Paint.Style.STROKE);
        } else {
            this.paint.setStyle(Paint.Style.FILL);
        }
    }

    @Override
    public String getJson() {
        return "{" +
                "\"draw\": {" +
                "\"shape\": \"square\"," +
                "\"x\":" + x + "," +
                "\"y\":" + y + "," +
                "\"x2\":" + x2 + "," +
                "\"y2\":" +
                y2 +
                "}," +
                "\"options\": {" +
                "\"color\":" + paint.getColor() + "," +
                "\"stroke\":" +
                (paint.getStyle() == Paint.Style.STROKE) +
                "}" +
                "}";
    }
}
