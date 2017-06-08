package pl.itto.firewall.utils;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.File;
import java.util.List;

import pl.itto.firewall.data.AppItem;

/**
 * Created by PL_itto on 5/4/2017.
 */

public class Utils {
    public static <T> T checkNotNull(T value) {
        if (value == null)
            throw new NullPointerException();
        return value;
    }

    public static AppItem getApp(PackageInfo info, PackageManager pm) {
        AppItem item = new AppItem();
        int uid = info.applicationInfo.uid;
        item.setUID(uid);
        Drawable icon = info.applicationInfo.loadIcon(pm);
        if (icon != null)
            item.setIcon(icon);
        String name = info.applicationInfo.loadLabel(pm).toString();
        item.setNames(new String[]{name});
        return item;
    }

    public static int[] decodeBlockList(String list) {
        Log.i("PL_itto.Utils","block List: "+list);
        if (list != null) {
            String[] res = list.split("\\|");
            if (res != null) {
                int[] resList = new int[res.length];
                for (int i = 0; i < res.length; i++) {
                    resList[i] = Integer.parseInt(res[i]);
                }
                return resList;
            }
        }

        return null;
    }

    public static String writeRules(List<String> blockList) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < blockList.size(); i++) {
            builder.append(blockList.get(i));
            if (i != blockList.size() - 1)
                builder.append("|");
        }
        return builder.toString();
    }

    public static boolean checkRoot(){
        return findBinary("su");
    }
    private static boolean findBinary(String binaryName) {
        boolean found = false;
        if (!found) {
            String[] places = {"/sbin/", "/system/bin/", "/system/xbin/", "/data/local/xbin/",
                    "/data/local/bin/", "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/"};
            for (String where : places) {
                if ( new File( where + binaryName ).exists() ) {
                    found = true;
                    break;
                }
            }
        }
        return found;
    }
}
