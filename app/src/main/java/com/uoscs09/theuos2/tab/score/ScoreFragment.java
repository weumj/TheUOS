package com.uoscs09.theuos2.tab.score;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.NumberPicker;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.annotation.ReleaseWhenDestroy;
import com.uoscs09.theuos2.async.AsyncFragmentJob;
import com.uoscs09.theuos2.base.AbsAsyncFragment;
import com.uoscs09.theuos2.http.HttpRequest;
import com.uoscs09.theuos2.parse.ParseSubjectList;
import com.uoscs09.theuos2.parse.ParseSubjectScore;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.OApiUtil;
import com.uoscs09.theuos2.util.StringUtil;

import java.util.ArrayList;

public class ScoreFragment extends AbsAsyncFragment<ArrayList<ScoreItem>> {
    @ReleaseWhenDestroy
    private AlertDialog alertDialog;
    @ReleaseWhenDestroy
    private Dialog mProgressDialog;
    @ReleaseWhenDestroy
    private NumberPicker datePicker, termPicker;
    @ReleaseWhenDestroy
    private EditText edit;
    private ArrayMap<String, String> table;
    @ReleaseWhenDestroy
    private ScoreAdapter adapter;
    @ReleaseWhenDestroy
    private ExpandableListView listView;

    private final ParseSubjectList mSubjectListParser = new ParseSubjectList();
    private final ParseSubjectScore mParser = new ParseSubjectScore();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Context context = getActivity();

        View dialogView = View.inflate(context, R.layout.dialog_score, null);
        datePicker = (NumberPicker) dialogView.findViewById(R.id.score_datePicker);
        int currentYear_1 = Integer.parseInt(OApiUtil.getYear());
        datePicker.setMaxValue(currentYear_1);
        datePicker.setMinValue(2007);
        datePicker.setValue(currentYear_1);

        termPicker = (NumberPicker) dialogView.findViewById(R.id.score_termPicker);
        termPicker.setMaxValue(2);
        termPicker.setMinValue(1);

        edit = (EditText) dialogView.findViewById(R.id.score_editText1);
        alertDialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setTitle(R.string.title_tab_score)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        execute();
                    }
                })
                .create();

        mProgressDialog = AppUtil.getProgressDialog(context, false, null);
        adapter = new ScoreAdapter(context, new ArrayList<ScoreItem>());
        initTable();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v;
        switch (AppUtil.theme) {
            case Black:
                v = inflater.inflate(R.layout.etc_score_dark, container, false);
                break;
            case BlackAndWhite:
            case White:
            default:
                v = inflater.inflate(R.layout.etc_score, container, false);
                break;
        }

        listView = (ExpandableListView) v.findViewById(R.id.expandableListView1);
        listView.setAdapter(adapter);
        View emptyView = v.findViewById(R.id.tab_search_subject_empty_view);
        emptyView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                alertDialog.show();
            }
        });
        listView.setEmptyView(emptyView);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.etc_score, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_wise:
                alertDialog.show();
                return true;
            default:
                return false;
        }
    }

    private void initTable() {
        table = new ArrayMap<>();
        table.put(OApiUtil.API_KEY, OApiUtil.UOS_API_KEY);
    }

    private void execute() {
        super.execute(JOB);
    }

    private AsyncFragmentJob.Base<ArrayList<ScoreItem>> JOB = new AsyncFragmentJob.Base<ArrayList<ScoreItem>>() {
        @Override
        public void onResult(ArrayList<ScoreItem> result) {
            adapter = new ScoreAdapter(getActivity(), result);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }

        @Override
        public ArrayList<ScoreItem> call() throws Exception {
            table.put(OApiUtil.SUBJECT_NAME, edit.getText().toString());
            table.put(OApiUtil.YEAR, String.valueOf(datePicker.getValue()));
            table.put(OApiUtil.TERM, termPicker.getValue() == 1 ? "A10" : "A20");

            ArrayList<ArrayList<String>> numList = HttpRequest.Builder
                    .newStringRequestBuilder("http://wise.uos.ac.kr/uosdoc/api.ApiApiSubjectList.oapi")
                    .setParams(table)
                    .setParamsEncoding(StringUtil.ENCODE_EUC_KR)
                    .setResultEncoding(StringUtil.ENCODE_EUC_KR)
                    .build()
                    .wrap(mSubjectListParser)
                    .get();


            HttpRequest.Builder<String> requestBuilder = HttpRequest.Builder
                    .newStringRequestBuilder("http://wise.uos.ac.kr/uosdoc/api.ApiUcsLecturerEstimateResultInq.oapi")
                    .setParams(table)
                    .setParamsEncoding(StringUtil.ENCODE_EUC_KR)
                    .setResultEncoding(StringUtil.ENCODE_EUC_KR);

            ArrayList<ScoreItem> list = new ArrayList<>();
            int size = numList.size();
            for (int i = 0; i < size; i++) {
                ArrayList<String> item = numList.get(i);
                table.put(OApiUtil.SUBJECT_NO, item.get(0));
                table.put(OApiUtil.SUBJECT_NAME, item.get(1));

                list.addAll(
                        requestBuilder.build()
                                .wrap(mParser)
                                .get()
                );

            }

            table.remove(OApiUtil.SUBJECT_NO);
            table.remove(OApiUtil.SUBJECT_NAME);
            return list;
        }
    };


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog.show();
    }

    @Override
    protected void onPostExecute() {
        super.onPostExecute();
        mProgressDialog.dismiss();
    }

    @NonNull
    @Override
    protected String getFragmentNameForTracker() {
        return "ScoreFragment";
    }
}
