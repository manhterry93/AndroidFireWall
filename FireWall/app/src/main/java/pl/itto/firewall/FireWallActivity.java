package pl.itto.firewall;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import pl.itto.firewall.data.AppRepository;
import pl.itto.firewall.utils.ActivityUtils;
import pl.itto.firewall.utils.FireWallUtils;

public class FireWallActivity extends AppCompatActivity {
    FloatingActionButton mFab;
    Toolbar mToolbar;
    FireWallPresenter mWallPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
}
