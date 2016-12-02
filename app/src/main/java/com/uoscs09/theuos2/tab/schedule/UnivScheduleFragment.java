package com.uoscs09.theuos2.tab.schedule;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsProgressFragment;
import com.uoscs09.theuos2.util.AppRequests;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.ResourceUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import mj.android.utils.task.Tasks;
import se.emilsjolander.stickylistheaders.ExpandableStickyListHeadersListView;


public class UnivScheduleFragment extends AbsProgressFragment<List<UnivScheduleItem>> {

    @Override
    protected int layoutRes() {
        return R.layout.tab_univ_schedule;
    }

    private static final int PERMISSION_REQUEST_CALENDAR = 12;

    private static final String SELECTION = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
            + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
            + CalendarContract.Calendars.OWNER_ACCOUNT + " = ?))";
    private static final String[] EVENT_PROJECTION = {
            CalendarContract.Calendars._ID
    };

    @BindView(R.id.list)
    ExpandableStickyListHeadersListView mListView;
    private AlertDialog mItemSelectDialog;
    private Dialog mProgressDialog;
    private UnivScheduleAdapter mAdapter;

    private final ArrayList<UnivScheduleItem> mList = new ArrayList<>();
    private UnivScheduleItem mSelectedItem;
    private String mSubTitle;
    private final SimpleDateFormat mDateFormat = new SimpleDateFormat("MMMM", Locale.getDefault());

    private String[] selectionArgs;
    private String mAccount;


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("list", mList);
        outState.putString("subTitle", mSubTitle);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            mList.clear();

            ArrayList<UnivScheduleItem> list = savedInstanceState.getParcelableArrayList("list");
            if (list != null)
                mList.addAll(list);

            mSubTitle = savedInstanceState.getString("subTitle");
            setSubtitleWhenVisible(mSubTitle);
        }

        mAdapter = new UnivScheduleAdapter(getActivity(), mList);

        mListView.setNestedScrollingEnabled(true);

        mListView.setOnItemClickListener((parent, view1, position, id) -> {
            mSelectedItem = mList.get(position);

            mItemSelectDialog.setMessage(String.format(getString(R.string.tab_univ_schedule_add_schedule_to_calender), mSelectedItem.content));
            mItemSelectDialog.show();
        });

        mListView.setAdapter(mAdapter);
        mListView.setOnHeaderClickListener((l, header, itemPosition, headerId, currentlySticky) -> {
            if (mListView.isHeaderCollapsed(headerId)) {
                mListView.expand(headerId);
            } else {
                mListView.collapse(headerId);
            }
        });

        registerProgressView(view.findViewById(R.id.progress_layout));

        mItemSelectDialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.tab_univ_schedule_add_to_calendar)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    sendClickEvent("add schedule to calender");

                    checkCalenderConditionAndAddSchedule();
                })
                .setNegativeButton(android.R.string.no, null)
                .setMessage("")
                .create();

        mProgressDialog = AppUtil.getProgressDialog(getActivity(), false, getString(R.string.progress_ongoing), null);


        if (mList.isEmpty())
            execute(false);
    }

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

        if (mAccount != null && selectionArgs != null) {
            addUnivScheduleToCalender();
            return;
        }

        //noinspection MissingPermission
        Account[] accounts = deviceAccounts();
        if (accounts.length < 1) {
            AppUtil.showToast(getActivity(), R.string.tab_univ_schedule_google_account_not_exist);
        } else if (accounts.length == 1) {
            mAccount = accounts[0].name;
            selectionArgs = new String[]{mAccount, "com.google", mAccount};
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
                            selectionArgs = new String[]{mAccount, "com.google", mAccount};
                            addUnivScheduleToCalender();
                        }
                    })
                    .setNegativeButton(android.R.string.no, (dialog1, which2) -> {
                        selectionArgs = null;
                        mAccount = null;
                    })
                    .show();
        }
    }

    @SuppressWarnings("ResourceType")
    private void addUnivScheduleToCalender() {
        mProgressDialog.show();

        Tasks.newTask(() -> {
            ContentResolver cr = getActivity().getContentResolver();

            Cursor c = cr.query(CalendarContract.Calendars.CONTENT_URI, EVENT_PROJECTION, SELECTION, selectionArgs, null);

            if (c == null) {
                throw new Exception(getString(R.string.tab_univ_schedule_calendar_not_exist));
            } else if (!c.moveToFirst()) {
                c.close();
                throw new Exception(getString(R.string.tab_univ_schedule_calendar_not_exist));
            }

            long calendarId = c.getLong(0);
            ContentValues cv = mSelectedItem.toContentValues(calendarId);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
                cv.put(CalendarContract.Events.EVENT_COLOR, ResourceUtil.getOrderedColor(getActivity(), mList.indexOf(mSelectedItem)));

            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, cv);
            c.close();
            mSelectedItem = null;

            return uri;
        }).getAsync(
                result -> {
                    mProgressDialog.dismiss();
                    if (result != null)
                        AppUtil.showToast(getActivity(), R.string.tab_univ_schedule_add_to_calendar_success, isMenuVisible());
                    else
                        AppUtil.showToast(getActivity(), R.string.tab_univ_schedule_add_to_calendar_fail, isMenuVisible());
                },
                e -> {
                    mProgressDialog.dismiss();

                    AppUtil.showErrorToast(getActivity(), e, isMenuVisible());
                }
        );

    }

    private void execute(boolean force) {
        task(AppRequests.UnivSchedules.request(force))
                .result(result -> {
                    mList.clear();
                    mList.addAll(result);
                    mAdapter.notifyDataSetChanged();

                    setSubtitleWhenVisible(mSubTitle = mDateFormat.format(mList.get(0).getDate(true).getTime()));
                })
                .error(t -> super.simpleErrorRespond(t))
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
