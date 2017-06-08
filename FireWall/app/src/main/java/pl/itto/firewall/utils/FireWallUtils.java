package pl.itto.firewall.utils;

import android.content.Context;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import pl.itto.firewall.R;
import pl.itto.firewall.data.AppDataSource;
import pl.itto.firewall.data.AppRepository;

/**
 * Created by PL_itto on 4/28/2017.
 */

public class FireWallUtils implements UtilContract {
    private static final String TAG = "PL_itto.FireWallUtils";
    public static final boolean DEBUG = true;
    private static final int IPTABLES_RAW_ID = R.raw.iptables_armv5;
    private static final int BUSYBOX_RAW_ID = R.raw.busybox_g1;

    /**
     * For shell script
     */
    final String ITFS_WIFI[] = {"tiwlan+", "wlan+", "eth+", "ra+"};
    final String ITFS_DATA[] = {"rmnet+", "pdp+", "ppp+", "uwbr+", "wimax+", "vsnet+", "ccmni+", "usb+"};
    private static final String RULE_CHAIN_MAIN = "itto_wall";
    private static final String RULE_CHAIN_DATA = "itto_data";
    private static final String RULE_CHAIN_WIFI = "itto_wifi";
    private static final String RULE_CHAIN_BLOCK = "itto_block";

    private static FireWallUtils INSTANCE;
    private Context mContext;
    private String mIptablesRaw = "", mBusyBoxRaw = "";
    private String dir = "";
    private String myIptables = "";
    private String myBusyBox = "";
    private boolean hasRoot = false;

    public static FireWallUtils getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new FireWallUtils(context);
        }
        return INSTANCE;
    }


    /**
     * special application UID used to indicate "any application"
     */
    public static final int SPECIAL_UID_ANY = -10;

    /**
     * special application UID used to indicate the Linux Kernel
     */
    public static final int SPECIAL_UID_KERNEL = -11;

    /**
     * root script file name
     */
    private static final String SCRIPT_FILE = "firewall.sh";


    private FireWallUtils(Context context) {
        mContext = context;
        mIptablesRaw = context.getString(R.string.raw_iptables);
        mBusyBoxRaw = context.getString(R.string.raw_busy_box);
        dir = mContext.getDir("bin", 0).getAbsolutePath();
        myIptables = dir + "/" + mIptablesRaw;
        myBusyBox = dir + "/" + mBusyBoxRaw;
        hasRoot = Utils.checkRoot();
    }

    /**
     * Create the generic shell script header used to determine which iptables binary to use
     *
     * @return script's header
     */
    private String scriptHeader() {

        return "" +
                "IPTABLES=iptables\n" +
                "BUSYBOX=busybox\n" +
                "GREP=grep\n" +
                "ECHO=echo\n" +
                "# Try to find busybox\n" +
                "if busybox --help >/dev/null ; then\n" +
                "   BUSYBOX=busybox\n" +
                "elif /system/xbin/busybox --help >/dev/null ;then\n" +
                "   BUSYBOX=/system/xbin/busybox\n" +
                "elif /system/bin/busybox --help >/dev/null ;then\n" +
                "   BUSYBOX=/system/bin/busybox\n" +
                "elif " + myBusyBox + " --help >/dev/null; then\n" +
                "   BUSYBOX=" + myBusyBox + "\n" +
                "   GREP= \"$BUSYBOX grep\"\n" +
                "   ECHO=\"$BUSYBOX echo\"\n" +
                "fi\n" +
                "# Try to find grep\n" +
                "if ! $ECHO 1 | $GREP -q 1 >/dev/null ;then\n" +
                "   if $ECHO 1 | $BUSYBOX grep -q 1> /dev/null ; then\n" +
                "       GREP=\"$BUSYBOX grep\"\n" +
                "   fi\n" +
                "   # Grep is absolutely required\n" +
                "   if ! $ECHO 1 | GREP -q 1 >/dev/null ; then\n" +
                "       $ECHO The grep command is required. FireWall will not work.\n" +
                "       exit 1\n" +
                "   fi\n" +
                "fi\n" +
                "# Try to find iptables\n" +
                "if ! iptables --version >/dev/null ;then\n" +
                "   if " + myIptables + " --version >/dev/null ; then\n" +
                "   IPTABLES =" + myIptables + "\n" +
                "   fi\n"+
                "fi\n" +
                "";
    }

    /**
     * Copy raw files that the firewall need to run
     *
     * @param resId raw file's id
     * @param file  path where the raw file is copied to
     * @param mode  mode (777,755,...)
     * @return
     */
    private boolean copyRawFile(int resId, File file, String mode) {
        final String absPath = file.getAbsolutePath();

        //Write the binary
        try {
            final FileOutputStream out = new FileOutputStream(file);
            final InputStream is = mContext.getResources().openRawResource(resId);
            byte buf[] = new byte[1024];
            int len;
            while ((len = is.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            is.close();
            // Change the permissions
            Runtime.getRuntime().exec("chmod -R " + mode + " " + absPath).waitFor();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException: " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.toString());
        } catch (InterruptedException e) {
            Log.e(TAG, "InterruptedException: " + e.toString());
        }
        return true;
    }

    /**
     * Make sure Binaries is exist
     *
     * @return true if binaries is exist or successfully installed, false if not
     */
    public boolean assertBinaries() {
        boolean changed = false;
        boolean result = true;
        StringBuilder builder = new StringBuilder();
        // Check iptables
        File file = new File(dir, mIptablesRaw);
        if (!file.exists()) {
            if (copyRawFile(IPTABLES_RAW_ID, file, "755"))
                changed = true;
            else {
                builder.append(" Iptables");
                result = false;
            }
        }

        // Check busybox
        file = new File(dir, mBusyBoxRaw);
        if (!file.exists()) {
            if (copyRawFile(BUSYBOX_RAW_ID, file, "755"))
                changed = true;
            else {
                if (result) {
                    builder.append(" Busybox");
                } else {
                    builder.append(" and Busybox");
                }
            }
        }

        if (changed) {
            if (result)
                showMsg(mContext.getString(R.string.toast_bin_install_done));
            else {
                showMsg(mContext.getString(R.string.toast_bin_install_error, builder.toString()));
            }
        }

        return true;
    }

    private void showMsg(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void applyIptablesRules(@NonNull ApplyRulesCallback callback, List<String> blockData, List<String> blockWifi) {
        assertBinaries();

        //Create Script for start firewall
        StringBuilder script = new StringBuilder();
        script.append(scriptHeader());
        // Init the firewall chain
        script.append("" +
                "$IPTABLES -V || exit \n" +
                "# Create the firewall chain if necessary\n" +
                "$IPTABLES -L " + RULE_CHAIN_MAIN + " >/dev/null || $IPTABLES -N " + RULE_CHAIN_MAIN + " || exit \n" +
                "$IPTABLES -L " + RULE_CHAIN_DATA + " >/dev/null || $IPTABLES -N " + RULE_CHAIN_DATA + " || exit \n" +
                "$IPTABLES -L " + RULE_CHAIN_WIFI + " >/dev/null || $IPTABLES -N " + RULE_CHAIN_WIFI + " || exit \n" +
                "$IPTABLES -L " + RULE_CHAIN_BLOCK + " >/dev/null || $IPTABLES -N " + RULE_CHAIN_BLOCK + " || exit \n" +
                "# Add firewall chain to OUTPUT if necessary\n" +
                "$IPTABLES -L OUTPUT | $GREP -q " + RULE_CHAIN_MAIN + " || $IPTABLES -A OUTPUT -j " + RULE_CHAIN_MAIN + " || exit \n" +
                "# Flush existing rules\n" +
                "$IPTABLES -F " + RULE_CHAIN_MAIN + " || exit \n" +
                "$IPTABLES -F " + RULE_CHAIN_DATA + " || exit \n" +
                "$IPTABLES -F " + RULE_CHAIN_WIFI + " || exit \n" +
                "$IPTABLES -F " + RULE_CHAIN_BLOCK + " || exit \n" +
                ""
        );

        // Add rule for each chain
        script.append("$IPTABLES -A " + RULE_CHAIN_BLOCK + " -j REJECT || exit \n");

        for (String interf : ITFS_DATA) {
            script.append("$IPTABLES -A " + RULE_CHAIN_MAIN + " -o " + interf + " -j " + RULE_CHAIN_DATA + " || exit \n");
        }

        for (String interf : ITFS_WIFI) {
            script.append("$IPTABLES -A " + RULE_CHAIN_MAIN + " -o " + interf + " -j " + RULE_CHAIN_WIFI + " || exit \n");
        }
        // add Filtering rule
        script.append("# Filtering rules \n");

        for (String uid : blockData)
            script.append("$IPTABLES -A " + RULE_CHAIN_DATA + " -m owner --uid-owner " +
                    uid + " -j " + RULE_CHAIN_BLOCK + " || exit \n");
        for (String uid : blockWifi)
            script.append("$IPTABLES -A " + RULE_CHAIN_WIFI + " -m owner --uid-owner " +
                    uid + " -j " + RULE_CHAIN_BLOCK + " || exit \n");

        //Run script
        ScriptExecuter scriptExecuter = new ScriptExecuter(mContext, callback);
        File file = new File(dir + "/" + SCRIPT_FILE);
        scriptExecuter.setData(file, script.toString(), true);
        scriptExecuter.start();
    }

    @Override
    public void clearIptablesRules(@NonNull ApplyRulesCallback callback) {
        //Create script
        StringBuilder script = new StringBuilder();
        assertBinaries();
        script.append(scriptHeader());
        script.append("" +
                "$IPTABLES -F " + RULE_CHAIN_MAIN + "\n" +
                "$IPTABLES -F " + RULE_CHAIN_BLOCK + "\n" +
                "$IPTABLES -F " + RULE_CHAIN_WIFI + "\n" +
                "$IPTABLES -F " + RULE_CHAIN_DATA + "\n"
        );
        ScriptExecuter scriptExecuter=new ScriptExecuter(mContext,callback);
        File file = new File(dir + "/" + SCRIPT_FILE);
        scriptExecuter.setData(file, script.toString(), true);
        scriptExecuter.start();
    }
}



