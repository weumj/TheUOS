package com.uoscs09.theuos2.tab;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.javacan.asyncexcute.AsyncCallback;
import com.uoscs09.theuos2.common.AsyncLoader;
import com.uoscs09.theuos2.parse.ParseUosRSS;
import com.uoscs09.theuos2.parse.ParseUtil;

import java.util.ArrayList;
import java.util.concurrent.Callable;


public class TabTest extends Fragment {

    private TextView textView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        textView = new TextView(getActivity());
        ScrollView scrollView = new ScrollView(getActivity());
        scrollView.addView(textView);


        AsyncLoader.excute(
                new Callable<ArrayList<ParseUosRSS.Item>>() {
                    @Override
                    public ArrayList<ParseUosRSS.Item> call() throws Exception {
                        return ParseUtil.parseXml(ParseUosRSS.getParser(), "http://www.uos.ac.kr/rss/gBoard.do");
                    }
                },
                new AsyncCallback.Base<ArrayList<ParseUosRSS.Item>>() {
                    @Override
                    public void onResult(ArrayList<ParseUosRSS.Item> result) {
                        StringBuilder sb = new StringBuilder();
                        for (ParseUosRSS.Item item : result) {
                            sb.append(item.title)
                                    .append("\n\n")
                                    .append(item.link)
                                    .append("\n\n")
                                    .append(item.description)
                                    .append("\n\n")
                                    .append(item.author)
                                    .append("\n\n")
                                    .append(item.pubDate)
                                    .append("\n\n")
                                    .append("-------------------------------------")
                                    .append("\n\n\n");
                        }
                        textView.setText(sb.toString());
                    }
                });

        return scrollView;
    }


}
