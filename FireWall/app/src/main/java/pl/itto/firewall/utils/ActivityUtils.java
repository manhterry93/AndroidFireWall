package pl.itto.firewall.utils;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

/**
 * Created by PL_itto on 4/27/2017.
 */

public class ActivityUtils {
    public static void addFragmentToActivity(@NonNull  FragmentManager fm, @NonNull Fragment fragment, int resId) {
        fm.beginTransaction().add(resId, fragment).commit();
    }
}
