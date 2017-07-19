package pl.itto.firewall;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.SearchView;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Created by PL_itto on 7/6/2017.
 */

public class CustomSearchView extends SearchView {

    public CustomSearchView(Context context) {
        super(context);
    }

    public CustomSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setEditTextStyle();
    }
    private void setEditTextStyle(){
        EditText editText= (EditText) findViewById(R.id.search_src_text);
        editText.setHintTextColor(getResources().getColor(R.color.color_search_hint));
        editText.setTextColor(getResources().getColor(R.color.color_search_text));
    }
}
