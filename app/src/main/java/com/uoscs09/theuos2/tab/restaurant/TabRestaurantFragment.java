package com.uoscs09.theuos2.tab.restaurant;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.annotation.AsyncData;
import com.uoscs09.theuos2.annotation.ReleaseWhenDestroy;
import com.uoscs09.theuos2.async.AsyncFragmentJob;
import com.uoscs09.theuos2.base.AbsAsyncFragment;
import com.uoscs09.theuos2.http.HttpRequest;
import com.uoscs09.theuos2.parse.ParserRest;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.IOUtil;
import com.uoscs09.theuos2.util.OApiUtil;
import com.uoscs09.theuos2.util.PrefUtil;
import com.uoscs09.theuos2.util.StringUtil;

import java.util.ArrayList;
import java.util.Calendar;

public class TabRestaurantFragment extends AbsAsyncFragment<ArrayList<RestItem>> {
    @ReleaseWhenDestroy
    private NestedScrollView mScrollView;
    @ReleaseWhenDestroy
    private TextView mSemesterTimeView, mVacationTimeView,
            mContentBreakfastView, mContentLunchView, mContentSupperView;
    @ReleaseWhenDestroy
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private int mCurrentSelection;
    @AsyncData
    private ArrayList<RestItem> mRestList;

    private static final ParserRest PARSER = new ParserRest();

    //private String mCurrentRestName;
    private final ArrayList<Tab> mTabList = new ArrayList<>();

    private static final String BUTTON = "button";
    private static final String REST = "rest_list";
    private static final String[] TIME_SEMESTER = {
            "학기중\n조식 : 08:00~10:00\n중식 : 11:00~14:00\n15:00~17:00",
            "학기중\n중식 : 11:30~14:00\n석식 : 15:00~19:00\n토요일 : 휴무",
            "학기중\n중식 : 11:30~13:30\n석식 : 17:00~18:30\n토요일 : 휴무",
            StringUtil.NULL, StringUtil.NULL};
    private static final String[] TIME_VACATION = {
            "방학중\n조식 : 09:00~10:00\n	08:30~10:00\n(계절학기 기간)\n중식 : 11:00~14:00\n15:00~17:00\n석식 : 17:00~18:30\n토요일 : 휴무",
            "방학중\n중식 : 11:30~14:00\n석식 : 16:00~18:30\n토요일 : 휴무",
            "방학중 : 휴관\n\n\n", StringUtil.NULL, StringUtil.NULL};

    private static final int[] REST_TAB_MENU_STRING_ID = {R.string.tab_rest_students_hall, R.string.tab_rest_anekan, R.string.tab_rest_natural, R.string.tab_rest_main_8th, R.string.tab_rest_living};
    private static final String[] REST_TAB_MENU_STRING_KOR = {"학생회관 1층", "양식당 (아느칸)", "자연과학관", "본관 8층", "생활관"};

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(BUTTON, mCurrentSelection);
        outState.putParcelableArrayList(REST, mRestList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mCurrentSelection = savedInstanceState.getInt(BUTTON);
            mRestList = savedInstanceState.getParcelableArrayList(REST);
        } else {

            int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            if (today == Calendar.SUNDAY || today == Calendar.SATURDAY)
                mCurrentSelection = 4;
            else
                mCurrentSelection = 0;

            mRestList = new ArrayList<>();
        }

        super.onCreate(savedInstanceState);

        LinearLayout mTabParent = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.view_tab_rest_toolbar_menu, getToolbarParent(), false);

        for (int stringId : REST_TAB_MENU_STRING_ID) {
            final Tab tab = new Tab(mTabParent);
            tab.setText(stringId);
            tab.ripple.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTabList.get(mCurrentSelection).setSelected(false);
                    mCurrentSelection = mTabList.indexOf(tab);
                    performTabClick(mCurrentSelection);
                }
            });
            mTabParent.addView(tab.tabView);
            mTabList.add(tab);
        }

        registerTabParentView(mTabParent);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.tab_restaurant, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.tab_rest_swipe_layout);
        mSwipeRefreshLayout.setColorSchemeResources(
                AppUtil.getAttrValue(getActivity(), R.attr.color_actionbar_title),
                AppUtil.getAttrValue(getActivity(), R.attr.colorAccent)
        );
        mSwipeRefreshLayout.setProgressBackgroundColorSchemeResource(AppUtil.getAttrValue(getActivity(), R.attr.colorPrimary));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                sendTrackerEvent("swipe", "SwipeRefreshView");
                execute();
            }
        });

        mScrollView = (NestedScrollView) mSwipeRefreshLayout.findViewById(R.id.tab_rest_scroll);
        registerNestedScrollingChild(mScrollView);

        mSemesterTimeView = (TextView) mScrollView.findViewById(R.id.tab_rest_text_semester);
        mVacationTimeView = (TextView) mScrollView.findViewById(R.id.tab_rest_text_vacation);
        mContentBreakfastView = (TextView) mScrollView.findViewById(R.id.tab_rest_text_breakfast);
        mContentLunchView = (TextView) mScrollView.findViewById(R.id.tab_rest_text_lunch);
        mContentSupperView = (TextView) mScrollView.findViewById(R.id.tab_rest_text_supper);

        FloatingActionButton actionButton = (FloatingActionButton) rootView.findViewById(R.id.action_btn);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendClickEvent("actionButton");
                mSwipeRefreshLayout.setRefreshing(true);
                execute();
            }
        });

        rootView.findViewById(R.id.tab_rest_show_week).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWeekDialog();
            }
        });

        if (mRestList.isEmpty())
            execute();
        else
            performTabClick(mCurrentSelection);

        super.onResume();

        return rootView;
    }


    private void performTabClick(int position) {
        setSemesterAndVacationText(position);
        setBody(REST_TAB_MENU_STRING_KOR[position]);
        mScrollView.scrollTo(0, 0);
        mTabList.get(mCurrentSelection).setSelected(true);
    }

    private void setSemesterAndVacationText(int i) {
        if (mSemesterTimeView != null)
            mSemesterTimeView.setText(TIME_SEMESTER[i]);
        if (mVacationTimeView != null)
            mVacationTimeView.setText(TIME_VACATION[i]);
    }

    void setBody(final String name) {

        sendClickEvent(name);

        //mCurrentRestName = name;
        //setSubtitleWhenVisible(name);
        RestItem item = null;
        if (mRestList != null) {
            int size = mRestList.size();
            for (int i = 0; i < size; i++) {
                item = mRestList.get(i);
                if (name.contains(item.title)) {
                    mContentBreakfastView.setText(item.breakfast);
                    mContentLunchView.setText(item.lunch);
                    mContentSupperView.setText(item.supper);
                    break;
                }
                item = null;
            }
        }
        if (item == null) {
            mContentBreakfastView.setText(R.string.tab_rest_no_info);
            mContentLunchView.setText(R.string.tab_rest_no_info);
            mContentSupperView.setText(R.string.tab_rest_no_info);
        }
    }

    private final AsyncFragmentJob.Base<ArrayList<RestItem>> JOB = new AsyncFragmentJob.Base<ArrayList<RestItem>>(){
        @Override
        public ArrayList<RestItem> call() throws Exception {
            Context context = getActivity();
            if (OApiUtil.getDateTime() - PrefUtil.getInstance(context).get(PrefUtil.KEY_REST_DATE_TIME, 0) < 3) {
                try {
                    ArrayList<RestItem> list = IOUtil.readFromFile(context, IOUtil.FILE_REST);
                    if (list != null)
                        return list;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // web 에서 읽어온지 오래되었거나, 파일이 존재하지 않은경우 web 에서 읽어옴
            return getRestListFromWeb(context);
        }

        @Override
        public void onResult(ArrayList<RestItem> result) {
            mRestList.clear();
            mRestList.addAll(result);

            mSwipeRefreshLayout.setRefreshing(false);

            performTabClick(mCurrentSelection);
        }

    };

    private void execute(){
        super.execute(JOB);
    }


    /**
     * web에서 식단표을 읽어온다.
     */
    public static ArrayList<RestItem> getRestListFromWeb(Context context) throws Exception {
        String body = HttpRequest.getBody("http://m.uos.ac.kr/mkor/food/list.do");
        ArrayList<RestItem> list = PARSER.parse(body);

        IOUtil.writeObjectToFile(context, IOUtil.FILE_REST, list);
        PrefUtil.getInstance(context).put(PrefUtil.KEY_REST_DATE_TIME, OApiUtil.getDate());
        return list;
    }

    void showWeekDialog() {
        WeekInformationDialogFragment dialogFragment = new WeekInformationDialogFragment();
        dialogFragment.setSelection(REST_TAB_MENU_STRING_ID[mCurrentSelection]);

        dialogFragment.show(getFragmentManager(), "week");
    }

    @NonNull
    @Override
    protected String getFragmentNameForTracker() {
        return "TabRestaurantFragment";
    }

    /*
    @Override
    protected CharSequence getSubtitle() {
        return mCurrentRestName;
    }
*/
    private static class Tab {
        public final FrameLayout tabView;
        public final TextView mTextView;
        public final View ripple;
        private final View mStrip;
        public int id;

        public Tab(LinearLayout parent) {
            tabView = (FrameLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.view_tab_rest_tab, parent, false);
            ripple = tabView.findViewById(R.id.ripple);
            mTextView = (TextView) tabView.findViewById(R.id.tab_rest_tab_text);
            mStrip = tabView.findViewById(R.id.tab_rest_tab_strip);
        }

        public void setSelected(boolean selected) {
            mStrip.setVisibility(selected ? View.VISIBLE : View.INVISIBLE);
        }

        public void setText(int stringId) {
            mTextView.setText(stringId);
            this.id = stringId;
        }


    }

}