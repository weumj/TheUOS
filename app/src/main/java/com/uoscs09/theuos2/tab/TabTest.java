package com.uoscs09.theuos2.tab;

import android.support.v4.app.Fragment;


public class TabTest extends Fragment {
/*
    private TextView textView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        textView = new TextView(getActivity());
        ScrollView scrollView = new ScrollView(getActivity());
        scrollView.addView(textView);

        AsyncUtil.execute(
                new Callable<RestWeekItem>() {
                    @Override
                    public RestWeekItem call() throws Exception {
                        return new RestaurantWeekMenuParser().parse(HttpTask.getBody("http://www.uos.ac.kr/food/placeList.do?rstcde=020"));
                    }
                },
                new AsyncCallback.Base<RestWeekItem>() {
                    @Override
                    public void onResult(RestWeekItem result) {
                        StringBuilder sb = new StringBuilder();
                        for (RestItem item : result.weekList) {
                            sb.append(item.title)
                                    .append("\n\n")
                                    .append(item.breakfast)
                                    .append("\n\n")
                                    .append(item.lunch)
                                    .append("\n\n")
                                    .append(item.supper)
                                    .append("\n\n")
                                    .append("-------------------------------------")
                                    .append("\n\n\n");
                        }
                        textView.setText(sb.toString());
                    }
                });

/*
        AsyncUtil.execute(
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
*/

}
