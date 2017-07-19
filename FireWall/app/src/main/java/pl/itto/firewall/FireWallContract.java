package pl.itto.firewall;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.util.List;

import pl.itto.firewall.base.BasePresenter;
import pl.itto.firewall.base.BaseView;
import pl.itto.firewall.data.AppItem;

/**
 * Created by PL_itto on 4/27/2017.
 */

public interface FireWallContract {
    interface View extends BaseView<Presenter> {
        void sortApps();
        void showNoApps();

        void showApps(List<AppItem> list);

        void updateState(boolean enabled);

        boolean isActive();

        void showLoadingAppsError();

        void showLoadingAppsCompleted(List<AppItem> listApps);

        void reloadAppList();

        void showLoadingAppsProgress();

        void stopLoadingAppsProgress();

        void toggleState(android.view.View v, boolean newState);

        void ruleSavedNotify();

        void showMsg(String msg);

        void openSortDialog();

        void clearSearch();

    }

    interface Presenter extends BasePresenter {
        Bundle getSetting();

        void saveSetting(Intent intent);

        boolean getState();

        void clearRules();

        void applyRules();

        void loadPrefs();

        void reloadAppList();

        void loadApps();

        void sortApps(Bundle setting);

        void toggleAppState(boolean isWifi, int pos, AppItem item, android.view.View v);
    }
}
