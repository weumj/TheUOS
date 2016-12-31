package com.uoscs09.theuos2.tab.score;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsProgressFragment;
import com.uoscs09.theuos2.base.BaseActivity;
import com.uoscs09.theuos2.base.ViewHolder;
import com.uoscs09.theuos2.util.AppRequests;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.ResourceUtil;

import java.util.Map;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mj.android.utils.recyclerview.ListRecyclerAdapter;
import mj.android.utils.recyclerview.ListRecyclerUtil;
import mj.android.utils.recyclerview.ViewHolderFactory;

public class TabWiseScoreFragment extends AbsProgressFragment<WiseScores> {
    @Override
    protected int layoutRes() {
        return R.layout.tab_wisescore;
    }

    @NonNull
    @Override
    public String getScreenNameForTracker() {
        return "TabWiseScoreFragment";
    }


    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    ScoreRecyclerAdapter adapter;

    @BindView(R.id.empty_view)
    View empty;


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (adapter != null && adapter.wiseScores != null)
            outState.putParcelable(getScreenNameForTracker(), adapter.wiseScores);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            if (adapter == null)
                adapter = new ScoreRecyclerAdapter(null);
        } else {
            WiseScores wiseScores = savedInstanceState.getParcelable(getScreenNameForTracker());

            if (wiseScores != null) {
                empty.setVisibility(View.GONE);
            }

            adapter = new ScoreRecyclerAdapter(wiseScores);
        }
    }

    @Override
    protected void setPrevAsyncData(WiseScores data) {
        if (data != null)
            adapter = new ScoreRecyclerAdapter(data);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final int viewCount = ResourceUtil.isScreenSizeSmall() ? 1 : 4;
        GridLayoutManager manager = new GridLayoutManager(getActivity(), viewCount);
        mRecyclerView.setLayoutManager(manager);
        //DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(), manager.getOrientation());
        //mRecyclerView.addItemDecoration(dividerItemDecoration);

        mRecyclerView.setAdapter(adapter);
    }


    @OnClick(R.id.empty1)
    void emptyClick() {
        sendEmptyViewClickEvent();
        showLoginDialog();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.tab_search_empty_room, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                showLoginDialog();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    void showLoginDialog() {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_tab_score_login, (ViewGroup) getView(), false);

        final EditText idView = (EditText) v.findViewById(R.id.dialog_wise_id_input);
        final EditText passView = (EditText) v.findViewById(R.id.dialog_wise_passwd_input);

        new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.tab_wise_score_login)
                .setIconAttribute(R.attr.theme_ic_action_login)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String id = idView.getText().toString().trim();
                    String pass = passView.getText().toString().trim();

                    if (TextUtils.isEmpty(id) || TextUtils.isEmpty(pass)) {
                        AppUtil.showToast(getActivity(), R.string.tab_timetable_wise_login_warning_null);
                        return;
                    }

                    sendClickEvent("mwise login");

                    loadScores(id, pass);
                })
                .setNegativeButton(android.R.string.no, null)
                .show();

        idView.performClick();
    }

    private void loadScores(String id, String pw) {
        final MaterialDialog progressDialog = AppUtil.getProgressDialog(getActivity());
        progressDialog.show();

        AppRequests.WiseScores.wiseScores(id, pw)
                .delayed()
                .result(wiseScores -> {
                    adapter.wiseScores = wiseScores;
                    adapter.notifyDataSetChanged();
                    empty.setVisibility(View.GONE);
                })
                .error(throwable -> super.simpleErrorRespond(throwable))
                .atLast(progressDialog::dismiss)
                .execute();

    }

    private static class ScoreRecyclerAdapter extends RecyclerView.Adapter<BaseScoreViewHolder> {
        WiseScores wiseScores;

        ScoreRecyclerAdapter(WiseScores wiseScore) {
            this.wiseScores = wiseScore;
        }

        @Override
        public int getItemCount() {
            if (wiseScores == null || wiseScores.semesterScores == null)
                return 0;

            return wiseScores.semesterScores.size() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            return position == 0 ? 0 : 1;
        }

        @Override
        public BaseScoreViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            if (viewType == 0) {
                return new ScoreViewHolder1(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_layout_wise_scores_head, viewGroup, false));
            } else {
                return new ScoreViewHolder2(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_layout_wise_scores, viewGroup, false));
            }
        }

        @Override
        public void onBindViewHolder(BaseScoreViewHolder holder, int position) {
            if (holder instanceof ScoreViewHolder1) {
                ((ScoreViewHolder1) holder).wiseScores = wiseScores;
            } else if (holder instanceof ScoreViewHolder2) {
                ((ScoreViewHolder2) holder).semesterScore = wiseScores.semesterScores.get(position - 1);
            }

            holder.setView(position);
        }
    }

    static abstract class BaseScoreViewHolder extends RecyclerView.ViewHolder {
        BaseScoreViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        protected abstract void setView(int position);

    }

    static class ScoreViewHolder1 extends BaseScoreViewHolder {
        @BindViews({
                R.id.tab_wise_score_text1,
                R.id.tab_wise_score_text2,
                R.id.tab_wise_score_text3,
                R.id.tab_wise_score_text4,
                R.id.tab_wise_score_text5,
                R.id.tab_wise_score_text6,
                R.id.tab_wise_score_text7,
        })
        TextView[] textViews;

        WiseScores wiseScores;

        ScoreViewHolder1(View itemView) {
            super(itemView);
        }

        @Override
        protected void setView(int position) {
            if (wiseScores != null) {
                int i = 0;
                for (Map.Entry<String, String> entry : wiseScores.scoreMap.entrySet()) {
                    textViews[i++].setText(entry.getKey() + " : " + entry.getValue());
                }

                Resources r = itemView.getResources();
                textViews[3].setText(r.getString(R.string.tab_wise_score_score_earned_total, String.valueOf(wiseScores.scoreEarnedTotal)));
                textViews[4].setText(r.getString(R.string.tab_wise_score_eval_sum_total, String.valueOf(wiseScores.evalSumTotal)));
                textViews[5].setText(r.getString(R.string.tab_wise_score_eval_percent_total, String.valueOf(wiseScores.evalPercentTotal)));
                textViews[6].setText(r.getString(R.string.tab_wise_score_eval_average_total, String.valueOf(wiseScores.evalAverageTotal)));
            }
        }
    }

    static class ScoreViewHolder2 extends BaseScoreViewHolder {
        @BindViews({android.R.id.text1, android.R.id.text2})
        TextView[] textViews;

        WiseScores.SemesterScore semesterScore;

        ScoreViewHolder2(View itemView) {
            super(itemView);
            itemView.findViewById(R.id.ripple).setOnClickListener(v -> {
                Context context = itemView.getContext();

                View vv = View.inflate(context, R.layout.dialog_tab_score_sub, null);
                RecyclerView recyclerView = (RecyclerView) vv.findViewById(R.id.recyclerview);
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
                recyclerView.setAdapter(new ListRecyclerAdapter<>(semesterScore.subjectScores, new ViewHolderFactory<WiseScores.SubjectScore, ListRecyclerAdapter.ViewHolder<WiseScores.SubjectScore>>() {
                    @Override
                    public ListRecyclerAdapter.ViewHolder<WiseScores.SubjectScore> newViewHolder(ViewGroup viewGroup, int i) {
                        return new SubScoreViewHolder(ListRecyclerUtil.makeViewHolderItemView(viewGroup, R.layout.list_layout_wise_scores_sub));
                    }
                }
                ));

                Toolbar toolbar = (Toolbar) vv.findViewById(R.id.toolbar);
                toolbar.setTitle(semesterScore.yearAndSemester);
                toolbar.setSubtitle(context.getString(R.string.tab_wise_score_sub_subtitle, semesterScore.scoreEarned, String.valueOf(semesterScore.evalAverage), String.valueOf(semesterScore.evalPercent)));

                new AlertDialog.Builder(context)
                        .setView(vv)
                        .setCancelable(true)
                        .show();

                if (context instanceof BaseActivity) {
                    ((BaseActivity) context).getTrackerUtil().sendClickEvent("TabWiseScoreFragment", "detail score");
                }
            });
        }

        @Override
        protected void setView(int i) {
            if (semesterScore != null) {
                textViews[0].setText(semesterScore.yearAndSemester);
                textViews[1].setText(String.valueOf(semesterScore.evalAverage));
            }
        }
    }

    static class SubScoreViewHolder extends ViewHolder<WiseScores.SubjectScore> {
        @BindViews({
                R.id.order,
                R.id.name,
                R.id.subject_div,
                R.id.credit,
                R.id.grade,
                R.id.eval,
                R.id.valid,
                R.id.retaking,
                R.id.retaking_name,
        })
        TextView[] textViews;

        SubScoreViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void setView(int position) {
            super.setView(position);

            WiseScores.SubjectScore item = getItem();
            textViews[0].setText(String.valueOf(item.order));
            textViews[1].setText(item.name);
            textViews[2].setText(item.subjectDiv);
            textViews[3].setText(String.valueOf(item.credit));
            textViews[4].setText(String.valueOf(item.grade));
            textViews[5].setText(String.valueOf(item.eval));
            textViews[6].setText(item.valid ? "Y" : "N");
            textViews[7].setText(item.retaking ? "Y" : "N");
            textViews[8].setText(item.retakingSubjectName);

        }
    }


    //private static class DetailScoreDialogFragment extends Bottom

}
