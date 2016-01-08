package com.uoscs09.theuos2.tab.restaurant;

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
import com.uoscs09.theuos2.base.AbsProgressFragment;
import com.uoscs09.theuos2.util.AppResources;
import com.uoscs09.theuos2.util.AppUtil;

import java.util.ArrayList;
import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;
import jp.wasabeef.recyclerview.animators.SlideInDownAnimator;

public class TabRestaurantFragment extends AbsProgressFragment<SparseArray<RestItem>> {
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

    @Bind(R.id.tab_rest_swipe_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.tab_rest_recycler_view)
    RecyclerView mRecyclerView;

    private RestItemAdapter mRestItemAdapter;
    // 리스트의 한 아이템은 식당 정보 (아침 점심 저녁) 를 나타냄
    @AsyncData
    private SparseArray<RestItem> mRestTable;

    private int mCurrentSelection;


    //private String mCurrentRestName;
    private final ArrayList<Tab> mTabList = new ArrayList<>();
    private Tab mCurrentTab;


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
            tab.ripple.setOnClickListener(v -> performTabClick(mTabList.indexOf(tab)));
            mTabParent.addView(tab.tabView);
            mTabList.add(tab);
        }

        registerTabParentView(mTabParent);

    }

    @Override
    protected int getLayout() {
        return R.layout.tab_restaurant;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSwipeRefreshLayout.setColorSchemeResources(
                AppUtil.getAttrValue(getActivity(), R.attr.color_actionbar_title),
                AppUtil.getAttrValue(getActivity(), R.attr.colorAccent)
        );
        mSwipeRefreshLayout.setProgressBackgroundColorSchemeResource(AppUtil.getAttrValue(getActivity(), R.attr.colorPrimary));
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            sendTrackerEvent("refresh", "SwipeRefreshView");
            execute(true);
        });


        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setAdapter(new SlideInBottomAnimationAdapter(mRestItemAdapter = new RestItemAdapter(mRestTable)));
        mRestItemAdapter.setRestMenu(mCurrentSelection);
        mRestItemAdapter.setExtraMenuListener(v -> showWeekDialog());

        mRecyclerView.setItemAnimator(new SlideInDownAnimator());

        registerProgressView(view.findViewById(R.id.progress_layout));

        if (mRestTable.size() == 0)
            execute(false);
        else
            performTabClick(mCurrentSelection);
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


    private void execute(boolean force) {
        mRecyclerView.getAdapter().notifyItemRangeRemoved(0, 5);
        mRestTable.clear();

        execute(true,
                AppResources.Restaurants.request(getActivity(), force),
                result -> {
                    mRestTable = result;

                    performTabClick(mCurrentSelection);

                    mRestItemAdapter.mItems = mRestTable;
                    mRestItemAdapter.notifyDataSetChanged();

                    mSwipeRefreshLayout.setRefreshing(false);
                },
                this::onError,
                true
        );
    }

    public void onError(Exception e) {
        e.printStackTrace();
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
    protected static class Tab {
        public final FrameLayout tabView;
        @Bind(R.id.tab_rest_tab_text)
        public TextView mTextView;
        @Bind(R.id.ripple)
        public View ripple;
        @Bind(R.id.tab_rest_tab_strip)
        public View mStrip;
        // public int id;

        private final int mSelectedColor;
        private final int mNormalColor;

        public Tab(LinearLayout parent) {
            tabView = (FrameLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.view_tab_rest_tab, parent, false);
            ButterKnife.bind(this, tabView);

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
