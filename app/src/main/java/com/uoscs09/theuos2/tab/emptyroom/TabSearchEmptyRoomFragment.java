package com.uoscs09.theuos2.tab.emptyroom;

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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.annotation.AsyncData;
import com.uoscs09.theuos2.async.AbstractRequest;
import com.uoscs09.theuos2.async.Request;
import com.uoscs09.theuos2.base.AbsProgressFragment;
import com.uoscs09.theuos2.http.HttpRequest;
import com.uoscs09.theuos2.parse.XmlParserWrapper;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.OApiUtil;
import com.uoscs09.theuos2.util.OApiUtil.Semester;
import com.uoscs09.theuos2.util.StringUtil;

import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.Bind;

/**
 * 빈 강의실을 조회하는 fragment
 */
public class TabSearchEmptyRoomFragment extends AbsProgressFragment<ArrayList<EmptyClassRoomItem>> implements Request.ResultListener<ArrayList<EmptyClassRoomItem>>, Request.ErrorListener {

    private AlertDialog mSearchDialog;
    private Spinner mBuildingSpinner;
    private Spinner mTimeSpinner;
    private Spinner mTermSpinner;
    private TextView[] textViews;
    private View[] tabStrips;
    @Bind(R.id.tab_search_subject_empty_view)
    View mEmptyView;
    @Bind(R.id.etc_search_list)
    ListView mListView;

    private ArrayAdapter<EmptyClassRoomItem> mAdapter;
    @AsyncData
    private ArrayList<EmptyClassRoomItem> mClassRoomList;
    private final ArrayMap<String, String> params = new ArrayMap<>(9);
    private String mTermString;
    private boolean isReverse = false;
    private int mTabSelection = -1;

    private static final String BUILDING = "building";
    private static final String URL = "http://wise.uos.ac.kr/uosdoc/api.ApiUcsFromToEmptyRoom.oapi";

    private static final XmlParserWrapper<ArrayList<EmptyClassRoomItem>> EMPTY_ROOM_PARSER = OApiUtil.getParser(EmptyClassRoomItem.class);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        initParamsTable();

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
                R.id.tab_search_empty_room_text_room_person};

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

        mEmptyView.findViewById(R.id.empty1).setOnClickListener(v -> {
            sendEmptyViewClickEvent();
            mSearchDialog.show();
        });
        mListView.setEmptyView(mEmptyView);

        registerProgressView(view.findViewById(R.id.progress_layout));
    }

    @Override
    protected int getLayout() {
        return R.layout.tab_search_empty_room;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(BUILDING, mClassRoomList);
        outState.putString("time", mTermString);
        super.onSaveInstanceState(outState);

    }

    private void putParams() {
        Calendar c = Calendar.getInstance();
        int time = mTimeSpinner.getSelectedItemPosition() + 1;
        String wdayTime = String.valueOf(c.get(Calendar.DAY_OF_WEEK)) + (time < 10 ? "0" : StringUtil.NULL) + String.valueOf(time);
        String building = ((String) mBuildingSpinner.getSelectedItem()).split(StringUtil.SPACE)[0];

        params.put(BUILDING, building);
        params.put("wdayTime", wdayTime);
        params.put(OApiUtil.TERM, Semester.values()[mTermSpinner.getSelectedItemPosition()].code);
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


    private void initParamsTable() {
        String date = new SimpleDateFormat("yyyyMMdd", Locale.KOREAN).format(new Date());
        params.put(OApiUtil.API_KEY, OApiUtil.UOS_API_KEY);
        params.put(OApiUtil.YEAR, OApiUtil.getYear());
        params.put(BUILDING, StringUtil.NULL);
        params.put("dateFrom", date);
        params.put("dateTo", date);
        params.put("classRoomDiv", StringUtil.NULL);
        params.put("wdayTime", StringUtil.NULL);
        params.put("aplyPosbYn", "Y");
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mEmptyView.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onPostExecute() {
        super.onPostExecute();

        if (mAdapter.isEmpty())
            mEmptyView.setVisibility(View.VISIBLE);
    }

    private void execute() {
        execute(true, mRequest, this, this, true);
    }

    private final Request<ArrayList<EmptyClassRoomItem>> mRequest = new AbstractRequest<ArrayList<EmptyClassRoomItem>>() {
        @Override
        public ArrayList<EmptyClassRoomItem> get() throws Exception {
            putParams();

            HttpRequest.Builder<HttpURLConnection> requestBuilder = HttpRequest.Builder.newConnectionRequestBuilder(URL)
                    .setParams(params)
                    .setParamsEncoding(StringUtil.ENCODE_EUC_KR);

            if (params.get(BUILDING).equals("00")) {

                final String[] buildings = {
                        "01", "02", "03", "04", "05",
                        "06", "08", "09", "10", "11",
                        "13", "14", "15", "16", "17",
                        "18", "19", "20", "23", "24", "25", "33"};

                ArrayList<EmptyClassRoomItem> list = new ArrayList<>();

                for (String bd : buildings) {
                    params.put(BUILDING, bd);
                    list.addAll(
                            requestBuilder.build()
                                    .checkNetworkState(getActivity())
                                    .wrap(EMPTY_ROOM_PARSER)
                                    .get()
                    );
                }

                return list;

            } else {
                return requestBuilder.build()
                        .checkNetworkState(getActivity())
                        .wrap(EMPTY_ROOM_PARSER)
                        .get();
            }
        }
    };

    @Override
    public void onResult(ArrayList<EmptyClassRoomItem> result) {
        mClassRoomList.clear();
        mClassRoomList.addAll(result);
        mAdapter.notifyDataSetChanged();
        AppUtil.showToast(getActivity(), getString(R.string.search_found_amount, result.size()), true);

        mTermString = mTimeSpinner.getSelectedItem().toString().split(StringUtil.NEW_LINE)[1] + StringUtil.NEW_LINE + mTermSpinner.getSelectedItem();
        setSubtitleWhenVisible(mTermString);
    }

    @Override
    public void onError(Exception e) {
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

        mAdapter.sort(EmptyClassRoomItem.getComparator(field, isReverse));
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