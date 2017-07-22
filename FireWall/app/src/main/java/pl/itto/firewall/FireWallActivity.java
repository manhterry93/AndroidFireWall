package pl.itto.firewall;

import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import pl.itto.firewall.data.AppRepository;
import pl.itto.firewall.utils.ActivityUtils;
import pl.itto.firewall.utils.FireWallUtils;

public class FireWallActivity extends AppCompatActivity {
    FloatingActionButton mFab;
    Toolbar mToolbar;
    FireWallPresenter mWallPresenter;
    Handler mExitHandler;
    boolean mExit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mExitHandler = new Handler();
        setContentView(R.layout.fire_wall_act);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);


        FireWallFragment fragment = (FireWallFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (fragment == null) {
            fragment = FireWallFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), fragment, R.id.contentFrame);
        }

        AppRepository repository = AppRepository.getInstance(this);
        FireWallUtils fireWallUtils = FireWallUtils.getInstance(this);
        mWallPresenter = new FireWallPresenter(repository, fragment, fireWallUtils);
        fireWallUtils.assertBinaries();
    }

    @Override
    public void onBackPressed() {
        if (mExit) {
            mExit = false;
            System.exit(0);
        } else {
            mExit = true;
            Toast.makeText(this, getString(R.string.exit_toast), Toast.LENGTH_SHORT).show();
            mExitHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mExit = false;
                }
            }, 1500);
        }
    }
}
