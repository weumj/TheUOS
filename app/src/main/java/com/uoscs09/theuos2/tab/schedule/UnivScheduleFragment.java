package com.uoscs09.theuos2.tab.schedule;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.async.AsyncUtil;
import com.uoscs09.theuos2.base.AbsArrayAdapter;
import com.uoscs09.theuos2.base.AbsProgressFragment;
import com.uoscs09.theuos2.common.PieProgressDrawable;
import com.uoscs09.theuos2.util.AppResources;
import com.uoscs09.theuos2.util.AppUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import se.emilsjolander.stickylistheaders.ExpandableStickyListHeadersListView;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;


public class UnivScheduleFragment extends AbsProgressFragment<ArrayList<UnivScheduleItem>> {

    private static final String SELECTION = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
            + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
            + CalendarContract.Calendars.OWNER_ACCOUNT + " = ?))";
    private static final String[] EVENT_PROJECTION = {
            CalendarContract.Calendars._ID
    };
    private static boolean permissionChecked = false;
    private static boolean permissionResult = false;

    @Bind(R.id.list)
    ExpandableStickyListHeadersListView mListView;
    private AlertDialog mItemSelectDialog;
    private Dialog mProgressDialog;
    private Adapter mAdapter;

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
    protected int getLayout() {
        return R.layout.tab_univ_schedule;
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

        mAdapter = new Adapter(getActivity(), mList);

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

                    addUnivScheduleToCalender();
                })
                .setNegativeButton(android.R.string.no, null)
                .setMessage("")
                .create();

        mProgressDialog = AppUtil.getProgressDialog(getActivity(), false, null);


        if (mList.isEmpty())
            execute();
    }

    private void getAccount() throws Exception {
        if (mAccount == null) {
            AccountManager accountManager = AccountManager.get(getActivity());
            Account[] accounts = accountManager.getAccountsByType("com.google");

            if (accounts.length < 1) {
                throw new Exception(getString(R.string.tab_univ_schedule_google_account_not_exist));
            }

            mAccount = accounts[0].name;

            selectionArgs = new String[]{mAccount, "com.google", mAccount};
        }
    }

    private boolean checkPermission() {
        permissionChecked = true;
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checkPermissionV22();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean checkPermissionV22() {
        return getActivity().checkSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
                && getActivity().checkSelfPermission(Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED;
    }

    private static final int PERMISSION_REQUEST_CALENDAR = 4822;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_REQUEST_CALENDAR:
                for (int result : grantResults) {
                    if (result == PackageManager.PERMISSION_DENIED) {
                        AppUtil.showToast(getActivity(), R.string.tab_univ_schedule_permission_denied);
                        return;
                    }
                }
                break;

            default:
                break;
        }
    }

    private void addUnivScheduleToCalender() {
        // permission 확인한 적이 없고, permission 요청 결과가 false 였고, permission 확인 결과가 false 인 경우
        if (!permissionChecked && !permissionResult && !(permissionResult = checkPermission())) {
            requestPermissions(new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR}, PERMISSION_REQUEST_CALENDAR);
            return;
        }

        mProgressDialog.show();

        AsyncUtil.newRequest(
                () -> {
                    getAccount();

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
                        //noinspection deprecation
                        cv.put(CalendarContract.Events.EVENT_COLOR, getResources().getColor(AppUtil.getColor(mList.indexOf(mSelectedItem))));

                    Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, cv);
                    c.close();

                    return uri;
                })
                .getAsync(
                        result -> {
                            mProgressDialog.dismiss();
                            if (result != null)
                                AppUtil.showToast(getActivity(), R.string.tab_univ_schedule_add_to_calendar_success, isMenuVisible());
                            else
                                AppUtil.showToast(getActivity(), R.string.tab_univ_schedule_add_to_calendar_fail, isMenuVisible());
                        },
                        e -> {
                            mProgressDialog.dismiss();
                            e.printStackTrace();

                            AppUtil.showErrorToast(getActivity(), e, isMenuVisible());
                        }
                );

    }

    /*
    private static void writeFile(Context context, ArrayList<UnivScheduleItem> object) throws IOException {
        IOUtil.writeObjectToFile(context, FILE_NAME, object);
    }

    private static ArrayList<UnivScheduleItem> readFile(Context context) throws IOException, ClassNotFoundException {
        return IOUtil.readFromFile(context, FILE_NAME);
    }
    */

    private void execute() {
        execute(true,
                AppResources.UnivSchedules.request(getActivity()),
                result -> {
                    mList.clear();
                    mList.addAll(result);
                    mAdapter.notifyDataSetChanged();

                    setSubtitleWhenVisible(mSubTitle = mDateFormat.format(mList.get(0).getDate(true).getTime()));
                },
                Throwable::printStackTrace,
                true
        );
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
                execute();
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

    private static class Adapter extends AbsArrayAdapter<UnivScheduleItem, ViewHolder> implements StickyListHeadersAdapter {

        public Adapter(Context context, List<UnivScheduleItem> list) {
            super(context, R.layout.list_layout_univ_schedule, list);
        }

        @Override
        public void onBindViewHolder(int position, UnivScheduleFragment.ViewHolder holder) {
            UnivScheduleItem item = getItem(position);

            //holder.item = item;

            holder.textView1.setText(item.content);
            holder.textView2.setText(item.sch_date);


            //noinspection deprecation
            holder.drawable.setColor(getContext().getResources().getColor(AppUtil.getColor(position)));
            //holder.drawable.setCentorColor(getContext().getResources().getColor(AppUtil.getColor(position)));

            holder.textView1.invalidateDrawable(holder.drawable);

        }

        @Override
        public UnivScheduleFragment.ViewHolder onCreateViewHolder(View convertView, int viewType) {
            return new UnivScheduleFragment.ViewHolder(convertView);
        }

        @Override
        public View getHeaderView(int position, View convertView, ViewGroup viewGroup) {
            HeaderViewHolder holder;
            if (convertView == null) {
                holder = new HeaderViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.list_layout_univ_schedule_header, viewGroup, false));
                convertView = holder.itemView;
                convertView.setTag(holder);

            } else {
                holder = (HeaderViewHolder) convertView.getTag();
            }

            UnivScheduleItem.ScheduleDate date = getItem(position).dateStart;
            Calendar c = getItem(position).getDate(true);

            if (c == null) {
                holder.textView.setText("");
                holder.textView2.setText("");
            } else {
                holder.textView.setText(String.valueOf(date.day));
                holder.textView2.setText(dateFormat.format(new Date(c.getTimeInMillis())));
            }

            return convertView;
        }

        private final SimpleDateFormat dateFormat = new SimpleDateFormat("E", Locale.getDefault());

        @Override
        public long getHeaderId(int position) {
            UnivScheduleItem.ScheduleDate date = getItem(position).dateStart;
            return date.month * 100 + date.day;
        }

    }

    static class ViewHolder extends AbsArrayAdapter.ViewHolder {
        @Bind(android.R.id.text1)
        TextView textView1;
        @Bind(android.R.id.text2)
        TextView textView2;
        /*
        @Bind(R.id.card_view)
        CardView cardView;
        */
        //UnivScheduleItem item;
        final PieProgressDrawable drawable = new PieProgressDrawable();

        @SuppressWarnings("deprecation")
        public ViewHolder(View view) {
            super(view);

            drawable.setLevel(100);
            drawable.setBorderWidth(-1f, view.getResources().getDisplayMetrics());
            int size = itemView.getResources().getDimensionPixelSize(R.dimen.univ_schedule_list_drawable_size);
            drawable.setBounds(0, 0, size, size);

            textView1.setCompoundDrawables(drawable, null, null, null);
        }

    }

    static class HeaderViewHolder extends AbsArrayAdapter.SimpleViewHolder {
        @Bind(android.R.id.text2)
        TextView textView2;

        public HeaderViewHolder(View view) {
            super(view);
        }

    }

}
