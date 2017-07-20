package pl.itto.firewall;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import pl.itto.firewall.data.AppRepository;

/**
 * Created by PL_itto on 7/8/2017.
 */

public class SortDialog extends DialogFragment {
    private RadioGroup mGroupSort;
    private CheckBox mCheckBlockTop;
    private RadioButton mRadioA_Z, mRadio_Z_A, mRadio_UID_up, mRadio_UID_down;

    public static SortDialog newInstance(Bundle args) {
        SortDialog fragment = new SortDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View v = getActivity().getLayoutInflater().inflate(R.layout.sort_dialog, null);
        mCheckBlockTop = (CheckBox) v.findViewById(R.id.check_sort_blocked);
        setupRadioGroup(v);
        loadSettings();
        return new AlertDialog.Builder(getContext())
                .setView(v)
                .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getTargetFragment().onActivityResult(1, Activity.RESULT_CANCELED, null);
                        dismiss();
                    }
                })
                .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();

                        intent.putExtra(AppRepository.SORT_TYPE_KEY, getSortType());
                        intent.putExtra(AppRepository.SORT_BLOCK_KEY, isSortBlockedTop());

                        getTargetFragment().onActivityResult(1, Activity.RESULT_OK, intent);
                        dismiss();
                    }
                })
                .create();
    }

    private void setupRadioGroup(View v) {
        mRadioA_Z = (RadioButton) v.findViewById(R.id.srt_a_z);
        mRadio_Z_A = (RadioButton) v.findViewById(R.id.srt_z_a);
        mRadio_UID_down = (RadioButton) v.findViewById(R.id.srt_uid_down);
        mRadio_UID_up = (RadioButton) v.findViewById(R.id.srt_uid_up);
        mGroupSort = (RadioGroup) v.findViewById(R.id.group_sort);
        String[] arrays = getResources().getStringArray(R.array.sort_item);
        for (int i = 0; i < mGroupSort.getChildCount(); i++) {
            RadioButton radioButton = (RadioButton) mGroupSort.getChildAt(i);
            radioButton.setText(arrays[i]);
        }
    }

    private int getSortType() {
        switch (mGroupSort.getCheckedRadioButtonId()) {
            case R.id.srt_a_z:
                return AppRepository.SORT_A_Z;
            case R.id.srt_z_a:
                return AppRepository.SORT_Z_A;
            case R.id.srt_uid_up:
                return AppRepository.SORT_UID_UP;
            case R.id.srt_uid_down:
                return AppRepository.SORT_UID_DOWN;
        }
        return AppRepository.SORT_A_Z;
    }

    private boolean isSortBlockedTop() {
        return mCheckBlockTop.isChecked();
    }

    private void loadSettings() {
        mCheckBlockTop.setChecked(getArguments().getBoolean(AppRepository.SORT_BLOCK_KEY, true));
        switch (getArguments().getInt(AppRepository.SORT_TYPE_KEY)) {
            case AppRepository.SORT_A_Z:
                mRadioA_Z.setChecked(true);
                break;
            case AppRepository.SORT_Z_A:
                mRadio_Z_A.setChecked(true);
                break;
            case AppRepository.SORT_UID_UP:
                mRadio_UID_up.setChecked(true);
                break;
            case AppRepository.SORT_UID_DOWN:
                mRadio_UID_down.setChecked(true);
                break;
            default:
                mRadioA_Z.setChecked(true);
                break;
        }
    }
}
