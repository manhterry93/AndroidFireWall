package pl.itto.firewall.bootup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import pl.itto.firewall.data.AppRepository;
import pl.itto.firewall.utils.ScriptExecuter;
import pl.itto.firewall.utils.UtilContract;

/**
 * Created by PL_itto on 6/5/2017.
 */

public class BootupReceiver extends BroadcastReceiver {
    private static final String TAG = "PL_itto.BootupReceiver";
    private static final String PATH_CONFIG = "/etc/itto_wall_config.xml";
    /**
     * root script file name
     */

    private static final String SCRIPT_FILE = "firewall.sh";
    private Handler mHandler;
    @Override
    public void onReceive(final Context context, Intent intent) {
        mHandler=new Handler();
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppRepository.SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
        boolean enabled = sharedPreferences.getBoolean(AppRepository.FIREWALL_STATE, false);
        if (enabled) {
            Log.i(TAG,"BootUp completed, execute firewall rules");
            //Execute firewall
            String dir = context.getDir("bin", 0).getAbsolutePath();
            File file = new File(dir + "/" + SCRIPT_FILE);
            ScriptExecuter executer = new ScriptExecuter(context.getApplicationContext(), new UtilContract.ApplyRulesCallback() {
                @Override
                public void onApplyRuleDone(String msg) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context,"ApplyRule Done",Toast.LENGTH_SHORT).show();
                        }
                    });

                    Log.i(TAG, "firewall is execute");
                }

                @Override
                public void onApplyFailed(String errMsg) {
                    Log.e(TAG, "Error on execute firewall: " + errMsg);
                }
            });

            executer.setData(file, null, true);
            executer.start();
        }


    }


}
