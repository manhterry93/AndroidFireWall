package pl.itto.firewall.data;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import pl.itto.firewall.FireWallContract;
import pl.itto.firewall.utils.Utils;

import static pl.itto.firewall.utils.Utils.checkNotNull;

/**
 * Created by PL_itto on 5/4/2017.
 */

public class AppRepository implements AppDataSource {
    private static final String TAG = "PL_itto.AppRepository";
    /**
     * Sharedpreference
     */
    public static final String SHARE_PREFERENCE_NAME = "firewall_pref";
    public static final String BLOCK_WIFI_KEY = "block_wifi";
    public static final String BLOCK_DATA_KEY = "block_data";
    public static final String FIREWALL_STATE = "fw_state";

    private static AppRepository sAppRepository = null;

    private Context mContext;
    private FireWallContract.Presenter mPresenter;
    private int mAppcount;
    private List<AppItem> mAppsList = new ArrayList<>();
    private SharedPreferences mSharedPreferences;
    private List<String> mBlockDataList;
    private List<String> mBlockWifiList;

    private AppRepository(Context context) {
        mContext = context;
        mAppsList = new ArrayList<>();
        mSharedPreferences = mContext.getSharedPreferences(SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
        //Init parameters
        mBlockDataList = new ArrayList<>();
        mBlockWifiList = new ArrayList<>();

    }

    public static AppRepository getInstance(Context context) {
        if (sAppRepository == null) {
            sAppRepository = new AppRepository(context);
        }
        return sAppRepository;
    }

    @Override
    public void toggleAppState(int pos, boolean isWifi) {
        AppItem item = mAppsList.get(pos);
        if (isWifi)
            item.setBlockWifi(!item.isBlockWifi());
        else
            item.setBlockData(!item.isBlockData());
    }

    @Override
    public void getApps(@NonNull LoadAppCallback callback) {
        checkNotNull(callback);
        LoadAppAsync loadAppAsync = new LoadAppAsync(callback);
        loadAppAsync.execute();
    }

    @Override
    public void loadPrefApps(@NonNull LoadAppCallback callback) {
        checkNotNull(callback);
        int[] wifiBlockList = Utils.decodeBlockList(mSharedPreferences.getString(BLOCK_WIFI_KEY, null));
        int[] dataBlockList = Utils.decodeBlockList(mSharedPreferences.getString(BLOCK_DATA_KEY, null));
        for (AppItem item : mAppsList) {
            int uid = item.getUID();
            if (dataBlockList != null)

                if (dataBlockList != null && Arrays.binarySearch(dataBlockList, uid) >= 0) {
                    item.setBlockData(true);
                }
            if (wifiBlockList != null && Arrays.binarySearch(wifiBlockList, uid) >= 0) {
                item.setBlockWifi(true);
            }
        }
        callback.onAppsLoaded(mAppsList);
    }

    @Override
    public void updateAppsCount(int count) {
        mAppcount = count;
    }

    @Override
    public List<String> getListBlock(boolean wifi) {
        if (wifi) return getBlockWifiList();
        else return getBlockDataList();
    }

    @Override
    public List<AppItem> getAppList() {
        return mAppsList;
    }

    @Override
    public void saveRules(@NonNull SaveRulesCallback callback) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.clear();
        mBlockDataList.clear();
        mBlockWifiList.clear();
        for (int i = 0; i < mAppsList.size(); i++) {
            AppItem item = mAppsList.get(i);
            String uid = String.valueOf(item.getUID());
            if (item.isBlockData())
                mBlockDataList.add(uid);
            if (item.isBlockWifi())
                mBlockWifiList.add(uid);
        }
        Collections.sort(mBlockDataList, mRuleComparator);
        Collections.sort(mBlockWifiList, mRuleComparator);
        String ruleData = Utils.writeRules(mBlockDataList);
        String ruleWifi = Utils.writeRules(mBlockWifiList);
        Log.i(TAG, "Rule data: " + ruleData);
        Log.i(TAG, "Rule Wifi: " + ruleWifi);
        if (!ruleData.isEmpty())
            editor.putString(BLOCK_DATA_KEY, ruleData);
        if (!ruleWifi.isEmpty())
            editor.putString(BLOCK_WIFI_KEY, ruleWifi);
        editor.commit();
        callback.onRulesSaved();
    }

    @Override
    public void saveState(boolean enable, @Nullable SaveStateCallback callback) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(FIREWALL_STATE, enable);
        boolean done = editor.commit();
        if (done && callback != null) {
            callback.onStateSaved();
        }
    }

    @Override
    public boolean getState() {
        return mSharedPreferences.getBoolean(FIREWALL_STATE, false);
    }


    class LoadAppAsync extends AsyncTask<Void, Void, List<AppItem>> {
        LoadAppCallback mLoadAppCallback;

        public LoadAppAsync(LoadAppCallback callback) {
            mLoadAppCallback = callback;
        }

        @Override
        protected List<AppItem> doInBackground(Void... params) {
            HashMap<Integer, AppItem> listApps = new HashMap<>();
            AppItem tempItem;
            PackageManager pm = mContext.getPackageManager();
            List<PackageInfo> apps = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);
            Log.i(TAG, "app list size: " + apps.size());
            for (PackageInfo packageInfo : apps) {
                if (packageInfo.requestedPermissions == null)
                    continue;
                for (String permission : packageInfo.requestedPermissions) {
                    if (TextUtils.equals(permission, Manifest.permission.INTERNET)) {
                        int uid = packageInfo.applicationInfo.uid;
                        tempItem = listApps.get(uid);
                        if (tempItem == null) {
                            tempItem = Utils.getApp(packageInfo, pm);
                            listApps.put(uid, tempItem);
                        } else {
                            // have 2 or more app with an UID, merge names to 1
                            String newNames[] = new String[tempItem.getNames().length + 1];
                            System.arraycopy(tempItem.getNames(), 0, newNames, 0, tempItem.getNames().length);
                            newNames[tempItem.getNames().length] = packageInfo.applicationInfo.loadLabel(pm).toString();
                            tempItem.setNames(newNames);
                        }
                    }
                }
            }

            mAppsList.clear();
            mAppsList.addAll(listApps.values());
            Collections.sort(mAppsList, mAppItemComparator);
            return mAppsList;
        }

        @Override
        protected void onPostExecute(List<AppItem> appItems) {
            super.onPostExecute(appItems);
            mLoadAppCallback.onAppsLoaded(appItems);
        }
    }

    private Comparator<AppItem> mAppItemComparator = new Comparator<AppItem>() {
        @Override
        public int compare(AppItem o1, AppItem o2) {
            return o1.getAllNames().compareTo(o2.getAllNames());
        }
    };

    private Comparator<String> mRuleComparator = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    };

    private List<String> getBlockDataList() {
        return mBlockDataList;
    }

    private List<String> getBlockWifiList() {
        return mBlockWifiList;
    }
}
