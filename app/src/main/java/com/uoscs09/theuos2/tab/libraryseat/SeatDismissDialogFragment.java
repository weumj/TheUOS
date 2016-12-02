package com.uoscs09.theuos2.tab.libraryseat;


import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.TextView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.BaseDialogFragment;
import com.uoscs09.theuos2.base.ViewHolder;

import butterknife.BindView;
import mj.android.utils.recyclerview.ListRecyclerAdapter;
import mj.android.utils.recyclerview.ListRecyclerUtil;
import mj.android.utils.recyclerview.ViewHolderFactory;

public class SeatDismissDialogFragment extends BaseDialogFragment {

    private static final String TAG = "SeatDismissInfo";

    public static void showFragment(Fragment f, SeatTotalInfo info) {
        SeatDismissDialogFragment fragment = new SeatDismissDialogFragment();
        fragment.setSeatInfo(info);
        fragment.show(f.getFragmentManager(), TAG);
    }

    public static boolean isNotPresent(Fragment f) {
        return f.getFragmentManager().findFragmentByTag(TAG) == null;
    }

    private View mDismissDialogView, mDismissEmptyView;
    private RecyclerView.Adapter mInfoAdapter;
    private SeatTotalInfo mSeatTotalInfo;
    // private Dialog mDialog;

    public void setSeatInfo(SeatTotalInfo info) {
        mSeatTotalInfo = info;
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
        this.mSeatTotalInfo = null;
    }

    protected View createView() {
        if (mDismissDialogView == null) {
            mDismissDialogView = View.inflate(getActivity(), R.layout.dialog_seat_dismiss_info, null);

            Toolbar toolbar = (Toolbar) mDismissDialogView.findViewById(R.id.toolbar);
            toolbar.setTitle(R.string.action_dismiss_info);

            RecyclerView recyclerView = (RecyclerView) mDismissDialogView.findViewById(R.id.tab_library_seat_dismiss_recyclerview);
            LinearLayoutManager manager = new LinearLayoutManager(recyclerView.getContext());

            recyclerView.setLayoutManager(manager);

            if (mSeatTotalInfo != null) {
                mInfoAdapter = new ListRecyclerAdapter<>(mSeatTotalInfo.seatDismissInfoList, new ViewHolderFactory<SeatDismissInfo, ListRecyclerAdapter.ViewHolder<SeatDismissInfo>>() {
                    @Override
                    public ListRecyclerAdapter.ViewHolder<SeatDismissInfo> newViewHolder(ViewGroup viewGroup, int i) {
                        return new Holder(ListRecyclerUtil.makeViewHolderItemView(viewGroup, R.layout.list_layout_seat_dismiss_info));
                    }
                });
                recyclerView.setAdapter(mInfoAdapter);

                if (mSeatTotalInfo.seatDismissInfoList.isEmpty())
                    showDismissInfoEmptyView();
            } else {
                dismiss();
            }
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

            if (mSeatTotalInfo.seatDismissInfoList.isEmpty())
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

    static class Holder extends ViewHolder<SeatDismissInfo> {
        @BindView(R.id.tab_library_seat_info_time)
        public TextView time;
        @BindView(R.id.tab_library_seat_info_number)
        public TextView count;

        public Holder(View v) {
            super(v);
        }

        @Override
        protected void setView(int position) {
            SeatDismissInfo info = getItem();

            time.setText(itemView.getContext().getString(R.string.tab_library_seat_dismiss_info_time_within, info.time));
            count.setText(String.valueOf(info.seatCount));
        }
    }
}
