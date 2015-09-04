package com.uoscs09.theuos2.tab.schedule;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.async.AbstractRequest;
import com.uoscs09.theuos2.async.AsyncUtil;
import com.uoscs09.theuos2.async.Request;
import com.uoscs09.theuos2.base.AbsArrayAdapter;
import com.uoscs09.theuos2.base.AbsProgressFragment;
import com.uoscs09.theuos2.common.PieProgressDrawable;
import com.uoscs09.theuos2.http.HttpRequest;
import com.uoscs09.theuos2.parse.XmlParserWrapper;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.IOUtil;
import com.uoscs09.theuos2.util.OApiUtil;
import com.uoscs09.theuos2.util.PrefUtil;

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


public class UnivScheduleFragment extends AbsProgressFragment<ArrayList<UnivScheduleItem>>
        implements Request.ResultListener<ArrayList<UnivScheduleItem>>, Request.ErrorListener {
    private static final String URL = OApiUtil.URL_API_MAIN_DB + '?' + OApiUtil.API_KEY + '=' + OApiUtil.UOS_API_KEY;
    private static final String FILE_NAME = "file_univ_schedule";
    private static final XmlParserWrapper<ArrayList<UnivScheduleItem>> UNIV_SCHEDULE_PARSER = OApiUtil.getUnivScheduleParser();

    private ExpandableStickyListHeadersListView mListView;
    private AlertDialog mItemSelectDialog;
    private Dialog mProgressDialog;
    private Adapter mAdapter;

    private final ArrayList<UnivScheduleItem> mList = new ArrayList<>();
    private UnivScheduleItem mSelectedItem;
    private String mSubTitle;
    private final SimpleDateFormat mDateFormat = new SimpleDateFormat("MMMM", Locale.getDefault());

    private String[] selectionArgs;
    private String mAccount;

    private static final String SELECTION = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
            + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
            + CalendarContract.Calendars.OWNER_ACCOUNT + " = ?))";
    private final String[] EVENT_PROJECTION = {
            CalendarContract.Calendars._ID
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("list", mList);
        outState.putString("subTitle", mSubTitle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mList.clear();

            ArrayList<UnivScheduleItem> list = savedInstanceState.getParcelableArrayList("list");
            if (list != null)
                mList.addAll(list);

            mSubTitle = savedInstanceState.getString("subTitle");
            setSubtitleWhenVisible(mSubTitle);
        }

        View view = inflater.inflate(R.layout.tab_univ_schedule, container, false);

        mAdapter = new Adapter(getActivity(), mList);

        mListView = (ExpandableStickyListHeadersListView) view.findViewById(R.id.list);
        mListView.setNestedScrollingEnabled(true);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectedItem = mList.get(position);

                mItemSelectDialog.setMessage(String.format(getString(R.string.tab_univ_schedule_add_schedule_to_calender), mSelectedItem.content));
                mItemSelectDialog.show();
            }
        });

        mListView.setAdapter(mAdapter);
        mListView.setOnHeaderClickListener(new StickyListHeadersListView.OnHeaderClickListener() {
            @Override
            public void onHeaderClick(StickyListHeadersListView l, View header, int itemPosition, long headerId, boolean currentlySticky) {
                if (mListView.isHeaderCollapsed(headerId)) {
                    mListView.expand(headerId);
                } else {
                    mListView.collapse(headerId);
                }
            }
        });

        registerProgressView(view.findViewById(R.id.progress_layout));

        mItemSelectDialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.tab_univ_schedule_add_to_calendar)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendClickEvent("add schedule to calender");

                        addUnivScheduleToCalender();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setMessage("")
                .create();

        mProgressDialog = AppUtil.getProgressDialog(getActivity(), false, null);


        if (mList.isEmpty())
            execute();

        return view;
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

    private void addUnivScheduleToCalender() {
        mProgressDialog.show();

        AsyncUtil.newRequest(
                new Callable<Uri>() {
                    @Override
                    public Uri call() throws Exception {
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
                            cv.put(CalendarContract.Events.EVENT_COLOR, getResources().getColor(AppUtil.getColor(mList.indexOf(mSelectedItem))));

                        Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, cv);
                        c.close();

                        return uri;
                    }
                })
                .getAsync(
                        new Request.ResultListener<Uri>() {
                            @Override
                            public void onResult(Uri result) {
                                mProgressDialog.dismiss();
                                if (result != null)
                                    AppUtil.showToast(getActivity(), R.string.tab_univ_schedule_add_to_calendar_success, isMenuVisible());
                                else
                                    AppUtil.showToast(getActivity(), R.string.tab_univ_schedule_add_to_calendar_fail, isMenuVisible());
                            }
                        },
                        new Request.ErrorListener() {
                            @Override
                            public void onError(Exception e) {
                                mProgressDialog.dismiss();
                                e.printStackTrace();

                                AppUtil.showErrorToast(getActivity(), e, isMenuVisible());
                            }
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

    private static ArrayList<UnivScheduleItem> readFromInternet(Context context) throws Exception {
        ArrayList<UnivScheduleItem> result = HttpRequest.Builder.newConnectionRequestBuilder(URL)
                .build()
                .checkNetworkState(context)
                .wrap(UNIV_SCHEDULE_PARSER)
                .wrap(IOUtil.<ArrayList<UnivScheduleItem>>newFileWriteProcessor(context, FILE_NAME))
                .get();

        PrefUtil pref = PrefUtil.getInstance(context);
        pref.put(PrefUtil.KEY_SCHEDULE_FETCH_MONTH, result.get(0).getDate(true).get(Calendar.MONTH));

        return result;
    }

    private void execute() {
        execute(true, mRequest, this, this, true);
    }

    private Request<ArrayList<UnivScheduleItem>> mRequest = new AbstractRequest<ArrayList<UnivScheduleItem>>() {
        @Override
        public ArrayList<UnivScheduleItem> get() throws Exception {
            Context context = getActivity();
            PrefUtil pref = PrefUtil.getInstance(context);

            // 이번 달의 일정이 기록된 파일이 있으면, 인터넷에서 가져오지 않고 그 파일을 읽음
            if (pref.get(PrefUtil.KEY_SCHEDULE_FETCH_MONTH, -1) == Calendar.getInstance().get(Calendar.MONTH)) {
                ArrayList<UnivScheduleItem> result = new IOUtil.Builder<ArrayList<UnivScheduleItem>>(FILE_NAME)
                        .setContext(context)
                        .build()
                        .get();

                if (result != null)
                    return result;

            }

            return readFromInternet(context);
        }
    };

    @Override
    public void onResult(ArrayList<UnivScheduleItem> result) {
        mList.clear();
        mList.addAll(result);
        mAdapter.notifyDataSetChanged();

        setSubtitleWhenVisible(mSubTitle = mDateFormat.format(mList.get(0).getDate(true).getTime()));
    }

    @Override
    public void onError(Exception e) {
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
                holder.textView.setText("" + date.day);
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
        final TextView textView1, textView2;
        final CardView cardView;
        //UnivScheduleItem item;
        final PieProgressDrawable drawable = new PieProgressDrawable();

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
