package umlv.fr.sharedraw.drawer.tools;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Circle implements Brush {
    private float radius;
    private Paint paint;
    private float x;
    private float y;

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
    public BrushType getBrushType() {
        return BrushType.CIRCLE;
    }

    private float radiusCalc(float x, float y) {
        float r1 = x - this.x;
        float r2 = y - this.y;
        r1 = r1 * r1;
        r2 = r2 * r2;
        return (float) Math.sqrt(r1 + r2);
    }
}
