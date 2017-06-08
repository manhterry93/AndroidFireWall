package pl.itto.firewall.data;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import pl.itto.firewall.R;

/**
 * Created by PL_itto on 4/27/2017.
 */

public class AppItem {
    public static final int ResAllow = R.drawable.allow;
    public static final int ResBlock = R.drawable.block;

    private Drawable mIcon;
    private int mUID;
    private String[] mNames;
    private boolean mBlockData;
    private boolean mBlockWifi;

    public Drawable getIcon() {
        return mIcon;
    }

    public void setIcon(Drawable icon) {
        mIcon = icon;
    }
    public AppItem(){
        mBlockData=false;
        mBlockWifi=false;
    }
    public int getUID() {
        return mUID;
    }

    public void setUID(int UID) {
        mUID = UID;
    }


    public boolean isBlockData() {
        return mBlockData;
    }

    public void setBlockData(boolean blockData) {
        mBlockData = blockData;
    }

    public boolean isBlockWifi() {
        return mBlockWifi;
    }

    public void setBlockWifi(boolean blockWifi) {
        mBlockWifi = blockWifi;
    }

    public String[] getNames() {
        return mNames;
    }

    public String getAllNames() {
        if (mNames != null) ;
        StringBuilder builder = new StringBuilder();
        int count = 0;
        for (String string : mNames) {
            count++;
            builder.append(string);
            if (count < mNames.length)
                builder.append(", ");
        }
        return builder.toString();
    }

    public void setNames(String[] names) {
        mNames = names;
    }

    public int getRes(boolean wifi) {
        if (wifi) {
            if (isBlockWifi())
                return ResBlock;
            return ResAllow;
        } else {
            if (isBlockData())
                return ResBlock;
            return ResAllow;
        }
    }
}
