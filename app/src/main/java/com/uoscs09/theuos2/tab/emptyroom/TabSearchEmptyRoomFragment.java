package com.uoscs09.theuos2.tab.emptyroom;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.annotation.AsyncData;
import com.uoscs09.theuos2.base.AbsProgressFragment;
import com.uoscs09.theuos2.tab.buildings.ClassroomTimetableDialogFragment;
import com.uoscs09.theuos2.util.AppRequests;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.OApiUtil;
import com.uoscs09.theuos2.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemClick;

/**
 * 빈 강의실을 조회하는 fragment
 */
public class TabSearchEmptyRoomFragment extends AbsProgressFragment<List<EmptyRoom>> {

    @Override
    protected int layoutRes() {
        return R.layout.tab_search_empty_room;
    }

    private AlertDialog mSearchDialog;
    private Spinner mBuildingSpinner;
    private Spinner mTimeSpinner;
    private Spinner mTermSpinner;
    private TextView[] textViews;
    private View[] tabStrips;

    @BindView(R.id.tab_search_subject_empty_view)
    View mEmptyView;
    @BindView(R.id.etc_search_list)
    ListView mListView;

    private ArrayAdapter<EmptyRoom> mAdapter;
    @AsyncData
    private ArrayList<EmptyRoom> mClassRoomList;
    private String mTermString;
    private boolean isReverse = false;
    private int mTabSelection = -1;

    private static final String BUILDING = "building";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        initSearchDialog();

        if (savedInstanceState != null) {
            mClassRoomList = savedInstanceState.getParcelableArrayList(BUILDING);
            mTermString = savedInstanceState.getString("time");
        } else {
            mClassRoomList = new ArrayList<>();
        }

        ViewGroup mTabParent = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.view_tab_emptyroom_toobar_menu, getToolbarParent(), false);
        textViews = new TextView[4];
        tabStrips = new View[4];
        int[] ids = {
                R.id.tab_search_empty_room_text_building_name,
                R.id.tab_search_empty_room_text_room_no,
                R.id.tab_search_empty_room_text_room_subj,
                R.id.tab_search_empty_room_text_room_person
        };

        int i = 0;
        for (int id : ids) {
            final int j = i;
            View ripple = mTabParent.findViewById(id);
            ripple.setOnClickListener(v -> onTabClick(j));

            textViews[i] = (TextView) ripple.findViewById(android.R.id.title);
            tabStrips[i++] = ripple.findViewById(R.id.tab_tab_strip);

        }
        registerTabParentView(mTabParent);

        super.onCreate(savedInstanceState);
    }

    private void initSearchDialog() {

        View dialogLayout = View.inflate(getActivity(), R.layout.dialog_search_empty_room, null);

        mBuildingSpinner = (Spinner) dialogLayout.findViewById(R.id.etc_empty_spinner_building);
        mBuildingSpinner.setSelection(1, false);
        mTimeSpinner = (Spinner) dialogLayout.findViewById(R.id.etc_empty_spinner_time);
        mTermSpinner = (Spinner) dialogLayout.findViewById(R.id.etc_empty_spinner_term);

        mSearchDialog = new AlertDialog.Builder(getActivity())
                .setView(dialogLayout)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    execute();
                })
                .create();
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAdapter = new SearchEmptyRoomAdapter(getActivity(), mClassRoomList);

        mListView.setAdapter(mAdapter);

        mListView.setEmptyView(mEmptyView);

        registerProgressView(view.findViewById(R.id.progress_layout));
    }

    @OnItemClick(R.id.etc_search_list)
    void roomClick(int position) {
        // 선택된 빈 강의실의 시간표 보여주기

        Dialog dialog = AppUtil.getProgressDialog(getActivity());
        dialog.show();

        final OApiUtil.Semester semester = OApiUtil.Semester.getSemesterByOrder(mTermSpinner.getSelectedItemPosition());
        AppRequests.Buildings.classRoomTimeTables(OApiUtil.getYear(), semester.code, mClassRoomList.get(position), false).getAsync(
                classRoomTimeTable -> {
                    ClassroomTimetableDialogFragment.showTimetableDialog(this, classRoomTimeTable, semester);

                    dialog.dismiss();

                    sendClickEvent("show classroom timetable");
                },
                throwable -> {
                    AppUtil.showErrorToast(getActivity(), throwable, true);
                    dialog.dismiss();
                }
        );

    }

    @OnClick(R.id.empty1)
    void emptyClick() {
        sendEmptyViewClickEvent();
        mSearchDialog.show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(BUILDING, mClassRoomList);
        outState.putString("time", mTermString);
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.tab_search_empty_room, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                sendClickEvent("option menu : search");
                mSearchDialog.show();
                return true;
            default:
                return false;
        }
    }

    private void execute() {
        mEmptyView.setVisibility(View.INVISIBLE);

        String building = ((String) mBuildingSpinner.getSelectedItem()).split(StringUtil.SPACE)[0];
        int time = mTimeSpinner.getSelectedItemPosition() + 1;
        int term = mTermSpinner.getSelectedItemPosition();

        execute(AppRequests.EmptyRooms.request(building, time, term),
                result -> {
                    mClassRoomList.clear();
                    mClassRoomList.addAll(result);
                    mAdapter.notifyDataSetChanged();
                    AppUtil.showToast(getActivity(), getString(R.string.search_found_amount, result.size()), true);

                    mTermString = mTimeSpinner.getSelectedItem().toString().split(StringUtil.NEW_LINE)[1] + StringUtil.NEW_LINE + mTermSpinner.getSelectedItem();
                    setSubtitleWhenVisible(mTermString);

                    if (mAdapter.isEmpty())
                        mEmptyView.setVisibility(View.VISIBLE);

                },
                e -> {
                    e.printStackTrace();

                    if (mAdapter.isEmpty())
                        mEmptyView.setVisibility(View.VISIBLE);

                    simpleErrorRespond(e);
                }
        );
    }

    private void onTabClick(int field) {
        if (mClassRoomList.isEmpty()) {
            return;
        }

        if (mTabSelection != -1) {
            textViews[mTabSelection].setCompoundDrawables(null, null, null, null);
            tabStrips[mTabSelection].setVisibility(View.INVISIBLE);
        }

        isReverse = field == mTabSelection && !isReverse;
        mTabSelection = field;

        /*
        switch (id) {
            case R.id.tab_search_empty_room_text_building_name:
                field = 0;
                break;
            case R.id.tab_search_empty_room_text_room_no:
                field = 1;
                break;
            case R.id.tab_search_empty_room_text_room_subj:
                field = 2;
                break;
            case R.id.tab_search_empty_room_text_room_person:
                field = 3;
                break;
            default:
                return;
        }
        */

        sendClickEvent("sort", field);


        textViews[field].setCompoundDrawablesWithIntrinsicBounds(
                AppUtil.getAttrValue(getActivity(), isReverse ?
                        R.attr.menu_theme_ic_action_navigation_arrow_drop_up : R.attr.menu_theme_ic_action_navigation_arrow_drop_down), 0, 0, 0);
        tabStrips[field].setVisibility(View.VISIBLE);

        mAdapter.sort(EmptyRoom.getComparator(field, isReverse));
    }

    @Override
    protected CharSequence getSubtitle() {
        return mTermString;
    }

    @NonNull
    @Override
    public String getScreenNameForTracker() {
        return "TabSearchEmptyRoomFragment";
    }

}