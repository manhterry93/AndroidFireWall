package pl.itto.firewall;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import java.util.List;

import pl.itto.firewall.data.AppDataSource;
import pl.itto.firewall.data.AppItem;
import pl.itto.firewall.utils.UtilContract;

/**
 * Created by PL_itto on 4/27/2017.
 */

public class FireWallPresenter implements FireWallContract.Presenter {
    private static final String TAG = "PL_itto.FireWallPresenter";
    private final AppDataSource mAppRepository;
    private final UtilContract mUtilContract;
    private final FireWallContract.View mAppsView;

    public FireWallPresenter(@NonNull AppDataSource appRepository, @NonNull FireWallContract.View appsView, @NonNull UtilContract contract) {
        mUtilContract = contract;
        mAppRepository = appRepository;
        mAppsView = appsView;
        mAppsView.setPresenter(this);
    }


    @Override
    public void start() {
        loadApps();
        mAppsView.updateState(getState());
    }
    @Override
    public Bundle getSetting() {
        return mAppRepository.getSettings();
    }

    @Override
    public void saveSetting(Intent intent) {
        mAppRepository.saveSettings(intent);
        mAppsView.sortApps();
    }

    @Override
    public boolean getState() {
        return mAppRepository.getState();
    }

    @Override
    public void clearRules() {
        mAppRepository.saveRules(new AppDataSource.SaveRulesCallback() {
            @Override
            public void onRulesSaved() {
                mUtilContract.clearIptablesRules(new UtilContract.ApplyRulesCallback() {
                    @Override
                    public void onApplyRuleDone(String msg) {
                        mAppsView.showMsg(msg);
                        mAppRepository.saveState(false, null);
                        mAppsView.updateState(false);
                    }

                    @Override
                    public void onApplyFailed(String errMsg) {
                        mAppsView.showMsg(errMsg);
                    }
                });
            }
        });

    }

    @Override
    public void applyRules() {
        mAppRepository.saveRules(new AppDataSource.SaveRulesCallback() {
            @Override
            public void onRulesSaved() {
                Log.i(TAG, "Rule saved, execute");
                mUtilContract.applyIptablesRules(new UtilContract.ApplyRulesCallback() {
                    @Override
                    public void onApplyRuleDone(String msg) {
                        mAppsView.showMsg(msg);
                        mAppRepository.saveState(true, null);
                        mAppsView.updateState(true);
                    }

                    @Override
                    public void onApplyFailed(String errMsg) {
                        mAppsView.showMsg(errMsg);
                    }
                }, mAppRepository.getListBlock(false), mAppRepository.getListBlock(true));
            }
        });

    }

    @Override
    public void loadPrefs() {
        mAppRepository.loadPrefApps(new AppDataSource.LoadAppCallback() {
            @Override
            public void onAppsLoaded(List<AppItem> list) {
                mAppsView.stopLoadingAppsProgress();
                mAppsView.showApps(list);
            }
        });
    }

    @Override
    public void reloadAppList() {
        mAppsView.showApps(mAppRepository.getAppList());
    }

    @Override
    public void loadApps() {
        mAppsView.clearSearch();
        mAppsView.showLoadingAppsProgress();
        mAppRepository.getApps(new AppDataSource.LoadAppCallback() {
            @Override
            public void onAppsLoaded(List<AppItem> list) {
                loadPrefs();
            }
        });
    }

    @Override
    public void sortApps(Bundle setting) {

    }

//    @Override
//    public void applyRules(List<AppItem> list) {
//
//    }

    @Override
    public void toggleAppState(boolean isWifi, int pos, AppItem item, View v) {
        mAppRepository.toggleAppState(pos, isWifi);
        mAppsView.reloadAppList();
//        if (isWifi)
//            mAppsView.toggleState(v, !item.isBlockWifi());
//        else
//            mAppsView.toggleState(v, item.isBlockData());
    }

    @Override
    public void toggleAppState(boolean isWifi, AppItem item, View v) {
        mAppRepository.toggleAppState(item.getUID(), isWifi);
        mAppsView.reloadAppList();
    }


}
