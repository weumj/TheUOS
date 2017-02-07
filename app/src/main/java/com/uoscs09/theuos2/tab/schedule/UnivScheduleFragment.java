package com.uoscs09.theuos2.tab.schedule;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsProgressFragment;
import com.uoscs09.theuos2.util.AppRequests;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.CollectionUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import se.emilsjolander.stickylistheaders.ExpandableStickyListHeadersListView;


public class UnivScheduleFragment extends AbsProgressFragment<List<UnivScheduleItem>> {

    @Override
    protected int layoutRes() {
        return R.layout.tab_univ_schedule;
    }

    private static final int PERMISSION_REQUEST_CALENDAR = 12;

    @BindView(R.id.list)
    ExpandableStickyListHeadersListView mListView;
    //private AlertDialog mItemSelectDialog;
    private Dialog mProgressDialog;
    private UnivScheduleAdapter mAdapter;

    private final ArrayList<UnivScheduleItem> mList = new ArrayList<>();
    private UnivScheduleItem mSelectedItem;
    private String mSubTitle;
    private final SimpleDateFormat mDateFormat = new SimpleDateFormat("MMMM", Locale.getDefault());

    private String mAccount;


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("list", mList);
        outState.putString("subTitle", mSubTitle);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mList.clear();

            ArrayList<UnivScheduleItem> list = savedInstanceState.getParcelableArrayList("list");
            if (list != null) mList.addAll(list);

            mSubTitle = savedInstanceState.getString("subTitle");
            setSubtitleWhenVisible(mSubTitle);
        }
    }

    @Override
    protected void setPrevAsyncData(List<UnivScheduleItem> data) {
        if(mList.isEmpty()) CollectionUtil.addAll(mList, data);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAdapter = new UnivScheduleAdapter(getActivity(), mList);

        mListView.setNestedScrollingEnabled(true);
/*
        mListView.setOnItemClickListener((parent, view1, position, id) -> {
            mSelectedItem = mList.get(position);

            mItemSelectDialog.setMessage(String.format(getString(R.string.tab_univ_schedule_add_schedule_to_calender), mSelectedItem.content));
            mItemSelectDialog.show();
        });
*/
        mListView.setAdapter(mAdapter);
        mListView.setOnHeaderClickListener((l, header, itemPosition, headerId, currentlySticky) -> {
            boolean collapsed = mListView.isHeaderCollapsed(headerId);
            if (collapsed) {
                mListView.expand(headerId);
            } else {
                mListView.collapse(headerId);
            }

            //todo fix
            Object o = header.getTag();
            if (o != null && o instanceof UnivScheduleAdapter.HeaderViewHolder) {
                ((UnivScheduleAdapter.HeaderViewHolder) o).setBarVisible(!collapsed);
            }

        });

        registerProgressView(view.findViewById(R.id.progress_layout));

        /*
        mItemSelectDialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.tab_univ_schedule_add_to_calendar)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    sendClickEvent("add schedule to calender");

                    checkCalenderConditionAndAddSchedule();
                })
                .setNegativeButton(android.R.string.no, null)
                .setMessage("")
                .create();
                */

        mProgressDialog = AppUtil.getProgressDialog(getActivity(), false, getString(R.string.progress_ongoing), null);


        if (mList.isEmpty())
            execute(false);
    }
/*
    @RequiresPermission(Manifest.permission.GET_ACCOUNTS)
    private Account[] deviceAccounts() {
        //noinspection MissingPermission
        return AccountManager.get(getActivity()).getAccountsByType("com.google");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_REQUEST_CALENDAR:
                if (checkPermissionResultAndShowToastIfFailed(permissions, grantResults, R.string.tab_univ_schedule_permission_denied)) {
                    checkCalenderConditionAndAddSchedule();
                }
                break;

            default:
                break;
        }
    }


    private void checkCalenderConditionAndAddSchedule() {
        if (mSelectedItem == null) {
            AppUtil.showToast(getActivity(), R.string.tab_univ_schedule_schedule_selection_not_exist);
            return;
        }

        String[] permissions = {Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR, Manifest.permission.GET_ACCOUNTS};

        if (!checkSelfPermissions(permissions)) {
            requestPermissions(permissions, PERMISSION_REQUEST_CALENDAR);
            return;
        }

        if (mAccount != null) {
            addUnivScheduleToCalender();
            return;
        }

        //noinspection MissingPermission
        Account[] accounts = deviceAccounts();
        if (accounts.length < 1) {
            AppUtil.showToast(getActivity(), R.string.tab_univ_schedule_google_account_not_exist);
        } else if (accounts.length == 1) {
            mAccount = accounts[0].name;
            addUnivScheduleToCalender();
        } else {
            CharSequence[] accountOptions = new CharSequence[accounts.length];
            int i = 0;
            for (Account account : accounts)
                accountOptions[i++] = account.name;

            new AlertDialog.Builder(getActivity())
                    .setIconAttribute(R.attr.theme_ic_action_calendar)
                    .setTitle(R.string.tab_univ_schedule_select_google_accounts)
                    .setSingleChoiceItems(accountOptions, 0, (dialog, which) -> {
                        mAccount = accounts[which].name;
                    })
                    .setPositiveButton(android.R.string.ok, (dialog2, which1) -> {
                        if (TextUtils.isEmpty(mAccount)) {
                            AppUtil.showToast(getActivity(), R.string.tab_univ_schedule_google_account_selection_not_exist);
                        } else {
                            addUnivScheduleToCalender();
                        }
                    })
                    .setNegativeButton(android.R.string.no, (dialog1, which2) -> mAccount = null)
                    .show();
        }
    }

    @SuppressWarnings("ResourceType")
    private void addUnivScheduleToCalender() {
        mProgressDialog.show();

        AppRequests.UnivSchedules.addUnivScheduleToCalender(getActivity(), mAccount, mSelectedItem, mList.indexOf(mSelectedItem))
                .delayed()
                .result(result -> {
                    if (result != null)
                        AppUtil.showToast(getActivity(), R.string.tab_univ_schedule_add_to_calendar_success, isMenuVisible());
                    else
                        AppUtil.showToast(getActivity(), R.string.tab_univ_schedule_add_to_calendar_fail, isMenuVisible());
                })
                .error(e -> AppUtil.showErrorToast(getActivity(), e, isMenuVisible())
                )
                .atLast(() -> {
                    mSelectedItem = null;
                    mProgressDialog.dismiss();
                })
                .execute();
    }
    */

    private void execute(boolean force) {
        appTask(AppRequests.UnivSchedules.request(force))
                .result(result -> {
                    mList.clear();
                    mList.addAll(result);
                    mAdapter.notifyDataSetChanged();

                    setSubtitleWhenVisible(mSubTitle = mDateFormat.format(mList.get(0).getDate(true).getTime()));
                })
                .error(t -> super.simpleErrorRespond(t))
                .build()
                .execute();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.tab_restaurant, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                execute(true);
                return true;

            default:
                return false;
        }
    }


    @Nullable
    @Override
    protected CharSequence getSubtitle() {
        return mSubTitle;
    }

    @NonNull
    @Override
    public String getScreenNameForTracker() {
        return "UnivScheduleFragment";
    }

}
