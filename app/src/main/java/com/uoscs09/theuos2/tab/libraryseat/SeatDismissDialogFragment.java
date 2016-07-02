package com.uoscs09.theuos2.tab.libraryseat;


import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewStub;
import android.widget.TextView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.BaseDialogFragment;

import butterknife.BindView;
import mj.android.utils.recyclerview.ListRecyclerAdapter;
import mj.android.utils.recyclerview.ListRecyclerUtil;

public class SeatDismissDialogFragment extends BaseDialogFragment {

    private View mDismissDialogView, mDismissEmptyView;
    private RecyclerView.Adapter mInfoAdapter;
    private SeatInfo mSeatInfo;
    // private Dialog mDialog;

    public void setSeatInfo(SeatInfo info) {
        mSeatInfo = info;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public final Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = createView();

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mDismissDialogView = null;
        mDismissEmptyView = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.mSeatInfo = null;
    }

    protected View createView() {
        if (mDismissDialogView == null) {
            mDismissDialogView = View.inflate(getActivity(), R.layout.dialog_seat_dismiss_info, null);

            Toolbar toolbar = (Toolbar) mDismissDialogView.findViewById(R.id.toolbar);
            toolbar.setTitle(R.string.action_dismiss_info);

            RecyclerView recyclerView = (RecyclerView) mDismissDialogView.findViewById(R.id.tab_library_seat_dismiss_recyclerview);
            LinearLayoutManager manager = new LinearLayoutManager(recyclerView.getContext());

            recyclerView.setLayoutManager(manager);

            mInfoAdapter = ListRecyclerUtil.newSimpleAdapter(mSeatInfo.seatDismissInfoList, Holder.class, R.layout.list_layout_seat_dismiss_info);
            recyclerView.setAdapter(mInfoAdapter);

            if (mSeatInfo.seatDismissInfoList.isEmpty())
                showDismissInfoEmptyView();
        }
        return mDismissDialogView;
    }

    private void showDismissInfoEmptyView() {
        if (mDismissEmptyView == null) {
            mDismissEmptyView = ((ViewStub) mDismissDialogView.findViewById(R.id.tab_library_seat_dismiss_stub_empty_info)).inflate();
            mDismissEmptyView.findViewById(android.R.id.content).setOnClickListener(v -> dismiss());
        } else {
            mDismissEmptyView.setVisibility(View.VISIBLE);
        }
    }

    public void notifyDataSetChanged() {
        if (mInfoAdapter != null) {
            mInfoAdapter.notifyDataSetChanged();

            if (mSeatInfo.seatDismissInfoList.isEmpty())
                showDismissInfoEmptyView();
            else if (mDismissEmptyView != null)
                mDismissEmptyView.setVisibility(View.GONE);
        }
    }

    @NonNull
    @Override
    public String getScreenNameForTracker() {
        return "SeatDismissDialogFragment";
    }

    static class Holder extends ListRecyclerAdapter.ViewHolder<SeatDismissInfo> {
        @BindView(R.id.tab_libray_seat_info_time)
        public TextView time;
        @BindView(R.id.tab_libray_seat_info_number)
        public TextView count;

        public Holder(View v) {
            super(v);
        }

        @Override
        protected void setView(int position) {
            SeatDismissInfo info = getItem();

            time.setText(time.getContext().getString(R.string.tab_library_seat_dismiss_info_time_within, info.time));
            count.setText(String.valueOf(info.seatCount));
        }
    }
}
