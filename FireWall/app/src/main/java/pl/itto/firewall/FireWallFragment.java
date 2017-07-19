package pl.itto.firewall;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import pl.itto.firewall.about.InfoActivity;
import pl.itto.firewall.data.AppItem;
import pl.itto.firewall.data.AppRepository;
import pl.itto.firewall.utils.Utils;

/**
 * Created by PL_itto on 4/27/2017.
 */

public class FireWallFragment extends Fragment implements FireWallContract.View {
    private static final String TAG = "PL_itto.FireWallFragment";

    private static final int REQUEST_SORT = 1;
    private FireWallContract.Presenter mPresenter;
    private static boolean firstStart = true;
    private AppCompatActivity mActivity;
    private Toolbar mToolbar;
    private ImageView mImg_status_icon;
    private TextView mTxt_status;
    private ScrollChildSwipeRefreshLayout mScrollLayout;
    private RecyclerView mAppListView;
    private ListAppAdapter mAppsAdapter;
    private LinearLayout mLayoutAppList;
    private LinearLayout mLayoutNoApp;
    private FrameLayout mLayoutParentFW;

    private ImageView mSortIcon;
    private SearchView mSearchView;
    private CheckBox mCheckDataAll, mCheckWifiAll;
    private AdView mAdView;
    private AdRequest mAdRequest;

    public static FireWallFragment newInstance() {
        return new FireWallFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        mAdRequest = new AdRequest.Builder().build();
        mActivity = (AppCompatActivity) getActivity();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fire_wall_frag, container, false);
        mLayoutParentFW = (FrameLayout) root.findViewById(R.id.fw_main_layout);
        mImg_status_icon = (ImageView) root.findViewById(R.id.fw_status_icon);
        mTxt_status = (TextView) root.findViewById(R.id.fw_status_title);
        mLayoutAppList = (LinearLayout) root.findViewById(R.id.fw_layout_app_list);
        mLayoutNoApp = (LinearLayout) root.findViewById(R.id.fw_no_app_layout);
        mScrollLayout = (ScrollChildSwipeRefreshLayout) root.findViewById(R.id.fw_refresh_layout);
        mAppListView = (RecyclerView) root.findViewById(R.id.fw_apps_list);
        mAppListView.setLayoutManager(new LinearLayoutManager(getContext()));

        mToolbar = (Toolbar) mActivity.findViewById(R.id.toolbar);
        mSearchView = (SearchView) mToolbar.findViewById(R.id.search);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.i(TAG, "onQUeryTextChange");
                if (mAppsAdapter != null) {
                    mAppsAdapter.filter(newText.toLowerCase());
                }
                return false;
            }
        });
        mAppsAdapter = new ListAppAdapter(getContext());
        mAppListView.setAdapter(mAppsAdapter);
        mScrollLayout.setScrollUpChild(mAppListView);
        mScrollLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.loadApps();
            }
        });


        /** Init AdView**/
        mAdView = (AdView) root.findViewById(R.id.adsView);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                Log.i(TAG, "Ads closed!");
                super.onAdClosed();
            }

            @Override
            public void onAdFailedToLoad(int i) {
                Log.i(TAG, "Ads failed to load!");
                super.onAdFailedToLoad(i);
            }

            @Override
            public void onAdLeftApplication() {
                Log.i(TAG, "Ads left application!");
                super.onAdLeftApplication();
            }

            @Override
            public void onAdOpened() {
                Log.i(TAG, "Ads opened!");
                super.onAdOpened();
            }

            @Override
            public void onAdLoaded() {
                Log.i(TAG, "Ads loaded!");
                super.onAdLoaded();
            }
        });

        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (firstStart) {
            mPresenter.start();
        }
        mAdView.loadAd(mAdRequest);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fire_wall_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setting:
                break;
            case R.id.action_stop:
                mPresenter.clearRules();
                break;
            case R.id.action_execute:
                mPresenter.applyRules();
                break;
            case R.id.action_detail:
                Intent i = new Intent(getContext(), InfoActivity.class);
                startActivity(i);
                break;
            case R.id.action_sort:
                openSortDialog();

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "omActivityResult: " + requestCode + " X " + resultCode);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_SORT:
                    if (data != null) {
                        Log.i(TAG, "sort: ");
                        mPresenter.saveSetting(data);
                    } else {
                        Log.e(TAG, "error");
                    }
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void setPresenter(FireWallContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void sortApps() {
        Log.i(TAG, "Start Sorting");
        mAppsAdapter.sort();
    }

    @Override
    public void showNoApps() {
        mScrollLayout.setScrollUpChild(mLayoutNoApp);
        mLayoutNoApp.setVisibility(View.VISIBLE);
        mLayoutAppList.setVisibility(View.GONE);
    }

    @Override
    public void showApps(List<AppItem> list) {
        mAppsAdapter.replaceData(list);
        if (!firstStart) {
            if (list.size() > 0) {
                mScrollLayout.setScrollUpChild(mAppListView);
                mLayoutAppList.setVisibility(View.VISIBLE);
                mLayoutNoApp.setVisibility(View.GONE);
            } else {
                showNoApps();
            }
        } else {
            firstStart = false;
        }
        if(list.size()>0){
            sortApps();
        }

    }

    @Override
    public void updateState(final boolean enabled) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (enabled) {
                    mImg_status_icon.setImageResource(R.drawable.ic_fw_on);
                    mTxt_status.setText(R.string.fw_on_title);
                } else {
                    mImg_status_icon.setImageResource(R.drawable.ic_fw_off);
                    mTxt_status.setText(R.string.fw_off_title);
                }
            }
        });

    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void showLoadingAppsError() {
    }

    @Override
    public void showLoadingAppsCompleted(List<AppItem> list) {

    }

    @Override
    public void reloadAppList() {
        mAppsAdapter.notifyDataSetChanged();
    }

    @Override
    public void showLoadingAppsProgress() {
        mScrollLayout.setRefreshing(true);
    }

    @Override
    public void stopLoadingAppsProgress() {
        Log.i(TAG, "stopLoading");
        mScrollLayout.setRefreshing(false);
    }

    @SuppressWarnings("unused")
    @Override
    public void toggleState(View v, boolean newState) {
        if (v instanceof ImageView) {
            if (newState)
                ((ImageView) v).setImageResource(AppItem.ResAllow);
            else
                ((ImageView) v).setImageResource(AppItem.ResBlock);
        }
    }

    @Override
    public void ruleSavedNotify() {
        showMsg(getString(R.string.snack_rule_saved));
    }


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mUid;
        ImageView mIcon;
        TextView mAppName;
        ImageView mWifi, m3G;
        AppItem mAppItem;
        int mPos;

        public ViewHolder(View itemView) {
            super(itemView);
            mUid = (TextView) itemView.findViewById(R.id.txt_uid);
            mIcon = (ImageView) itemView.findViewById(R.id.img_app_icon);
            mAppName = (TextView) itemView.findViewById(R.id.txt_app_detail);
            mWifi = (ImageView) itemView.findViewById(R.id.img_wifi_check);
            m3G = (ImageView) itemView.findViewById(R.id.img_3g_check);
            mWifi.setOnClickListener(this);
            m3G.setOnClickListener(this);
        }

        public void bindView(AppItem item, int position) {
            mPos = position;
            mAppItem = item;
            mUid.setText(String.valueOf(item.getUID()));
            mIcon.setImageDrawable(item.getIcon());
            mAppName.setText(item.getAllNames());
            mWifi.setImageResource(item.getRes(true));
            m3G.setImageResource(item.getRes(false));
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.img_3g_check:
                    Log.i(TAG, "onCheck: " + mPos);
                    mPresenter.toggleAppState(false, mPos, mAppItem, m3G);
                    break;
                case R.id.img_wifi_check:
                    Log.i(TAG, "onWifiCheck: " + mPos);
                    mPresenter.toggleAppState(true, mPos, mAppItem, mWifi);
                    break;
            }
        }
    }

    class ListAppAdapter extends RecyclerView.Adapter<ViewHolder> {
        Context mContext;
        List<AppItem> mItemList;
        List<AppItem> mItemListTemp;

        private ListAppAdapter(Context context) {
            mItemList = new ArrayList<>();
            mItemListTemp = new ArrayList<>();
            mContext = context;
        }

        public void replaceData(List<AppItem> list) {
            setList(list);
            notifyDataSetChanged();
        }

        private Context getContext() {
            return mContext;
        }

        void setList(List<AppItem> list) {
            mItemList = list;
            mItemListTemp.clear();
            mItemListTemp.addAll(list);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View v = inflater.inflate(R.layout.fire_wall_list_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            AppItem item = mItemListTemp.get(position);
            holder.bindView(item, position);
        }

        @Override
        public int getItemCount() {
            return mItemListTemp.size();
        }

        private void filter(String args) {
            mItemListTemp.clear();
            for (int i = 0; i < mItemList.size(); i++) {
                AppItem item = mItemList.get(i);
                if (String.valueOf(item.getUID()).toLowerCase().contains(args) || item.getAllNames().toLowerCase().contains(args)) {
                    mItemListTemp.add(item);
                }
            }
            notifyDataSetChanged();
        }

        void sort() {
            Bundle bundle = mPresenter.getSetting();
            int type = bundle.getInt(AppRepository.SORT_TYPE_KEY);
            boolean blocked_top = bundle.getBoolean(AppRepository.SORT_BLOCK_KEY);
            Log.i(TAG, "Sort: ,type: " + type + " blockTop: " + blocked_top);
            SortComparator comparator = new SortComparator(blocked_top, type);
            Collections.sort(mItemList, comparator);
            Collections.sort(mItemListTemp, comparator);
            notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.reloadAppList();
    }

    public void showMsg(String msg) {
        Snackbar.make(mLayoutParentFW, msg, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void openSortDialog() {
        Bundle bundle = mPresenter.getSetting();
        SortDialog dialog = SortDialog.newInstance(bundle);
        dialog.setTargetFragment(this, REQUEST_SORT);
        dialog.show(getFragmentManager(), "");
    }

    @Override
    public void clearSearch() {
        if (mSearchView != null) {
            mSearchView.setQuery("", false);
            mSearchView.clearFocus();
        }


    }

    protected class SortComparator implements java.util.Comparator<AppItem> {
        private boolean mBlockTop;
        private int mType;

        public SortComparator(boolean blockTop, int type) {
            mBlockTop = blockTop;
            mType = type;
        }

        @Override
        public int compare(AppItem o1, AppItem o2) {
            if (mBlockTop) {
                if (o1.isBlocked()) {
                    if (o2.isBlocked()) {
                        switch (mType) {
                            case AppRepository.SORT_A_Z:
                                return o1.getAllNames().toLowerCase().compareTo(o2.getAllNames().toLowerCase());
                            case AppRepository.SORT_Z_A:
                                return o2.getAllNames().toLowerCase().compareTo(o1.getAllNames().toLowerCase());
                            case AppRepository.SORT_UID_UP:
                                return o1.getUID() - o2.getUID();
                            case AppRepository.SORT_UID_DOWN:
                                return o2.getUID() - o1.getUID();
                        }

                    } else {
                        return 1;
                    }
                } else {
                    if (o2.isBlocked()) {
                        return -1;
                    } else {
                        switch (mType) {
                            case AppRepository.SORT_A_Z:
                                return o1.getAllNames().toLowerCase().compareTo(o2.getAllNames().toLowerCase());
                            case AppRepository.SORT_Z_A:
                                return o2.getAllNames().toLowerCase().compareTo(o1.getAllNames().toLowerCase());
                            case AppRepository.SORT_UID_UP:
                                return o1.getUID() - o2.getUID();
                            case AppRepository.SORT_UID_DOWN:
                                return o2.getUID() - o1.getUID();
                        }
                    }
                }
            } else {
                switch (mType) {
                    case AppRepository.SORT_A_Z:
                        return o1.getAllNames().toLowerCase().compareTo(o2.getAllNames().toLowerCase());
                    case AppRepository.SORT_Z_A:
                        return o2.getAllNames().toLowerCase().compareTo(o1.getAllNames().toLowerCase());
                    case AppRepository.SORT_UID_UP:
                        return o1.getUID() - o2.getUID();
                    case AppRepository.SORT_UID_DOWN:
                        return o2.getUID() - o1.getUID();
                }
            }
            return o1.getAllNames().compareTo(o2.getAllNames());
        }
    }
}