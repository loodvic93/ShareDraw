package umlv.fr.sharedraw;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import umlv.fr.sharedraw.drawer.ViewPagerCustom;
import umlv.fr.sharedraw.http.HttpService;

public class MainFragmentActivity extends AppCompatActivity {
    private UserConnectedActivity userConnectedActivity;
    private DashboardActivity dashboardActivity;
    private MessageActivity messageActivity;
    public static HttpService HTTP_SERVICE;
    public static String mUsername;
    public static String mTitle;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            HttpService.HttpBinder binder = (HttpService.HttpBinder) service;
            HTTP_SERVICE = binder.getService();
            signalToJoinDashboard();

            if (dashboardActivity != null) {
                dashboardActivity.notifyServiceConnected();
            }

            if (userConnectedActivity != null) {
                userConnectedActivity.notifyServiceConnected();
            }

            if (messageActivity != null) {
                messageActivity.notifyServiceConnected();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            HTTP_SERVICE = null;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_fragment);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        String[] TITLES = {getString(R.string.tab1), getString(R.string.tab2), getString(R.string.tab3)};
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), TITLES);

        // Set up the ViewPager with the sections adapter.
        ViewPagerCustom mViewPager = (ViewPagerCustom) findViewById(R.id.container);
        assert mViewPager != null;
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        assert tabLayout != null;
        tabLayout.setupWithViewPager(mViewPager);

        initVariable(savedInstanceState);
    }

    private void signalToJoinDashboard() {
        HTTP_SERVICE.signalToJoinDashboard(getString(R.string.server), mTitle, mUsername);
    }

    private void signalToQuitDashboard() {
        HTTP_SERVICE.signalToLeaveDashboard(getString(R.string.server), mTitle, mUsername);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("username", mUsername);
        outState.putString("title", mTitle);
    }

    @Override
    protected void onPause() {
        signalToQuitDashboard();
        unbindService(connection);
        super.onPause();
    }

    @Override
    protected void onResume() {
        Intent intent = new Intent(this, HttpService.class);
        intent.putExtra("server", getString(R.string.server));
        intent.putExtra("title", mTitle);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        super.onResume();
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private final String[] tabs;

        public SectionsPagerAdapter(FragmentManager fm, String[] tabs) {
            super(fm);
            this.tabs = tabs;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    dashboardActivity = DashboardActivity.newInstance(mTitle, mUsername);
                    return dashboardActivity;
                case 1:
                    userConnectedActivity = UserConnectedActivity.newInstance();
                    return userConnectedActivity;
                case 2:
                    System.out.println("CASE 2");
                    messageActivity = MessageActivity.newInstance();
                    return messageActivity;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return tabs.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabs[position];
        }
    }

    @SuppressWarnings("all")
    private void initVariable(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mUsername = savedInstanceState.getString("username");
            mTitle = savedInstanceState.getString("title");
        } else {
            Intent intent = getIntent();
            mUsername = intent.getStringExtra("username");
            mTitle = intent.getStringExtra("title");
        }
        setTitle(mTitle.replaceAll("_", " "));
    }
}
