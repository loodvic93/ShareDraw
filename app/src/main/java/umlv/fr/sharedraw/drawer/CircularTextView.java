package umlv.fr.sharedraw.drawer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.TextView;

public class CircularTextView extends TextView {
    private int backgroundColor;

    public CircularTextView(Context context) {
        super(context);
    }

    public CircularTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CircularTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setBackground(Drawable background) {
        if (background instanceof ColorDrawable) {
            backgroundColor = ((ColorDrawable) background).getColor();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        Paint circlePaint = new Paint();
        circlePaint.setColor(backgroundColor);
        circlePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        int h = this.getHeight() - 15;
        int w = this.getWidth() - 15;
        int diameter = ((h > w) ? h : w);
        int radius = diameter / 2;
        this.setHeight(diameter);
        this.setWidth(diameter);
        canvas.drawCircle(diameter / 2, diameter / 2, radius, circlePaint);
        super.draw(canvas);
    }
}