package umlv.fr.sharedraw.drawer.tools;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Circle implements Brush {
    private float radius;
    private Paint paint;
    private float x;
    private float y;

    public Circle(float x, float y, float radius, int color, boolean stroke) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.paint = new Paint();
        this.paint.setStrokeWidth(STROKE_WIDTH);
        if (stroke) {
            this.paint.setStyle(Paint.Style.STROKE);
        } else {
            this.paint.setStyle(Paint.Style.FILL);
        }
        this.paint.setColor(color);
    }

    public Circle() {
        this.radius = 0;
        this.paint = new Paint();
        this.paint.setStyle(Paint.Style.STROKE);
        this.paint.setStrokeWidth(STROKE_WIDTH);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawCircle(x, y, radius, paint);
    }

    @Override
    public void changeColor(int color) {
        paint.setColor(color);
    }

    @Override
    public void start(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void moveTo(float x, float y) {
        this.radius = radiusCalc(x, y);
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
    @SuppressWarnings("all")
    public String getJson() {
        StringBuilder json = new StringBuilder();
        json.append("{")
                .append("\"draw\": {")
                .append("\"shape\": \"circle\",")
                .append("\"center\": [")
                .append(x)
                .append(", ")
                .append(y)
                .append("],")
                .append("\"radius\": ")
                .append(radius)
                .append("},")
                .append("\"options\": {")
                .append("\"color\":")
                .append(paint.getColor() + ",")
                .append("\"stroke\":")
                .append((paint.getStyle() == Paint.Style.STROKE))
                .append("}")
                .append("}");
        return json.toString();
    }

    private float radiusCalc(float x, float y) {
        float r1 = x - this.x;
        float r2 = y - this.y;
        r1 = r1 * r1;
        r2 = r2 * r2;
        return (float) Math.sqrt(r1 + r2);
    }
}
