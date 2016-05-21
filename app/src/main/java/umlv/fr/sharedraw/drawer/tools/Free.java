package umlv.fr.sharedraw.drawer.tools;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import umlv.fr.sharedraw.drawer.tools.point.Point;

public class Free implements Brush {
    private final List<Point> points = new ArrayList<>();
    private Paint paint;
    private Path path;
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

    @SuppressWarnings("rawtypes")
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Free createFromParcel(Parcel in) {
            return new Free(in);
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
        dest.writeInt(paint.getColor());
        dest.writeInt(points.size());
        for (Point p : points) {
            dest.writeFloat(p.x);
            dest.writeFloat(p.y);
        }
    }

    private Free(Parcel in) {
        this.getFromParcel(in);
    }

    private void getFromParcel(Parcel in) {
        this.paint = new Paint();
        this.paint.setAntiAlias(true);
        this.paint.setStyle(Paint.Style.STROKE);
        this.paint.setStrokeJoin(Paint.Join.ROUND);
        this.paint.setStrokeWidth(STROKE_WIDTH);
        this.paint.setColor(in.readInt());
        this.path = new Path();

        int sizePoint = in.readInt();
        for (int i = 0; i < sizePoint; i++) {
            Point p = new Point(in.readFloat(), in.readFloat());
            points.add(p);
            if (i == 0) {
                this.path.moveTo(p.x, p.y);
            } else {
                this.path.lineTo(p.x, p.y);
            }
            this.x = p.x;
            this.y = p.y;
        }
    }
}
