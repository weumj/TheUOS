package com.uoscs09.theuos2.tab.schedule;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.javacan.asyncexcute.AsyncCallback;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsArrayAdapter;
import com.uoscs09.theuos2.base.AbsProgressFragment;
import com.uoscs09.theuos2.common.AsyncLoader;
import com.uoscs09.theuos2.common.PieProgressDrawable;
import com.uoscs09.theuos2.http.HttpRequest;
import com.uoscs09.theuos2.parse.ParseUnivSchedule;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.OApiUtil;

import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import se.emilsjolander.stickylistheaders.ExpandableStickyListHeadersListView;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;


public class UnivScheduleFragment extends AbsProgressFragment<ArrayList<UnivScheduleItem>> {
    private static final String URL = OApiUtil.URL_API_MAIN_DB + '?' + OApiUtil.API_KEY + '=' + OApiUtil.UOS_API_KEY;

    private final ParseUnivSchedule mParser = new ParseUnivSchedule();

    private ArrayList<UnivScheduleItem> mList = new ArrayList<>();

    Adapter mAdapter;

    MaterialDialog mItemSelectDialog;

    UnivScheduleItem mSelectedItem;

    Dialog mProgressDialog;

    private String mSubTitle;

    private SimpleDateFormat mDateFormat = new SimpleDateFormat("MMMM", Locale.getDefault());

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("list", mList);
        outState.putString("subTitle", mSubTitle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(savedInstanceState != null){
            mList.clear();

            ArrayList<UnivScheduleItem> list = savedInstanceState.getParcelableArrayList("list");
            mList.addAll(list);

            mSubTitle = savedInstanceState.getString("subTitle");
            setSubtitleWhenVisible(mSubTitle);
        }

        View view = inflater.inflate(R.layout.tab_univ_schedule, container, false);

        mAdapter = new Adapter(getActivity(), mList);

        final ExpandableStickyListHeadersListView listView = (ExpandableStickyListHeadersListView) view.findViewById(R.id.list);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectedItem = mList.get(position);

                mItemSelectDialog.setContent(String.format(getString(R.string.tab_univ_schedule_add_schedule_to_calender), mSelectedItem.content));
                mItemSelectDialog.show();
            }
        });

        listView.setAdapter(mAdapter);
        listView.setOnHeaderClickListener(new StickyListHeadersListView.OnHeaderClickListener() {
            @Override
            public void onHeaderClick(StickyListHeadersListView l, View header, int itemPosition, long headerId, boolean currentlySticky) {
                if (listView.isHeaderCollapsed(headerId)) {
                    listView.expand(headerId);
                } else {
                    listView.collapse(headerId);
                }
            }
        });

        registerProgressView(view.findViewById(R.id.progress_layout));

        mItemSelectDialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.tab_univ_schedule_add_to_calendar)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.no)
                .content("")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);

                        sendClickEvent("add schedule to calender");

                        addUnivScheduleToCalender();
                    }
                })
                .build();

        mProgressDialog = AppUtil.getProgressDialog(getActivity(), false, null);


        if (mList.isEmpty())
            execute();

        return view;
    }

    private final String[] EVENT_PROJECTION = {
            CalendarContract.Calendars._ID
    };

    private String mAccount;

    void addUnivScheduleToCalender() {
        mProgressDialog.show();

        AsyncLoader.excute(mCalendarQueryCallable, mCalendarQueryCallback);
    }


    private final Callable<Uri> mCalendarQueryCallable = new Callable<Uri>() {

        private final String SELECTION = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
                + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + CalendarContract.Calendars.OWNER_ACCOUNT + " = ?))";
        private String[] selectionArgs;


        @Override
        public Uri call() throws Exception {

            if (mAccount == null) {
                AccountManager accountManager = AccountManager.get(getActivity());
                Account[] accounts = accountManager.getAccountsByType("com.google");

                if (accounts.length < 1) {
                    throw new Exception(getString(R.string.tab_univ_schedule_google_account_not_exist));
                }

                mAccount = accounts[0].name;

                selectionArgs = new String[]{mAccount, "com.google", mAccount};

            }

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
                cv.put(CalendarContract.Events.EVENT_COLOR, getResources().getColor(AppUtil.getColor(mList.indexOf(mSelectedItem))));

            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, cv);

            c.close();

            return uri;
        }
    };

    private final AsyncCallback<Uri> mCalendarQueryCallback = new AsyncCallback.Base<Uri>() {
        @Override
        public void onResult(Uri result) {
            if (result != null)
                AppUtil.showToast(getActivity(), R.string.tab_univ_schedule_add_to_calendar_success, isMenuVisible());
            else
                AppUtil.showToast(getActivity(), R.string.tab_univ_schedule_add_to_calendar_fail, isMenuVisible());
        }

        @Override
        public void exceptionOccured(Exception e) {
            e.printStackTrace();

            AppUtil.showErrorToast(getActivity(), e, isMenuVisible());
        }

        @Override
        public void onPostExcute() {
            mProgressDialog.dismiss();
        }
    };

    @Override
    public ArrayList<UnivScheduleItem> call() throws Exception {
        HttpURLConnection connection = HttpRequest.getConnection(URL);

        try {
            return mParser.parse(connection.getInputStream());

        } finally {
            connection.disconnect();
        }

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

    @Override
    protected void onTransactResult(ArrayList<UnivScheduleItem> result) {

        mList.clear();
        mList.addAll(result);
        mAdapter.notifyDataSetChanged();

        setSubtitleWhenVisible(mSubTitle = mDateFormat.format(mList.get(0).getDate(true).getTime()));
    }

    @Nullable
    @Override
    protected CharSequence getSubtitle() {
        return mSubTitle;
    }

    @NonNull
    @Override
    protected String getFragmentNameForTracker() {
        return "UnivScheduleFragment";
    }

    private static class Adapter extends AbsArrayAdapter<UnivScheduleItem, ViewHolder> implements StickyListHeadersAdapter {

        public Adapter(Context context, List<UnivScheduleItem> list) {
            super(context, R.layout.list_layout_univ_schedule, list);
        }

        @Override
        public void onBindViewHolder(int position, UnivScheduleFragment.ViewHolder holder) {
            UnivScheduleItem item = getItem(position);

            holder.item = item;

            holder.textView1.setText(item.content);
            holder.textView2.setText(item.sch_date);


            holder.drawable.setColor(getContext().getResources().getColor(AppUtil.getColor(position)));
            //holder.drawable.setCentorColor(getContext().getResources().getColor(AppUtil.getColor(position)));

            holder.textView1.invalidateDrawable(holder.drawable);

        }

        @Override
        public UnivScheduleFragment.ViewHolder getViewHolder(View convertView) {
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
                holder.textView.setText("" + date.day);
                holder.textView2.setText(dateFormat.format(new Date(c.getTimeInMillis())));
            }

            return convertView;
        }

        private SimpleDateFormat dateFormat = new SimpleDateFormat("E", Locale.getDefault());

        @Override
        public long getHeaderId(int position) {
            UnivScheduleItem.ScheduleDate date = getItem(position).dateStart;
            return date.month * 100 + date.day;
        }

    }

    static class ViewHolder extends AbsArrayAdapter.ViewHolder {
        final TextView textView1, textView2;
        final CardView cardView;
        UnivScheduleItem item;
        PieProgressDrawable drawable = new PieProgressDrawable();

        @SuppressWarnings("deprecation")
        public ViewHolder(View view) {
            super(view);

            cardView = (CardView) view.findViewById(R.id.card_view);


            textView1 = (TextView) view.findViewById(android.R.id.text1);
            textView2 = (TextView) view.findViewById(android.R.id.text2);

            drawable.setLevel(100);
            drawable.setBorderWidth(-1f, view.getResources().getDisplayMetrics());
            int size = itemView.getResources().getDimensionPixelSize(R.dimen.univ_schedule_list_drawable_size);
            drawable.setBounds(0, 0, size, size);

            textView1.setCompoundDrawables(drawable, null, null, null);
        }

    }

    static class HeaderViewHolder extends AbsArrayAdapter.SimpleViewHolder {
        final TextView textView2;

        public HeaderViewHolder(View view) {
            super(view);

            textView2 = (TextView) view.findViewById(android.R.id.text2);
        }
    }

}
