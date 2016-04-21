package umlv.fr.sharedraw;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.Random;

import umlv.fr.sharedraw.drawer.CanvasView;

public class DashboardActivity extends AppCompatActivity {
    private String author;
    private String title;
    private CanvasView canvasView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        author = intent.getStringExtra("author");
        title = intent.getStringExtra("title");
        setContentView(R.layout.activity_dashboard);
        canvasView = (CanvasView)findViewById(R.id.canvas);
    }

    public void clearCanvas(View v) {
        canvasView.clearCanvas();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("author", author);
        outState.putString("title", title);
    }
}