package com.uoscs09.theuos2.tab.buildings;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsProgressFragment;
import com.uoscs09.theuos2.util.AppRequests;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.OApiUtil;

import butterknife.BindView;
import mj.android.utils.task.DelayedTask;
import se.emilsjolander.stickylistheaders.ExpandableStickyListHeadersListView;

// todo grid
public class TabBuildingRoomFragment extends AbsProgressFragment<BuildingRoom> {
    @Override
    protected int layoutRes() {
        return R.layout.tab_building_room;
    }

    @NonNull
    @Override
    public String getScreenNameForTracker() {
        return "TabBuildingRoomFragment";
    }

    Spinner spinner;
    @BindView(R.id.list)
    ExpandableStickyListHeadersListView mListView;
    BuildingRoomAdapter buildingRoomAdapter;

    //BuildingRoom buildingRoom;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewGroup mTabParent = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.view_tab_building_toolbar_menu, getToolbarParent(), false);
        spinner = (Spinner) mTabParent.findViewById(R.id.spinner);

        registerTabParentView(mTabParent);
    }

    @Override
    protected void setPrevAsyncData(BuildingRoom data) {
        mListView.setAdapter(buildingRoomAdapter = new BuildingRoomAdapter(getActivity(), data));
        buildingRoomAdapter.notifyDataSetChanged();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mListView.setOnHeaderClickListener((l, header, itemPosition, headerId, currentlySticky) -> {
            if (mListView.isHeaderCollapsed(headerId)) {
                mListView.expand(headerId);
            } else {
                mListView.collapse(headerId);
            }
        });

        mListView.setOnItemClickListener((parent, view1, position, id) -> {
            if (buildingRoomAdapter == null)
                return;

            BuildingRoom.RoomInfo item = buildingRoomAdapter.getItem(position);

            Dialog dialog = AppUtil.getProgressDialog(getActivity());
            dialog.show();

            final OApiUtil.Semester semester = OApiUtil.Semester.getSemesterByOrder(spinner.getSelectedItemPosition());
            final DelayedTask<ClassRoomTimetable> task = AppRequests.Buildings.classRoomTimeTables(OApiUtil.getYear(), semester.code, item).delayed()
                    .result(classroomTimeTable -> {
                        ClassroomTimetableDialogFragment.showTimetableDialog(this, classroomTimeTable, semester, view1);
                        sendClickEvent("show classroom timetable");
                    })
                    .error(throwable -> AppUtil.showErrorToast(getActivity(), throwable, true))
                    .atLast(() -> {
                        dialog.dismiss();
                        dialog.setOnCancelListener(null);
                    });
            task.execute();

            dialog.setOnCancelListener(dialog1 -> task.cancel());

        });

        registerProgressView(view.findViewById(R.id.progress_layout));

        loadData(false);

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
                loadData(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void loadData(boolean force) {
        appTask(AppRequests.Buildings.buildingRooms(force))
                .result(room -> mListView.setAdapter(buildingRoomAdapter = new BuildingRoomAdapter(getActivity(), room)))
                .error(throwable -> super.simpleErrorRespond(throwable))
                .build()
                .execute();
    }
}
