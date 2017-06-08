package pl.itto.firewall;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;

import pl.itto.firewall.about.InfoActivity;
import pl.itto.firewall.data.AppItem;
import pl.itto.firewall.utils.Utils;

/**
 * Created by PL_itto on 4/27/2017.
 */

public class FireWallFragment extends Fragment implements FireWallContract.View {
    private static final String TAG = "PL_itto.FireWallFragment";
    private FireWallContract.Presenter mPresenter;
    private static boolean firstStart = true;

    private ImageView mImg_status_icon;
    private TextView mTxt_status;
    private ScrollChildSwipeRefreshLayout mScrollLayout;
    private RecyclerView mAppListView;
    private ListAppAdapter mAppsAdapter;
    private LinearLayout mLayoutAppList;
    private LinearLayout mLayoutNoApp;
    private FrameLayout mLayoutParentFW;

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
                super.onAdClosed();
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }

            @Override
            public void onAdLoaded() {
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
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void setPresenter(FireWallContract.Presenter presenter) {
        mPresenter = presenter;
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

        private ListAppAdapter(Context context) {
            mItemList = new ArrayList<>();
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
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View v = inflater.inflate(R.layout.fire_wall_list_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            AppItem item = mItemList.get(position);
            holder.bindView(item, position);
        }

        @Override
        public int getItemCount() {
            return mItemList.size();
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
}
