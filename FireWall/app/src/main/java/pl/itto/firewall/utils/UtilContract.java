package pl.itto.firewall.utils;

import android.support.annotation.NonNull;

import java.util.List;

import pl.itto.firewall.data.AppDataSource;

/**
 * Created by PL_itto on 5/10/2017.
 */

public interface UtilContract {
    interface ApplyRulesCallback {
        void onApplyRuleDone(String msg);

        void onApplyFailed(String errMsg);
    }

    void applyIptablesRules(@NonNull ApplyRulesCallback callback, List<String> blockData, List<String> blockWifi);
    void clearIptablesRules(@NonNull ApplyRulesCallback callback);
}
