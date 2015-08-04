package com.uoscs09.theuos2.tab.restaurant;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.annotation.AsyncData;
import com.uoscs09.theuos2.annotation.ReleaseWhenDestroy;
import com.uoscs09.theuos2.async.AsyncFragmentJob;
import com.uoscs09.theuos2.base.AbsProgressFragment;
import com.uoscs09.theuos2.common.SerializableArrayMap;
import com.uoscs09.theuos2.http.HttpRequest;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.IOUtil;
import com.uoscs09.theuos2.util.OApiUtil;
import com.uoscs09.theuos2.util.PrefUtil;

import java.util.ArrayList;
import java.util.Calendar;

import jp.wasabeef.recyclerview.animators.SlideInDownAnimator;
import jp.wasabeef.recyclerview.animators.adapters.SlideInBottomAnimationAdapter;

public class TabRestaurantFragment extends AbsProgressFragment<SparseArray<RestItem>> {

    @ReleaseWhenDestroy
    private SwipeRefreshLayout mSwipeRefreshLayout;
    @ReleaseWhenDestroy
    private RecyclerView mRecyclerView;
    private RestItemAdapter mRestItemAdapter;
    // 리스트의 한 아이템은 식당 정보 (아침 점심 저녁) 를 나타냄
    @AsyncData
    private SparseArray<RestItem> mRestTable;

    private int mCurrentSelection;
    private boolean force = false;

    private static final ParseRest REST_PARSER = new ParseRest();

    //private String mCurrentRestName;
    private final ArrayList<Tab> mTabList = new ArrayList<>();
    private Tab mCurrentTab;

    private static final String BUTTON = "button";
    private static final String REST = "rest_list";

    private static final int[] TIME_SEMESTER = {
            R.string.tab_rest_operation_time_students_hall_in_semester,
            R.string.tab_rest_operation_time_anekan_in_semester,
            R.string.tab_rest_operation_time_nature_science_in_semester,
            R.string.tab_rest_operation_time_main_8th_in_semester,
            R.string.tab_rest_operation_time_dormitory_in_semester
    };

    private static final int[] TIME_VACATION = {
            R.string.tab_rest_operation_time_students_hall_in_vacation,
            R.string.tab_rest_operation_time_anekan_in_vacation,
            R.string.tab_rest_operation_time_nature_science_in_vacation,
            R.string.tab_rest_operation_time_main_8th_in_vacation,
            R.string.tab_rest_operation_time_dormitory_in_vacation
    };

    private static final int[] REST_TAB_MENU_STRING_ID = {R.string.tab_rest_students_hall, R.string.tab_rest_anekan, R.string.tab_rest_nature_science, R.string.tab_rest_main_8th, R.string.tab_rest_dormitory};
    private static final String[] REST_TAB_MENU_STRING_LABEL = {"학생회관 1층", "양식당 (아느칸)", "자연과학관", "본관 8층", "생활관"};

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(BUTTON, mCurrentSelection);
        outState.putSparseParcelableArray(REST, mRestTable);
        super.onSaveInstanceState(outState);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mCurrentSelection = savedInstanceState.getInt(BUTTON);
            mRestTable = savedInstanceState.getSparseParcelableArray(REST);
        } else {

            mRestTable = new SparseArray<>();

            int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            if (today == Calendar.SUNDAY || today == Calendar.SATURDAY)
                mCurrentSelection = 4;
            else
                mCurrentSelection = 0;

        }

        super.onCreate(savedInstanceState);

        LinearLayout mTabParent = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.view_tab_rest_toolbar_menu, getToolbarParent(), false);

        for (int stringId : REST_TAB_MENU_STRING_ID) {
            final Tab tab = new Tab(mTabParent);
            tab.setText(stringId);
            tab.ripple.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    performTabClick(mTabList.indexOf(tab));
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
                sendTrackerEvent("refresh", "SwipeRefreshView");
                execute(true);
            }
        });

        mRecyclerView = (RecyclerView) mSwipeRefreshLayout.findViewById(R.id.tab_rest_recycler_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setAdapter(new SlideInBottomAnimationAdapter(mRestItemAdapter = new RestItemAdapter(mRestTable)));
        mRestItemAdapter.setRestMenu(mCurrentSelection);
        mRestItemAdapter.setExtraMenuListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWeekDialog();
            }
        });

        mRecyclerView.setItemAnimator(new SlideInDownAnimator());

        registerProgressView(rootView.findViewById(R.id.progress_layout));

        if (mRestTable.size() == 0)
            execute(false);
        else
            performTabClick(mCurrentSelection);

        return rootView;
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
                sendTrackerEvent("refresh", "option menu");
                execute(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void performTabClick(int newTabSelection) {
        if (mCurrentTab == null)
            mCurrentTab = mTabList.get(mCurrentSelection);

        mCurrentTab.setSelected(false);

        mCurrentSelection = newTabSelection;
        mCurrentTab = mTabList.get(mCurrentSelection);
        mRestItemAdapter.setRestMenu(mCurrentSelection);
        mCurrentTab.setSelected(true);
        mRecyclerView.getAdapter().notifyItemRangeChanged(0, 4);
        sendClickEvent(REST_TAB_MENU_STRING_LABEL[mCurrentSelection]);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mRecyclerView.getAdapter().notifyItemRangeRemoved(0, 5);
        mRestTable.clear();
    }

    private final AsyncFragmentJob.Base<SparseArray<RestItem>> JOB = new AsyncFragmentJob.Base<SparseArray<RestItem>>() {

        @Override
        public SparseArray<RestItem> call() throws Exception {
            Context context = getActivity();
            if (!force && OApiUtil.getDateTime() - PrefUtil.getInstance(context).get(PrefUtil.KEY_REST_DATE_TIME, 0) < 3) {
                try {
                    SparseArray<RestItem> result = getRestMapFromFile(context);

                    if (result.size() > 0l)
                        return result;

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            // web 에서 읽어온지 오래되었거나, 파일이 존재하지 않은경우 web 에서 읽어옴
            return getRestListFromWeb(context);
        }

        @Override
        public void onPostExcute() {
            super.onPostExcute();

            force = false;
        }

        @Override
        public void onResult(SparseArray<RestItem> result) {
            mRestTable = result;

            performTabClick(mCurrentSelection);

            mRestItemAdapter.mItems = mRestTable;
            mRestItemAdapter.notifyDataSetChanged();

            mSwipeRefreshLayout.setRefreshing(false);

        }

    };

    private void execute(boolean force) {
        this.force = force;
        super.execute(JOB);
    }

    /**
     * web 에서 식단표을 읽어온다.
     */
    public static SparseArray<RestItem> getRestListFromWeb(Context context) throws Exception {

        SparseArray<RestItem> sparseArray = HttpRequest.Builder
                .newStringRequestBuilder("http://m.uos.ac.kr/mkor/food/list.do")
                .build()
                .checkNetworkState(context)
                .wrap(REST_PARSER)
                .get();

        SerializableArrayMap<Integer, RestItem> result = SerializableArrayMap.fromSparseArray(sparseArray);

        IOUtil.writeObjectToFile(context, IOUtil.FILE_REST, result);
        PrefUtil.getInstance(context).put(PrefUtil.KEY_REST_DATE_TIME, OApiUtil.getDate());

        return sparseArray;
    }


    @NonNull
    public static SparseArray<RestItem> getRestMapFromFile(Context context) {
        SerializableArrayMap<Integer, RestItem> map = IOUtil.readFromFileSuppressed(context, IOUtil.FILE_REST);
        return SerializableArrayMap.toSparseArray(map);
    }

    private void showWeekDialog() {
        sendClickEvent("show week");
        WeekInformationDialogFragment dialogFragment = new WeekInformationDialogFragment();
        dialogFragment.setSelection(REST_TAB_MENU_STRING_ID[mCurrentSelection]);

        dialogFragment.show(getFragmentManager(), "week");
    }

    @NonNull
    @Override
    public String getScreenNameForTracker() {
        return "TabRestaurantFragment";
    }

    private static final int VIEW_TYPE_REST_ITEM = 0;
    private static final int VIEW_TYPE_TIME = 1;
    private static final int VIEW_TYPE_EXTRA = 2;

    private static class RestItemAdapter extends RecyclerView.Adapter<ViewHolder> {
        private static final int[] REST_ITEM_TITLES = new int[]{R.string.tab_rest_breakfast, R.string.tab_rest_lunch, R.string.tab_rest_dinner};

        private int restMenu;
        SparseArray<RestItem> mItems;
        private View.OnClickListener l;


        public RestItemAdapter(SparseArray<RestItem> items) {
            this.mItems = items;
        }

        public void setRestMenu(int menu) {
            this.restMenu = menu;
        }

        public void setExtraMenuListener(View.OnClickListener l) {
            this.l = l;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(getItemViewRes(viewType), parent, false), viewType);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            switch (getItemViewType(position)) {
                default:
                case VIEW_TYPE_REST_ITEM: {
                    holder.title.setText(REST_ITEM_TITLES[position]);

                    String s = null;
                    RestItem item = mItems.get(restMenu);

                    if (item == null)
                        item = RestItem.EMPTY;

                    switch (position) {
                        case 0:
                            s = item.breakfast;
                            break;
                        case 1:
                            s = item.lunch;
                            break;
                        case 2:
                            s = item.supper;
                            break;
                        default:
                            break;
                    }
                    holder.content.setText(s);

                    break;
                }

                case VIEW_TYPE_TIME:
                    holder.title.setText(TIME_SEMESTER[restMenu]);
                    holder.content.setText(TIME_VACATION[restMenu]);
                    break;

                case VIEW_TYPE_EXTRA:
                    holder.title.setOnClickListener(l);
                    break;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 3)
                return VIEW_TYPE_TIME;
            else if (position == 4)
                return VIEW_TYPE_EXTRA;
            else
                return VIEW_TYPE_REST_ITEM;

        }

        public int getItemViewRes(int viewType) {
            if (viewType == VIEW_TYPE_TIME)
                return R.layout.list_layout_rest_time;
            else if (viewType == VIEW_TYPE_EXTRA)
                return R.layout.list_layout_rest_extra;
            else
                return R.layout.list_layout_rest;

        }

        @Override
        public int getItemCount() {
            return mItems.size() == 0 ? 0 : 5;
        }

    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView title;
        public TextView content;

        public ViewHolder(View itemView, int viewType) {
            super(itemView);

            switch (viewType) {
                default:
                    title = (TextView) itemView.findViewById(R.id.tab_rest_list_title);
                    content = (TextView) itemView.findViewById(R.id.tab_rest_list_content);
                    break;

                case VIEW_TYPE_EXTRA:
                    title = (TextView) itemView.findViewById(R.id.tab_rest_show_week);
                    break;
            }
        }
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
        // public int id;

        private final int mSelectedColor;
        private final int mNormalColor;

        public Tab(LinearLayout parent) {
            tabView = (FrameLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.view_tab_rest_tab, parent, false);
            ripple = tabView.findViewById(R.id.ripple);
            mTextView = (TextView) tabView.findViewById(R.id.tab_rest_tab_text);
            mStrip = tabView.findViewById(R.id.tab_rest_tab_strip);
            mSelectedColor = AppUtil.getAttrColor(parent.getContext(), R.attr.color_actionbar_title);
            mNormalColor = mSelectedColor | 0xaa << 24;
        }

        public void setSelected(boolean selected) {
            mStrip.setVisibility(selected ? View.VISIBLE : View.INVISIBLE);
            mTextView.setTextColor(selected ? mSelectedColor : mNormalColor);
        }

        public void setText(int stringId) {
            mTextView.setText(stringId);
            // this.id = stringId;
        }


    }

}
