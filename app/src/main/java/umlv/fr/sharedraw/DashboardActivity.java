package umlv.fr.sharedraw;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;

import umlv.fr.sharedraw.actions.Action;
import umlv.fr.sharedraw.actions.Draw;
import umlv.fr.sharedraw.drawer.CanvasView;
import umlv.fr.sharedraw.drawer.tools.Brush;
import umlv.fr.sharedraw.drawer.tools.Clean;


public class DashboardActivity extends Fragment implements NotifyService, NotifyDraw {
    private ArrayList<Integer> actionForCurrentUser = new ArrayList<>();
    private CanvasView drawer;
    private String mUsername;
    private String mTitle;


    public DashboardActivity() {

    }

    public static DashboardActivity newInstance(String title, String username) {
        DashboardActivity dashboardFragment = new DashboardActivity();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("username", username);
        dashboardFragment.setArguments(args);
        return dashboardFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initVariable(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_dashboard, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        drawer = (CanvasView) getActivity().findViewById(R.id.canvas);
        drawer.delegate(this);


        getActivity().findViewById(R.id.palette).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
                if (view.getId() != R.id.palette) return false;

                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        params.topMargin = (int) event.getRawY() - view.getHeight();
                        params.leftMargin = (int) event.getRawX() - (view.getWidth() / 2);
                        view.setLayoutParams(params);
                        break;

                    case MotionEvent.ACTION_UP:
                        params.topMargin = (int) event.getRawY() - view.getHeight();
                        params.leftMargin = (int) event.getRawX() - (view.getWidth() / 2);
                        view.setLayoutParams(params);
                        break;

                    case MotionEvent.ACTION_DOWN:
                        view.setLayoutParams(params);
                        break;
                }

                return true;
            }
        });


        ImageButton stroke = (ImageButton) getActivity().findViewById(R.id.imageButton_stroke);
        stroke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.stroke();
            }
        });

        ImageButton save = (ImageButton) getActivity().findViewById(R.id.imageButton_save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.save(mTitle.replaceAll("_", " "));
            }
        });

        ImageButton clean = (ImageButton) getActivity().findViewById(R.id.imageButton_clear);
        clean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.clearCanvas();
                notifyCleanCanvas();
            }
        });

        ImageButton free = (ImageButton) getActivity().findViewById(R.id.imageButton_line);
        free.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.changeBrush(umlv.fr.sharedraw.drawer.tools.Brush.BrushType.FREE);
            }
        });

        ImageButton square = (ImageButton) getActivity().findViewById(R.id.imageButton_square);
        square.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.changeBrush(umlv.fr.sharedraw.drawer.tools.Brush.BrushType.SQUARE);
            }
        });

        ImageButton circle = (ImageButton) getActivity().findViewById(R.id.imageButton_circle);
        circle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.changeBrush(umlv.fr.sharedraw.drawer.tools.Brush.BrushType.CIRCLE);
            }
        });

        ImageButton fixedLine = (ImageButton) getActivity().findViewById(R.id.fixedline);
        fixedLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.changeBrush(umlv.fr.sharedraw.drawer.tools.Brush.BrushType.LINE);
            }
        });

        final ImageButton black = (ImageButton) getActivity().findViewById(R.id.imageButton_color_black);
        black.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.changeColor(Color.BLACK);
                hideAllColorButton();
            }
        });

        ImageButton white = (ImageButton) getActivity().findViewById(R.id.imageButton_color_white);
        white.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.changeColor(Color.WHITE);
                hideAllColorButton();
            }
        });

        ImageButton red = (ImageButton) getActivity().findViewById(R.id.imageButton_color_red);
        red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.changeColor(Color.RED);
                hideAllColorButton();
            }
        });

        ImageButton green = (ImageButton) getActivity().findViewById(R.id.imageButton_color_green);
        green.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.changeColor(Color.GREEN);
                hideAllColorButton();
            }
        });

        ImageButton blue = (ImageButton) getActivity().findViewById(R.id.imageButton_color_blue);
        blue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.changeColor(Color.BLUE);
                hideAllColorButton();
            }
        });

        ImageButton yellow = (ImageButton) getActivity().findViewById(R.id.imageButton_color_yellow);
        yellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.changeColor(Color.YELLOW);
                hideAllColorButton();
            }
        });

        ImageButton palette = (ImageButton) getActivity().findViewById(R.id.paletteChoose);
        palette.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (black.getVisibility() == View.GONE) {
                    showAllColorButton();
                } else {
                    hideAllColorButton();
                }
            }
        });
    }

    private void hideAllColorButton() {
        ImageButton black = (ImageButton) getActivity().findViewById(R.id.imageButton_color_black);
        ImageButton white = (ImageButton) getActivity().findViewById(R.id.imageButton_color_white);
        ImageButton red = (ImageButton) getActivity().findViewById(R.id.imageButton_color_red);
        ImageButton green = (ImageButton) getActivity().findViewById(R.id.imageButton_color_green);
        ImageButton blue = (ImageButton) getActivity().findViewById(R.id.imageButton_color_blue);
        ImageButton yellow = (ImageButton) getActivity().findViewById(R.id.imageButton_color_yellow);
        black.setVisibility(View.GONE);
        white.setVisibility(View.GONE);
        red.setVisibility(View.GONE);
        green.setVisibility(View.GONE);
        blue.setVisibility(View.GONE);
        yellow.setVisibility(View.GONE);
    }

    private void showAllColorButton() {
        ImageButton black = (ImageButton) getActivity().findViewById(R.id.imageButton_color_black);
        ImageButton white = (ImageButton) getActivity().findViewById(R.id.imageButton_color_white);
        ImageButton red = (ImageButton) getActivity().findViewById(R.id.imageButton_color_red);
        ImageButton green = (ImageButton) getActivity().findViewById(R.id.imageButton_color_green);
        ImageButton blue = (ImageButton) getActivity().findViewById(R.id.imageButton_color_blue);
        ImageButton yellow = (ImageButton) getActivity().findViewById(R.id.imageButton_color_yellow);
        black.setVisibility(View.VISIBLE);
        white.setVisibility(View.VISIBLE);
        red.setVisibility(View.VISIBLE);
        green.setVisibility(View.VISIBLE);
        blue.setVisibility(View.VISIBLE);
        yellow.setVisibility(View.VISIBLE);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("username", mUsername);
        outState.putString("title", mTitle);
        outState.putIntegerArrayList("actionForCurrentUser", actionForCurrentUser);
    }

    @SuppressWarnings("all")
    private void initVariable(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mUsername = savedInstanceState.getString("username");
            mTitle = savedInstanceState.getString("title");
            actionForCurrentUser = savedInstanceState.getIntegerArrayList("actionForCurrentUser");
        } else {
            Bundle bundle = getArguments();
            mUsername = bundle.getString("username");
            mTitle = bundle.getString("title");
        }
    }

    @Override
    public void notifyServiceConnected() {
        List<Action> draws = MainFragmentActivity.HTTP_SERVICE.getListOfDrawAction();
        if (draws != null) {
            for (Action draw : draws) {
                drawer.addBrush(((Draw) draw).getBrush());
            }
            drawer.invalidate();
        }
        MainFragmentActivity.HTTP_SERVICE.delegateDrawerActivity(this);
    }

    @Override
    public void notifyOnDraw(umlv.fr.sharedraw.drawer.tools.Brush brush) {
        MainFragmentActivity.HTTP_SERVICE.postMessage(getString(R.string.server), mTitle, "&author=" + mUsername + "&message=" + brush.getJson());
    }

    public void notifyCleanCanvas() {
        MainFragmentActivity.HTTP_SERVICE.postMessage(getString(R.string.server), mTitle, "&author=" + mUsername + "&message={\"draw\":{\"shape\": \"clean\"}}");
    }

    @Override
    public void notifyNewDraw(Brush brush) {
        if (brush == null) return;
        if (brush instanceof Clean) {
            drawer.clearCanvas(false);
        } else {
            drawer.addBrush(brush);
            drawer.postInvalidate();
        }
    }
}