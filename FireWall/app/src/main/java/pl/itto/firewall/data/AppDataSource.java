package pl.itto.firewall.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Created by PL_itto on 5/4/2017.
 */

public interface AppDataSource {
    interface LoadAppCallback {
        void onAppsLoaded(List<AppItem> list);
    }

    interface SaveRulesCallback {
        void onRulesSaved();
    }

    interface SaveStateCallback {
        void onStateSaved();
    }

    void toggleAppState(int pos, boolean isWifi);

    void getApps(@NonNull LoadAppCallback callback);

    void loadPrefApps(@NonNull LoadAppCallback callback);

    void updateAppsCount(int count);

    List<String> getListBlock(boolean wifi);

    List<AppItem> getAppList();

    void saveRules(@NonNull SaveRulesCallback callback);

    void saveState(boolean on, @Nullable SaveStateCallback callback);

    boolean getState();
}
