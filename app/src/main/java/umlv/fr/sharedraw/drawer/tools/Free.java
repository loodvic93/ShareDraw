package umlv.fr.sharedraw.drawer.tools;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

public class Free implements Brush {
    private final Paint paint;
    private final Path path;
    private float x;
    private float y;

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
        canvas.save();
    }

    @Override
    public void changeColor(int color) {
        paint.setColor(color);
    }

    @Override
    public void start(float x, float y) {
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
        }
    }

    @Override
    public BrushType getBrushType() {
        return BrushType.FREE;
    }
}
