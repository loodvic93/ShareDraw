package umlv.fr.sharedraw.drawer.tools;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Square implements Brush {
    private final Paint paint;
    private float x;
    private float y;
    private float x2;
    private float y2;

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
    public BrushType getBrushType() {
        return BrushType.SQUARE;
    }
}
