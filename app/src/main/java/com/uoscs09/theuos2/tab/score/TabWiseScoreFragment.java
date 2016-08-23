package com.uoscs09.theuos2.tab.score;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsProgressFragment;
import com.uoscs09.theuos2.base.ViewHolder;
import com.uoscs09.theuos2.util.AppRequests;
import com.uoscs09.theuos2.util.AppUtil;

import java.util.Map;

import butterknife.BindView;
import butterknife.BindViews;
import mj.android.utils.recyclerview.ListRecyclerAdapter;
import mj.android.utils.recyclerview.ListRecyclerUtil;

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

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    ListRecyclerAdapter<WiseScores.SemesterScore, ScoreViewHolder> adapter;


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final int viewCount = AppUtil.isScreenSizeSmall() ? 2 : 4;
        GridLayoutManager manager = new GridLayoutManager(getActivity(), viewCount);
        mRecyclerView.setLayoutManager(manager);

        mRecyclerView.addItemDecoration(ListRecyclerUtil.squareViewItemDecoration(getActivity(), viewCount, R.dimen.tab_wise_score_side_margin));

        //loadScores("id", "pass");
    }

    private void loadScores(String id, String pw) {

        AppRequests.WiseScores.wiseScores(id, pw).getAsync(wiseScores -> {
                    int i = 0;
                    for (Map.Entry<String, String> entry : wiseScores.scoreMap.entrySet()) {
                        textViews[i++].setText(entry.getKey() + " : " + entry.getValue());
                    }

                    textViews[3].setText("취득학점 : " + wiseScores.scoreEarnedTotal);
                    textViews[4].setText("평점합계 : " + wiseScores.evalSumTotal);
                    textViews[5].setText("평점평균 : " + wiseScores.evalPercentTotal);
                    textViews[6].setText("백분율 환산 : " + wiseScores.evalAverageTotal);

                    mRecyclerView.setAdapter(adapter = ListRecyclerUtil.newSimpleAdapter(wiseScores.semesterScores, ScoreViewHolder.class, R.layout.list_layout_wise_scores));
                },
                throwable -> {
                    super.simpleErrorRespond(throwable);
                }
        );

    }


    static class ScoreViewHolder extends ViewHolder<WiseScores.SemesterScore> {
        @BindViews({android.R.id.text1, android.R.id.text2})
        TextView[] textViews;

        public ScoreViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        protected void setView(int i) {
            textViews[0].setText(getItem().yearAndSemester);
            textViews[1].setText(String.valueOf(getItem().evalAverage));
        }
    }
}
