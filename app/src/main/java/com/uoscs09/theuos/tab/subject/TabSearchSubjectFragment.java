package com.uoscs09.theuos.tab.subject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.AbsDrawableProgressFragment;
import com.uoscs09.theuos.common.impl.annotaion.AsyncData;
import com.uoscs09.theuos.common.impl.annotaion.ReleaseWhenDestroy;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.OApiUtil;
import com.uoscs09.theuos.common.util.OApiUtil.Term;
import com.uoscs09.theuos.common.util.StringUtil;
import com.uoscs09.theuos.http.HttpRequest;
import com.uoscs09.theuos.http.parse.ParseSubject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Hashtable;

public class TabSearchSubjectFragment extends AbsDrawableProgressFragment<ArrayList<SubjectItem>> implements
        OnItemClickListener, OnItemSelectedListener, View.OnClickListener {
    @ReleaseWhenDestroy
    private SubjectAdapter adapter;
    @AsyncData
    protected ArrayList<SubjectItem> mSubjectList;
    protected Hashtable<String, String> params;
    @ReleaseWhenDestroy
    protected AlertDialog ad;
    @ReleaseWhenDestroy
    protected EditText et;
    @ReleaseWhenDestroy
    private Spinner mDialogSpinner1, mDialogSpinner2, mDialogSpinner3,
            mDialogSpinner4, mDialogTermSpinner, mDialogYearSpinner;
    private int[] selections = new int[4];
    @ReleaseWhenDestroy
    private View mTitleLayout;
    @ReleaseWhenDestroy
    private TextView[] textViews;
    private String mSearchConditionString;
    private int sortFocusViewId;
    private boolean isInverse = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        params = new Hashtable<>();
        Context context = getActivity();
        View dialogView = View.inflate(context, R.layout.dialog_search_subject, null);
        initDialog(dialogView);

        et = (EditText) dialogView.findViewById(R.id.search_subj_editText1);
        mDialogSpinner1 = (Spinner) dialogView.findViewById(R.id.search_subj_spinner1);
        mDialogSpinner2 = (Spinner) dialogView.findViewById(R.id.search_subj_spinner2);
        mDialogSpinner3 = (Spinner) dialogView.findViewById(R.id.search_subj_spinner3);
        mDialogSpinner4 = (Spinner) dialogView.findViewById(R.id.search_subj_spinner4);
        mDialogSpinner1.setAdapter(createArrayAdapter(R.array.search_subj_opt1));
        mDialogSpinner1.setOnItemSelectedListener(this);
        mDialogSpinner2.setOnItemSelectedListener(this);
        mDialogSpinner3.setOnItemSelectedListener(this);

        mDialogTermSpinner = (Spinner) dialogView.findViewById(R.id.search_subj_spinner_term);
        mDialogYearSpinner = (Spinner) dialogView.findViewById(R.id.search_subj_spinner_year);
        mDialogYearSpinner.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, OApiUtil.getYears()));
        // current year
        mDialogYearSpinner.setSelection(2);

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Context context = getActivity();

        final View rootView = inflater.inflate(R.layout.tab_search_subj, container, false);
        mTitleLayout = rootView.findViewById(R.id.tab_search_subject_head_layout);
        mTitleLayout.setVisibility(View.INVISIBLE);
        View empty = rootView.findViewById(R.id.tab_search_subject_empty_view);
        empty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ad.show();
            }
        });

        if (savedInstanceState != null) {
            mSubjectList = savedInstanceState.getParcelableArrayList("mSubjectList");
            mSearchConditionString = savedInstanceState.getString("action");
        } else {
            mSubjectList = new ArrayList<>();
        }

        ListView listView = (ListView) rootView.findViewById(R.id.tab_search_subject_list_view);
        adapter = new SubjectAdapter(context, R.layout.list_layout_subject, mSubjectList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setEmptyView(empty);

        int[] ids = {R.id.tab_search_subject_sub_dept1,
                R.id.tab_search_subject_sub_div1, R.id.tab_search_subject_no1,
                R.id.tab_search_subject_class_div1,
                R.id.tab_search_subject_sub_nm1, R.id.tab_search_subject_yr1,
                R.id.tab_search_subject_credit1,
                R.id.tab_search_subject_prof_nm1,
                R.id.tab_search_subject_class_nm1,
                R.id.tab_search_subject_tlsn_cnt1,
                R.id.tab_search_subject_tlsn_limit1};
        textViews = new TextView[ids.length];
        int i = 0;
        for (int id : ids) {
            textViews[i] = (TextView) rootView.findViewById(id);
            textViews[i++].setOnClickListener(this);
        }
        return rootView;
    }

    // for titles
    @Override
    public void onClick(View v) {
        if (mSubjectList.isEmpty()) {
            return;
        }
        int field;
        int id = v.getId();
        int bias = 0;
        for (TextView tv : textViews) {
            tv.setCompoundDrawables(null, null, null, null);
        }

        isInverse = id == sortFocusViewId && !isInverse;
        sortFocusViewId = id;
        switch (id) {
            case R.id.tab_search_subject_sub_dept1:
                field = 0;
                break;
            case R.id.tab_search_subject_sub_div1:
                field = 1;
                break;
            case R.id.tab_search_subject_no1:
                field = 3;
                bias = -1;
                break;
            case R.id.tab_search_subject_class_div1:
                field = 4;
                bias = -1;
                break;
            case R.id.tab_search_subject_sub_nm1:
                field = 5;
                bias = -1;
                break;
            case R.id.tab_search_subject_yr1:
                field = 6;
                bias = -1;
                break;
            case R.id.tab_search_subject_credit1:
                field = 7;
                bias = -1;
                break;
            case R.id.tab_search_subject_prof_nm1:
                field = 8;
                bias = -1;
                break;
            case R.id.tab_search_subject_class_nm1:
                field = 10;
                bias = -2;
                break;
            case R.id.tab_search_subject_tlsn_cnt1:
                field = 11;
                bias = -2;
                break;
            case R.id.tab_search_subject_tlsn_limit1:
                field = 12;
                bias = -2;
                break;
            default:
                return;
        }

        Drawable d = getResources().getDrawable(
                AppUtil.getStyledValue(getActivity(), isInverse ? R.attr.ic_navigation_collapse : R.attr.ic_navigation_expand));
        textViews[field + bias].setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);

        adapter.sort(SubjectItem.getComparator(field, isInverse));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("mSubjectList", mSubjectList);
        outState.putString("action", mSearchConditionString);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mSubjectList.isEmpty())
            mTitleLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onItemClick(AdapterView<?> ad, View v, int pos, long id) {
        SubjectInfoDialFrag.showDialog(getFragmentManager(),
                (SubjectItem) ad.getItemAtPosition(pos), getActivity(),
                mDialogTermSpinner.getSelectedItemPosition(),
                mDialogYearSpinner.getSelectedItem().toString());
    }

    private void initDialog(View v) {
        ad = new MaterialDialog.Builder(getActivity())
                .customView(v, true)
                .title(R.string.title_tab_search_subject)
                .content(R.string.tab_book_subject_opt)
                .positiveText(android.R.string.ok)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        InputMethodManager ipm = (InputMethodManager) getActivity()
                                .getSystemService(
                                        Activity.INPUT_METHOD_SERVICE);
                        ipm.hideSoftInputFromWindow(
                                et.getWindowToken(), 0);
                        excute();
                    }
                })
                .build();
    }

    @Override
    public void onTransactResult(ArrayList<SubjectItem> result) {
        adapter.clear();
        adapter.addAll(result);
        adapter.notifyDataSetChanged();
        if (result.isEmpty()) {
            mTitleLayout.setVisibility(View.INVISIBLE);
        } else {
            mTitleLayout.setVisibility(View.VISIBLE);
        }
        AppUtil.showToast(getActivity(), String.valueOf(result.size())
                + getString(R.string.search_found), true);

        mSearchConditionString = mDialogYearSpinner.getSelectedItem()
                .toString()
                + " / "
                + mDialogTermSpinner.getSelectedItem().toString();
        setSubtitleWhenVisible(mSearchConditionString);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ArrayList<SubjectItem> call() throws Exception {
        String query;
        params.clear();
        params.put(OApiUtil.API_KEY, OApiUtil.UOS_API_KEY);
        params.put(OApiUtil.YEAR, mDialogYearSpinner.getSelectedItem()
                .toString());
        params.put(OApiUtil.TERM,
                OApiUtil.getTermCode(Term.values()[mDialogTermSpinner
                        .getSelectedItemPosition()]));
        switch (mDialogSpinner1.getSelectedItemPosition()) {
            default:
            case 0:// 교양
                query = "http://wise.uos.ac.kr/uosdoc/api.ApiUcrCultTimeInq.oapi";
                params.put(
                        "subjectDiv",
                        getCultSubjectDiv(mDialogSpinner2.getSelectedItemPosition()));
                break;
            case 1:// 전공
                query = "http://wise.uos.ac.kr/uosdoc/api.ApiUcrMjTimeInq.oapi";
                switch (selections[1]) {
                    case R.array.search_subj_major_2_0_0:
                        params.putAll(getMajorDeptDiv(
                                mDialogSpinner3.getSelectedItemPosition(),
                                mDialogSpinner4.getSelectedItemPosition()));
                        break;
                    default:
                        params.putAll(getMajorDeptDiv2(selections[1],
                                mDialogSpinner4.getSelectedItemPosition()));
                        break;
                }
                break;
        }
        params.put("subjectNm", URLEncoder.encode(et.getText().toString(),
                StringUtil.ENCODE_EUC_KR));
        String body = HttpRequest.getBody(query, StringUtil.ENCODE_EUC_KR, params);
        return new ParseSubject(body).parse();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.tab_search_subject, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                ad.show();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long arg3) {
        switch (arg0.getId()) {
            case R.id.search_subj_spinner1: {
                int array;
                switch (pos) {
                    case 0:
                        array = R.array.search_cult_1;
                        break;
                    case 1:
                        array = R.array.search_subj_major_1_0;
                        break;
                    default:
                        return;
                }
                selections[0] = array;
                ArrayAdapter<CharSequence> aa = createArrayAdapter(array);
                mDialogSpinner2.setAdapter(aa);
                break;
            }
            case R.id.search_subj_spinner2: {
                int array, array2 = 0;
                if (mDialogSpinner1.getSelectedItemPosition() == 0) {
                    // 처음 spinner가 "교양"인 경우
                    mDialogSpinner4.setVisibility(View.INVISIBLE);
                    array = R.array.search_cult_2;
                } else {
                    // 처음 spinner가 "전공"인 경우
                    mDialogSpinner4.setVisibility(View.VISIBLE);
                    switch (pos) { // 두 번재 spinner의 위치
                        case 0: // "대학"
                            array = R.array.search_subj_major_2_0_0;
                            break;
                        case 1: // "대학원"
                            array = R.array.search_subj_major_2_0_1;
                            array2 = R.array.search_subj_major_3_0_1_0;
                            break;
                        case 2: // "세무전문대학원"
                            array = R.array.search_subj_major_2_0_2;
                            array2 = R.array.search_subj_major_3_0_2_0;
                            break;
                        case 3: // "디자인전문대학원"
                            array = R.array.search_subj_major_2_0_3;
                            array2 = R.array.search_subj_major_3_0_3_0;
                            break;
                        case 4: // "법학전문대학원"
                            array = R.array.search_subj_major_2_0_4;
                            array2 = R.array.search_subj_major_3_0_4_0;
                            break;
                        case 5: // "도시과학대학원"
                            array = R.array.search_subj_major_2_0_5;
                            array2 = R.array.search_subj_major_3_0_5_0;
                            break;
                        case 6: // "경영대학원"
                            array = R.array.search_subj_major_2_0_6;
                            array2 = R.array.search_subj_major_3_0_6_0;
                            break;
                        case 7: // "과학기술대학원"
                            array = R.array.search_subj_major_2_0_7;
                            array2 = R.array.search_subj_major_3_0_7_0;
                            break;
                        case 8: // "교육대학원"
                            array = R.array.search_subj_major_2_0_8;
                            array2 = R.array.search_subj_major_3_0_8_0;
                            break;
                        case 9: // "국제도시과학대학원"
                            array = R.array.search_subj_major_2_0_9;
                            array2 = R.array.search_subj_major_3_0_9_0;
                        default:
                            return;
                    }
                    // 두 번째 spinner의 선택이 "대학"이 아닌경우
                    // 네 번째 spinner의 항목을 변경한다.
                    if (pos > 0) {
                        ArrayAdapter<CharSequence> aaa = createArrayAdapter(array2);
                        mDialogSpinner4.setAdapter(aaa);
                        selections[2] = array2;
                    }
                }
                // 위에서 판별한 결과에 따라 세 번째 spinner의 항목을 변경한다.
                selections[1] = array;
                ArrayAdapter<CharSequence> aa = createArrayAdapter(array);
                mDialogSpinner3.setAdapter(aa);
                break;
            }
            case R.id.search_subj_spinner3: {
                int array;
                if (mDialogSpinner1.getSelectedItemPosition() == 0) {
                    // 첫 번째 spinner의 항목이 "교양" 인 경우 아무것도 하지 않는다.
                    return;
                } else {
                    // 첫 번째 spinner의 항목이 "전공" 인 경우
                    switch (mDialogSpinner2.getSelectedItemPosition()) {
                        case 0:// 두 번째 spinner의 항목이 "대학" 인 경우
                            switch (pos) {
                                case 0: // "정경대학"
                                    array = R.array.search_subj_major_3_0_0_0;
                                    break;
                                case 1:// "경영대학"
                                    array = R.array.search_subj_major_3_0_0_1;
                                    break;
                                case 2:// "공과대학"
                                    array = R.array.search_subj_major_3_0_0_2;
                                    break;
                                case 3:// "인문대학"
                                    array = R.array.search_subj_major_3_0_0_3;
                                    break;
                                case 4:// "자연과학대학"
                                    array = R.array.search_subj_major_3_0_0_4;
                                    break;
                                case 5:// "도시과학대학"
                                    array = R.array.search_subj_major_3_0_0_5;
                                    break;
                                case 6:// "예술체육대학"
                                    array = R.array.search_subj_major_3_0_0_6;
                                    break;
                                case 7:// "국제교육원"
                                    array = R.array.search_subj_major_3_0_0_7;
                                    break;
                                default:
                                    return;
                            }
                            break;
                        default:
                            // 두 번째 spinner의 항목이 "대학" 이 아닌 다른 항목이면
                            // 아무것도 하지 않는다 (이미 두 번째 spinner에서 처리)
                            return;
                    }
                }
                // 판별한 결과에 따라 네 번째 spinner의 항목들을 변경한다.
                selections[3] = array;
                ArrayAdapter<CharSequence> aa = createArrayAdapter(array);
                mDialogSpinner4.setAdapter(aa);
                break;
            }
            default:
                break;
        }
    }

    private ArrayAdapter<CharSequence> createArrayAdapter(int arrayResource) {
        return ArrayAdapter.createFromResource(getActivity(), arrayResource, R.layout.support_simple_spinner_dropdown_item);
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

    private String getCultSubjectDiv(int subjectDiv) {
        switch (subjectDiv) {
            default:
            case 0:
                return "A01";
            case 1:
                return "A02";
            case 2:
                return "A06";
            case 3:
                return "A07";
        }
    }

    private Hashtable<String, String> getMajorDeptDiv(int deptDiv, int subDept) {
        Hashtable<String, String> table = new Hashtable<>(3);
        switch (deptDiv) {
            case 0:// 정경대학
                table.put("deptDiv", "210");
                table.put("dept", "A201120212");
                switch (subDept) {
                    case 0:// 행정
                        table.put("subDept", "A201140214");
                        break;
                    case 1:// 국제관계
                        table.put("subDept", "A201150215");
                        break;
                    case 2:// 경제
                        table.put("subDept", "A201160216");
                        break;
                    case 3:// 사회복지
                        table.put("subDept", "A201170217");
                        break;
                    case 4:// 세무
                        table.put("subDept", "A201180218");
                        break;
                    case 5:// 법학
                        table.put("subDept", "A202200320");
                        break;
                }
                break;
            case 1:// 경영대학
                table.put("deptDiv", "210");
                table.put("dept", "A201130213");
                table.put("subDept", "A201190219");
                break;
            case 2:// 공과대학
                table.put("deptDiv", "220");
                table.put("dept", "A200110111");
                switch (subDept) {
                    case 0:
                        table.put("subDept", "A200160116");// -전자전기컴퓨터공학부
                        break;
                    case 1:
                        table.put("subDept", "A200130113");// -화학공학과
                        break;
                    case 2:
                        table.put("subDept", "A200170117");// -기계정보공학과
                        break;
                    case 3:
                        table.put("subDept", "A200180118");// -신소재공학과
                        break;
                    case 4:
                        table.put("subDept", "A200190119");// -토목공학과
                        break;
                    case 5:
                        table.put("subDept", "A200200120");// -컴퓨터과학부
                        break;
                }
                break;
            case 3:// 인문대학
                table.put("deptDiv", "210");
                table.put("dept", "A200220122");
                switch (subDept) {
                    case 0:
                        table.put("subDept", "A200230123");// -영어영문학과
                        break;
                    case 1:
                        table.put("subDept", "A200240124");// -국어국문학과
                        break;
                    case 2:
                        table.put("subDept", "A200250125");// -국사학과
                        break;
                    case 3:
                        table.put("subDept", "A200260126");// -철학과
                        break;
                    case 4:
                        table.put("subDept", "A201020202");// -중국어문화학과
                        break;
                }
                break;
            case 4:// 자연과학대학
                table.put("deptDiv", "210");
                table.put("dept", "A200280128");
                switch (subDept) {
                    case 0:
                        table.put("subDept", "A200310131");// -수학과
                        break;
                    case 1:
                        table.put("subDept", "A200300130");// -통계학과
                        break;
                    case 2:
                        table.put("subDept", "A200320132");// -물리학과
                        break;
                    case 3:
                        table.put("subDept", "A200330133");// -생명과학과
                        break;
                    case 4:
                        table.put("subDept", "A200290129");// -환경원예학과
                        break;
                }
                break;
            case 5:// 도시과학대학
                table.put("deptDiv", "210");
                table.put("dept", "A200370137");
                switch (subDept) {
                    case 0:
                        table.put("subDept", "A200380138");// -도시행정학과
                        break;
                    case 1:
                        table.put("subDept", "A200400140");// -도시사회학과
                        break;
                    case 2:
                        table.put("subDept", "A200890189");// -건축학전공
                        break;
                    case 3:
                        table.put("subDept", "A200900190");// -건축공학전공
                        break;
                    case 4:
                        table.put("subDept", "A200490149");// -도시공학과
                        break;
                    case 5:
                        table.put("subDept", "A200500150");// -교통공학과
                        break;
                    case 6:
                        table.put("subDept", "A200510151");// -조경학과
                        break;
                    case 7:
                        table.put("subDept", "A200450145");// -환경공학부
                        break;
                    case 8:
                        table.put("subDept", "A201000200");// -공간정보공학과
                        break;
                    case 9:
                        table.put("subDept", "A201010201");// -소방방재학과
                        break;
                }
                break;
            case 6:// 예술체육대학
                table.put("deptDiv", "210");
                table.put("dept", "A200590159");
                switch (subDept) {
                    case 0:
                        table.put("subDept", "A200810181");// -공업디자인전공
                        break;
                    case 1:
                        table.put("subDept", "A200820182");// -시각디자인전공
                        break;
                    case 2:
                        table.put("subDept", "A200610161");// -환경조각학과
                        break;
                    case 3:
                        table.put("subDept", "A200620162");// -음악학과
                        break;
                    case 4:
                        table.put("subDept", "A200540154");// -스포츠과학과
                        break;
                }
                break;
            case 7:// 국제교육원
                table.put("deptDiv", "210");
                table.put("dept", "A201100210");
                table.put("subDept", "A201110211");
                break;
        }
        return table;
    }

    private Hashtable<String, String> getMajorDeptDiv2(int deptDiv, int subDept) {
        Hashtable<String, String> table = new Hashtable<>(3);
        switch (deptDiv) {
            case R.array.search_subj_major_2_0_1:// 대학원
                table.put("deptDiv", "310");
                table.put("dept", "A300010101");
                switch (subDept) {
                    case 0:// 도시행정학과
                        table.put("subDept", "A300030103");
                        break;
                    case 1:// 행정학과
                        table.put("subDept", "A300040104");
                        break;
                    case 2:// -사회복지학과
                        table.put("subDept", "A300320132");
                        break;
                    case 3:// -도시사회학과
                        table.put("subDept", "A300360136");
                        break;
                    case 4:// -법학과
                        table.put("subDept", "A300050105");
                        break;
                    case 5:// -국제관계학과
                        table.put("subDept", "A300370137");
                        break;
                    case 6:// -경영학과
                        table.put("subDept", "A300060106");
                        break;
                    case 7:// -경제학과
                        table.put("subDept", "A300070107");
                        break;
                    case 8:// -국어국문학과
                        table.put("subDept", "A300270127");
                        break;
                    case 9:// -영어영문학과
                        table.put("subDept", "A300260126");
                        break;
                    case 10:// -국사학과
                        table.put("subDept", "A300280128");
                        break;
                    case 11:// -철학과
                        table.put("subDept", "A300310131");
                        break;
                    case 12:// -조경학과
                        table.put("subDept", "A300200120");
                        break;
                    case 13:// -환경원예학과
                        table.put("subDept", "A300210121");
                        break;
                    case 14:// -컴퓨터과학과
                        table.put("subDept", "A302160316");
                        break;
                    case 15:// 물리학과
                        table.put("subDept", "A300240124");
                        break;
                    case 16:// -생명과학과
                        table.put("subDept", "A300250125");
                        break;
                    case 17:// -토목공학과
                        table.put("subDept", "A300110111");
                        break;
                    case 18:// --건축공학과
                        table.put("subDept", "A300120112");
                        break;
                    case 19:// --건축학과
                        table.put("subDept", "A300410141");
                        break;
                    case 20:// A300130113-환경공학과
                        table.put("subDept", "A300130113");
                        break;
                    case 21:// -화학공학과
                        table.put("subDept", "A300140114");
                        break;
                    case 22:// -도시공학과
                        table.put("subDept", "A300160116");
                        break;
                    case 23:// -교통공학과
                        table.put("subDept", "A300170117");
                        break;
                    case 24:// - -신소재공학과
                        table.put("subDept", "A300390139");
                        break;
                    case 25:// -기계정보공학과
                        table.put("subDept", "A300340134");
                        break;
                    case 26:// -공간정보공학과
                        table.put("subDept", "A301860286");
                        break;
                    case 27:// 환경조각학과
                        table.put("subDept", "A300420142");
                        break;
                    case 28:// -전자전기컴퓨터공학과
                        table.put("subDept", "A302040304");
                        break;
                    case 29:// -음악학과
                        table.put("subDept", "A301940294");
                        break;
                    case 30:// -스포츠과학과
                        table.put("subDept", "A302310331");
                        break;
                    default:
                        return null;
                }
                break;
            case R.array.search_subj_major_2_0_2:// 세무전문대학원
                table.put("deptDiv", "310");
                table.put("dept", "A300430143");
                table.put("subDept", "A302100310");
                break;
            case R.array.search_subj_major_2_0_3:// 디자인
                table.put("deptDiv", "310");
                table.put("dept", "A300500150");
                table.put("subDept", "A300520152");
                break;
            case R.array.search_subj_major_2_0_4:// 법학
                table.put("deptDiv", "310");
                table.put("dept", "A302020302");
                table.put("subDept", "A302030303");
                break;
            case R.array.search_subj_major_2_0_5:// 도시
                table.put("deptDiv", "310");
                table.put("dept", "A300570157");
                switch (subDept) {
                    case 0:// 도시행정학과
                        table.put("subDept", "A300590159");
                        break;
                    case 1:// 방재공학과
                        table.put("subDept", "A300720172");
                        break;
                    case 2:// -사회복지학과
                        table.put("subDept", "A300750175");
                        break;
                    case 3:// -교통관리학과
                        table.put("subDept", "A300640164");
                        break;
                    case 4:// -건축공학과
                        table.put("subDept", "A300690169");
                        break;
                    case 5:// -조경학과
                        table.put("subDept", "A300700170");
                        break;
                    case 6:// -환경공학과
                        table.put("subDept", "A300710171");
                        break;
                    case 7:// -부동산학과
                        table.put("subDept", "A301840284");
                        break;
                    case 8:// -관광문화학과
                        table.put("subDept", "A301900290");
                        break;
                    default:
                        return null;
                }
                break;
            case R.array.search_subj_major_2_0_6:// 경영
                table.put("deptDiv", "310");
                table.put("dept", "A300760176");
                table.put("subDept", "A300780178");
                break;
            case R.array.search_subj_major_2_0_7:// 과학기술
                table.put("deptDiv", "301");
                table.put("dept", "A302190319");
                switch (subDept) {
                    case 0:// 화학공학과
                        table.put("subDept", "A302210321");
                        break;
                    case 1:// 신소재공학과
                        table.put("subDept", "A302230323");
                        break;
                    case 2:// 기계공학과
                        table.put("subDept", "A302240324");
                        break;
                    case 3:// 환경원예학과
                        table.put("subDept", "A302250325");
                        break;
                    case 4:// 토목공학과
                        table.put("subDept", "A302260326");
                        break;
                    case 5:// 전자전기공학과
                        table.put("subDept", "A302220322");
                        break;
                    default:
                        return null;
                }
                break;
            case R.array.search_subj_major_2_0_8:// 교육
                table.put("deptDiv", "321");
                table.put("dept", "A300960196");
                switch (subDept) {
                    case 0:// 국어교육전공
                        table.put("subDept", "A300980198");
                        break;
                    case 1:// 영어교육전공
                        table.put("subDept", "A300990199");
                        break;
                    case 2:// 수학교육전공
                        table.put("subDept", "A301000200");
                        break;
                    case 3:// 역사교육전공
                        table.put("subDept", "A301010201");
                        break;
                    case 4:// 교수학습/상담전공
                        table.put("subDept", "A302300330");
                        break;
                    default:
                        return null;
                }
                break;
            case R.array.search_subj_major_2_0_9:// 국제
                switch (subDept) {
                    case 0:// 글로벌건설경영학과
                        table.put("deptDiv", "310");
                        table.put("dept", "A303100410");
                        table.put("subDept", "A303110411");
                        break;
                    case 1:// 첨단녹색도시개발학과
                        table.put("deptDiv", "310");
                        table.put("dept", "A303100410");
                        table.put("subDept", "A303120412");
                        break;
                    case 2:// 국제도시개발프로그램
                        table.put("deptDiv", "321");
                        table.put("dept", "A302280328");
                        table.put("subDept", "A302290329");
                        break;
                    default:
                        return null;
                }
                break;
            default:
                return null;
        }
        return table;
    }

    @Override
    protected MenuItem getLoadingMenuItem(Menu menu) {
        return menu.findItem(R.id.action_search);
    }

    @Override
    protected CharSequence getSubtitle() {
        return mSearchConditionString;
    }
}
