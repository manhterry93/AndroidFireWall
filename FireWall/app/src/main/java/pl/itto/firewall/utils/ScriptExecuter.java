package pl.itto.firewall.utils;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import pl.itto.firewall.R;
import pl.itto.firewall.utils.UtilContract.ApplyRulesCallback;

/**
 * Created by PL_itto on 5/10/2017.
 */

public class ScriptExecuter extends Thread {
    private static final String TAG = "PL_itto.ScriptExecuter";
    private ApplyRulesCallback mRulesCallback;
    private File mScriptFile;
    private String mScript;
    private boolean mAsRoot = false;
    private Context mContext;
    private Process exec;
    private int exitCode;
    private boolean mUseScript = true;

    public ScriptExecuter(Context context, ApplyRulesCallback callback) {
        this.mContext = context;
        this.mRulesCallback = callback;
        mUseScript = false;
    }

    /**
     * Set data to run
     *
     * @param file   file to execute script
     * @param script script that will be writed to file. null if no need write to file
     * @param root   run as Root
     */
    public void setData(File file, @Nullable String script, boolean root) {
//        Log.i(TAG, "script: " + script);
        if (script != null) {
            mUseScript = true;
            this.mScript = script;
        } else
            mUseScript = false;
        this.mScriptFile = file;
        mAsRoot = root;
    }


    @Override
    public void run() {
//        super.run();
        try {
            mScriptFile.createNewFile();
            String path = mScriptFile.getAbsolutePath();
            if (mUseScript) {
                OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(mScriptFile));
                if (new File("/system/bin/sh").exists())
                    out.write("#!/system/bin/sh\n");
                out.write(mScript);
                if (!mScript.endsWith("\n"))
                    out.write("\n");
                out.write("exit \n");
                out.flush();
                out.close();
            }
            //Start execute the cmd
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stdout = process.getInputStream();
            InputStream stderr = process.getErrorStream();
            stdin.write(("chmod -R 777 " + path + "\n").getBytes());
            stdin.flush();
            stdin.write(("su -c " + path).getBytes());
            stdin.flush();
            stdin.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
            String line;
            while ((line = reader.readLine()) != null) {
                Log.i(TAG, "output: " + line);
            }
            reader.close();

            reader = new BufferedReader(new InputStreamReader(stderr));
            while ((line = reader.readLine()) != null) {
                Log.e(TAG, "error: " + line);
            }
            reader.close();
            process.waitFor();
            process.destroy();
            exitCode = 0;
        } catch (IOException e) {
            exitCode = 1;
            Log.e(TAG, "Error on create script file: " + e.toString());
        } catch (InterruptedException e) {
            exitCode = 1;
            Log.e(TAG, "Error on grant permission for script file: " + e.toString());
        } finally {
            destroyProcess();
        }
    }

    public synchronized void destroyProcess() {
        if (exec != null)
            exec.destroy();
        exec = null;
        if (exitCode == 0)
            mRulesCallback.onApplyRuleDone(mContext.getString(R.string.snack_execute_done));
        else
            mRulesCallback.onApplyFailed(mContext.getString(R.string.snack_execute_failed));
    }
}
