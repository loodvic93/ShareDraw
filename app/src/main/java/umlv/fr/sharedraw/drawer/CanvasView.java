package umlv.fr.sharedraw.drawer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import umlv.fr.sharedraw.MainFragmentActivity;
import umlv.fr.sharedraw.NotifyDraw;
import umlv.fr.sharedraw.drawer.tools.Brush;
import umlv.fr.sharedraw.drawer.tools.Circle;
import umlv.fr.sharedraw.drawer.tools.Free;
import umlv.fr.sharedraw.drawer.tools.Line;
import umlv.fr.sharedraw.drawer.tools.Square;

/**
 * Permit to draw on screen
 *
 * @author Olivier
 * @version 1.0
 */
public class CanvasView extends View {
    private final List<Brush> brushes = new ArrayList<>();
    private Brush.BrushType brush = Brush.BrushType.FREE;
    private NotifyDraw delegate = null;
    private final Context mContext;
    private boolean stroke = true;
    private final Paint mPaint;
    private final Path mPath;
    private Brush brushUsed;
    private Bitmap mBitmap;
    private Canvas mCanvas;




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
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
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

    public void save() {
        View content = this;
        content.setDrawingCacheEnabled(true);
        content.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        Bitmap bitmap = content.getDrawingCache();
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        System.out.println("PATH = " + path);
        path = Environment.getDataDirectory().getAbsolutePath();
        File file = new File(path+"/board.png");
        FileOutputStream ostream;
        try {
            file.createNewFile();
            ostream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, ostream);
            ostream.flush();
            ostream.close();
            Toast.makeText(mContext, "image saved", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext, "error", Toast.LENGTH_LONG).show();
        }
    }

    public void addBrush(Brush brush) {

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

    // override onDraw
    @Override
    protected void onDraw(Canvas canvas) {
        if (!brushes.isEmpty()) {
            for (Brush b : brushes) {
                b.draw(canvas);
            }
        } else {
            canvas.drawColor(Color.WHITE);
        }
    }


    //override the onTouchEvent
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startTouch(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                moveTouch(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                upTouch();
                invalidate();
                break;
        }
        return true;
    }
}