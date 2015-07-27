package com.uoscs09.theuos2.tab.libraryseat;


import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.TextView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.BaseDialogFragment;
import com.uoscs09.theuos2.base.ListRecyclerAdapter;

import java.util.List;

public class SeatDismissDialogFragment extends BaseDialogFragment {

    private View mDismissDialogView, mDismissEmptyView;
    private SeatDismissInfoListAdapter mInfoAdapter;
    private SeatInfo mSeatInfo;

    public void setSeatInfo(SeatInfo info) {
        mSeatInfo = info;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);

        if (mInfoAdapter == null)
            mInfoAdapter = new SeatDismissInfoListAdapter(mSeatInfo.seatDismissInfoList);
        else
            notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setView(createView())
                .create();
    }

    private View createView() {
        mDismissDialogView = View.inflate(getActivity(), R.layout.dialog_seat_dismiss_info, null);

        Toolbar toolbar = (Toolbar) mDismissDialogView.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.action_dismiss_info);

        RecyclerView recyclerView = (RecyclerView) mDismissDialogView.findViewById(R.id.tab_library_seat_dismiss_recyclerview);
        LinearLayoutManager manager = new LinearLayoutManager(recyclerView.getContext());

        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(mInfoAdapter);

        return mDismissDialogView;
    }

    private void showDismissInfoEmptyView() {
        if (mDismissEmptyView == null) {
            mDismissEmptyView = ((ViewStub) mDismissDialogView.findViewById(R.id.tab_library_seat_dismiss_stub_empty_info)).inflate();
            mDismissEmptyView.findViewById(android.R.id.content).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
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

    static class SeatDismissInfoListAdapter extends ListRecyclerAdapter<SeatDismissInfo, SeatDismissInfoListAdapter.Holder> {

        public SeatDismissInfoListAdapter(List<SeatDismissInfo> list) {
            super(list);
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_layout_seat_dismiss_info, parent, false));
        }

        static class Holder extends ListRecyclerAdapter.ViewHolder<SeatDismissInfo> {
            public final TextView time, count;

            public Holder(View v) {
                super(v);
                time = (TextView) v.findViewById(R.id.tab_libray_seat_info_time);
                count = (TextView) v.findViewById(R.id.tab_libray_seat_info_number);
            }

            @Override
            protected void setView() {
                SeatDismissInfo info = getItem();

                time.setText(time.getContext().getString(R.string.tab_library_seat_dismiss_info_time_within, info.time));
                count.setText(Integer.toString(info.seatCount));
            }
        }
    }

}
