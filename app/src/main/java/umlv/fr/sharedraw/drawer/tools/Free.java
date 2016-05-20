package umlv.fr.sharedraw.drawer.tools;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import java.util.ArrayList;
import java.util.List;

import umlv.fr.sharedraw.drawer.tools.point.Point;

public class Free implements Brush {
    private final List<Point> points = new ArrayList<>();
    private final Paint paint;
    private final Path path;
    private float x;
    private float y;

    public Free(List<Point> points, int color) {
        this.paint = new Paint();
        this.paint.setAntiAlias(true);
        this.paint.setStyle(Paint.Style.STROKE);
        this.paint.setStrokeJoin(Paint.Join.ROUND);
        this.paint.setStrokeWidth(STROKE_WIDTH);
        this.paint.setColor(color);
        this.path = new Path();

        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);
            if (i == 0) {
                this.path.moveTo(p.x, p.y);
            } else {
                this.path.lineTo(p.x, p.y);
            }
            this.x = p.x;
            this.y = p.y;
        }
    }

    public Free() {
        this.paint = new Paint();
        this.path = new Path();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(STROKE_WIDTH);
    }

    @Override
    public void draw(Canvas canvas) {
        path.lineTo(this.x, this.y);
        canvas.drawPath(path, paint);
    }

    @Override
    public void changeColor(int color) {
        paint.setColor(color);
    }

    @Override
    public void start(float x, float y) {
        points.add(new Point(x, y));
        path.moveTo(x, y);
        this.x = x;
        this.y = y;
    }

    @Override
    public void moveTo(float x, float y) {
        float dx = Math.abs(x - this.x);
        float dy = Math.abs(y - this.y);
        if (dx >= FAULT_TOLERANT || dy >= FAULT_TOLERANT) {
            path.quadTo(this.x, this.y, (x + this.x) / 2, (y + this.y) / 2);
            this.x = x;
            this.y = y;
            points.add(new Point(x, y));
        }
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
                .append("\"shape\": \"free\",")
                .append("\"coordinates\":[");

        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);
            if (i == points.size() - 1) {
                json.append("[").append(p.x).append(",").append(p.y).append("]]");
            } else {
                json.append("[").append(p.x).append(",").append(p.y).append("], ");
            }
        }

        json.append("},")
                .append("\"options\": {")
                .append("\"color\":")
                .append(paint.getColor())
                .append("}")
                .append("}");
        return json.toString();
    }
}
