package umlv.fr.sharedraw.drawer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import umlv.fr.sharedraw.R;
import umlv.fr.sharedraw.drawer.tools.Brush;
import umlv.fr.sharedraw.drawer.tools.Circle;
import umlv.fr.sharedraw.drawer.tools.Clean;
import umlv.fr.sharedraw.drawer.tools.Free;
import umlv.fr.sharedraw.drawer.tools.Line;
import umlv.fr.sharedraw.drawer.tools.Square;
import umlv.fr.sharedraw.notifier.NotifyDraw;

/**
 * Permit to draw on screen
 *
 * @author Olivier
 * @version 1.0
 */
public class CanvasView extends View {
    private Brush.BrushType brush = Brush.BrushType.FREE;
    private List<Brush> brushes = new ArrayList<>();
    private PointF start = new PointF();
    private NotifyDraw delegate = null;
    private PointF mid = new PointF();
    private static final int NONE = 0;
    private static final int DRAW = 1;
    private static final int DRAG = 2;
    private final Context mContext;
    private boolean stroke = true;
    private final Path mPath;
    private int mode = NONE;
    private Brush brushUsed;
    private Canvas mCanvas;
    private Paint mPaint;
    private float dx;
    private float dy;

    public CanvasView(Context context) {
        super(context);
        this.mContext = context;
        mPath = new Path();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(Brush.STROKE_WIDTH);
    }

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        mPath = new Path();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(Brush.STROKE_WIDTH);
    }

    public CanvasView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        mPath = new Path();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(Brush.STROKE_WIDTH);
    }

    @Override
    protected void onSizeChanged(int w, int h, int old_width, int old_height) {
        super.onSizeChanged(w, h, old_width, old_height);
        if (w > 0 && h > 0) {
            Bitmap mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }
    }

    public void changeColor(int color) {
        mPaint.setColor(color);
    }

    public void changeBrush(Brush.BrushType brush) {
        this.brush = brush;
    }

    public void stroke() {
        stroke = !stroke;
    }

    public void clearCanvas() {
        mPath.reset();
        brushes.clear();
        invalidate();
    }

    public void clearCanvas(boolean invalidate) {
        mPath.reset();
        brushes.clear();
        if (invalidate) {
            invalidate();
        } else {
            postInvalidate();
        }
    }

    public void save(String name) {
        File myFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator + "ShareDraw", name + ".bmp");
        if (myFile.exists()) {
            if (!myFile.delete()) {
                Toast.makeText(mContext, mContext.getString(R.string.error_save), Toast.LENGTH_SHORT).show();
                return;
            }
        }
        File myDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator + "ShareDraw");
        Boolean success = true;
        if (!myDir.exists()) {
            success = myDir.mkdir();
        }
        if (success) {
            try {
                FileOutputStream output = new FileOutputStream(myFile, true);
                Bitmap bitmap = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                this.draw(canvas);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
                Toast.makeText(mContext, mContext.getString(R.string.saved, name), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Toast.makeText(mContext, mContext.getString(R.string.error_save), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.error_save), Toast.LENGTH_SHORT).show();
        }
    }

    public void addBrush(Brush brush) {
        if (brush instanceof Clean) {
            clearCanvas();
            return;
        }
        brushes.add(brush);
    }

    public void delegate(NotifyDraw delegate) {
        this.delegate = delegate;
    }

    private void startTouch(float x, float y) {
        switch (brush) {
            case CIRCLE:
                brushUsed = new Circle();
                break;
            case FREE:
                brushUsed = new Free();
                break;
            case LINE:
                brushUsed = new Line();
                break;
            case SQUARE:
                brushUsed = new Square();
                break;
            default:
                brushUsed = new Free();
        }
        brushUsed.changeColor(mPaint.getColor());
        brushUsed.setStroke(stroke);
        brushUsed.start(x, y);
        brushes.add(brushUsed);
    }

    private void moveTouch(float x, float y) {
        brushUsed.moveTo(x, y);
    }

    private void upTouch() {
        brushUsed.draw(mCanvas);
        if (delegate != null) {
            delegate.notifyOnDraw(brushUsed);
        }
    }

    private void drawBrushes(Canvas canvas) {
        if (!brushes.isEmpty()) {
            for (Brush b : brushes) {
                if (b != null) {
                    b.draw(canvas);
                }
            }
        }
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(dx, dy);
        drawBrushes(canvas);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX() - dx;
        float y = event.getY() - dy;

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                start.set(x, y);
                mode = DRAW;
                startTouch(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                float oldDist = spacing(event);
                if (oldDist > 10f) {
                    midPoint(mid, event);
                    mode = DRAG;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAW) {
                    moveTouch(x, y);
                } else if (mode == DRAG) {
                    dx = event.getX() - start.x;
                    dy = event.getY() - start.y;
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (mode == DRAW)
                    upTouch();
                mode = NONE;
                invalidate();
                break;
        }
        return true;
    }


    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.brushesToSave = brushes;
        ss.paintToSave = mPaint;
        ss.brushToSave = brush;
        ss.brushUsedToSave = brushUsed;
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        this.brushes = ss.brushesToSave;
        this.mPaint = ss.paintToSave;
        this.brush = ss.brushToSave;
        this.brushUsed = ss.brushUsedToSave;
    }

    static class SavedState extends BaseSavedState {
        Paint paintToSave;
        List<Brush> brushesToSave;
        Brush.BrushType brushToSave;
        Brush brushUsedToSave;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            paintToSave = new Paint();
            paintToSave.setAntiAlias(true);
            paintToSave.setStrokeJoin(Paint.Join.ROUND);
            paintToSave.setStrokeWidth(Brush.STROKE_WIDTH);
            paintToSave.setStyle((in.readByte() == 1) ? Paint.Style.STROKE : Paint.Style.FILL);
            paintToSave.setColor(in.readInt());
            int sizeBrushes = in.readInt();
            brushesToSave = new ArrayList<>();
            for (int i = 0; i < sizeBrushes; i++) {
                Brush b = in.readParcelable(Brush.class.getClassLoader());
                brushesToSave.add(b);
            }
            brushToSave = Brush.BrushType.valueOf(in.readString());
            brushUsedToSave = in.readParcelable(Brush.class.getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeByte((byte) ((paintToSave.getStyle() == Paint.Style.STROKE) ? 1 : 0));
            out.writeInt(paintToSave.getColor());
            out.writeInt(brushesToSave.size());
            for (Brush b : brushesToSave) {
                out.writeParcelable(b, PARCELABLE_WRITE_RETURN_VALUE);
            }
            out.writeString(brushToSave.name());
            out.writeParcelable(brushUsedToSave, PARCELABLE_WRITE_RETURN_VALUE);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}