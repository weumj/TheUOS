package com.uoscs09.theuos2.tab.subject;


import android.app.Activity;
import android.content.Context;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.nhaarman.listviewanimations.appearance.simple.AlphaInAnimationAdapter;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.annotation.AsyncData;
import com.uoscs09.theuos2.base.AbsProgressFragment;
import com.uoscs09.theuos2.customview.CustomHorizontalScrollView;
import com.uoscs09.theuos2.util.AppRequests;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.OApiUtil;

import java.util.ArrayList;
import java.util.Map;

import butterknife.Bind;
import butterknife.OnItemClick;
import mj.android.utils.task.Task;

public class TabSearchSubjectFragment2 extends AbsProgressFragment<ArrayList<SubjectItem2>>
        implements AdapterView.OnItemSelectedListener {
    private AlertDialog mSearchDialog;
    private EditText mSearchEditText;
    private Spinner mDialogSpinner1, mDialogSpinner2, mDialogSpinner3, mDialogSpinner4, mDialogTermSpinner, mDialogYearSpinner;
    private final int[] selections = new int[4];
    /*@ReleaseWhenDestroy
    private View mTitleLayout;
    */
    private TextView[] textViews;
    private View[] tabStrips;

    @Bind(R.id.tab_search_subject_scrollview)
    CustomHorizontalScrollView mScrollView;
    @Bind(R.id.tab_search_subject_empty_view)
    View mEmptyView;
    @Bind(R.id.tab_search_subject_list_view)
    ListView mListView;

    private String mSearchConditionString;
    private int mTabSelection = 1;
    private boolean isInverse = false;
    private boolean mIsScrollViewScrolling = false;

    @AsyncData
    private ArrayList<SubjectItem2> mSubjectList;

    private SubjectAdapter2 mSubjectAdapter;
    private AlphaInAnimationAdapter mAminAdapter;

    private final CoursePlanDialogFragment mCoursePlanDialogFragment = new CoursePlanDialogFragment();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Context context = getActivity();
        View dialogView = View.inflate(context, R.layout.dialog_search_subject, null);
        initDialog(dialogView);

        mSearchEditText = (EditText) dialogView.findViewById(R.id.search_subj_editText1);
        mDialogSpinner1 = (Spinner) dialogView.findViewById(R.id.search_subj_spinner1);
        mDialogSpinner2 = (Spinner) dialogView.findViewById(R.id.search_subj_spinner2);
        mDialogSpinner3 = (Spinner) dialogView.findViewById(R.id.search_subj_spinner3);
        mDialogSpinner4 = (Spinner) dialogView.findViewById(R.id.search_subj_spinner4);

        mDialogSpinner1.setAdapter(createArrayAdapter(getActivity(), R.array.search_subj_opt1));
        mDialogSpinner1.setOnItemSelectedListener(this);
        mDialogSpinner2.setOnItemSelectedListener(this);
        mDialogSpinner3.setOnItemSelectedListener(this);

        mDialogTermSpinner = (Spinner) dialogView.findViewById(R.id.search_subj_spinner_term);
        mDialogYearSpinner = (Spinner) dialogView.findViewById(R.id.search_subj_spinner_year);
        mDialogYearSpinner.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, OApiUtil.getYears()));

        // current year
        mDialogYearSpinner.setSelection(2);

        //ViewGroup mToolBarParent = (ViewGroup) getActivity().findViewById(R.id.toolbar_parent);

        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayout() {
        return R.layout.tab_search_subj;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = getActivity();

        //mTitleLayout = rootView.findViewById(R.id.tab_search_subject_head_layout);
        //mTitleLayout.setVisibility(View.INVISIBLE);

        mEmptyView.findViewById(R.id.empty1).setOnClickListener(v -> {
            sendEmptyViewClickEvent();
            mSearchDialog.show();
        });

        if (savedInstanceState != null) {
            mSubjectList = savedInstanceState.getParcelableArrayList("mSubjectList");
            mSearchConditionString = savedInstanceState.getString("action");
        } else {
            mSubjectList = new ArrayList<>();
        }

        mSubjectAdapter = new SubjectAdapter2(context, mSubjectList);
        mAminAdapter = new AlphaInAnimationAdapter(mSubjectAdapter);
        mAminAdapter.setAbsListView(mListView);

        mListView.setAdapter(mAminAdapter);
        mListView.setEmptyView(mEmptyView);

        final CustomHorizontalScrollView mTabParent = (CustomHorizontalScrollView) LayoutInflater.from(getActivity()).inflate(R.layout.view_tab_search_subject_toolbar_menu, getToolbarParent(), false);
        mTabParent.setOnScrollListener((l, t, oldl, oldt) -> {
            if (!mIsScrollViewScrolling) {
                mIsScrollViewScrolling = true;

                mScrollView.scrollTo(l, 0);

                mIsScrollViewScrolling = false;
            }
        });

        mScrollView.setOnScrollListener((l, t, oldl, oldt) -> {
            if (!mIsScrollViewScrolling) {
                mIsScrollViewScrolling = true;

                mTabParent.scrollTo(l, 0);

                mIsScrollViewScrolling = false;
            }
        });

        registerTabParentView(mTabParent);

        int[] ids = {R.id.tab_search_subject_sub_dept1, R.id.tab_search_subject_sub_div1, R.id.tab_search_subject_no1, R.id.tab_search_subject_class_div1,
                R.id.tab_search_subject_sub_nm1, R.id.tab_search_subject_yr1, R.id.tab_search_subject_credit1, R.id.tab_search_subject_prof_nm1,
                R.id.tab_search_subject_class_nm1, R.id.tab_search_subject_tlsn_cnt1, R.id.tab_search_subject_tlsn_limit1};
        textViews = new TextView[ids.length];
        tabStrips = new View[ids.length];

        int i = 0;
        ViewGroup rippleParent = (ViewGroup) mTabParent.findViewById(R.id.tab_search_subject_head_layout);
        for (int id : ids) {
            final int j = i;
            View ripple = rippleParent.getChildAt(i);
            ripple.setOnClickListener(v -> onTabClick(j));

            textViews[i] = (TextView) ripple.findViewById(id);
            tabStrips[i++] = ripple.findViewById(R.id.tab_tab_strip);

        }

        registerProgressView(view.findViewById(R.id.progress_layout));
    }


    private void onTabClick(int index) {
        if (mSubjectList.isEmpty()) {
            return;
        }

        if (mTabSelection != -1) {
            textViews[mTabSelection].setCompoundDrawables(null, null, null, null);
            tabStrips[mTabSelection].setVisibility(View.INVISIBLE);
        }

        /*

        switch (id) {
            case R.id.tab_search_subject_sub_dept1:
                field = 0;
                break;
            case R.id.tab_search_subject_sub_div1:
                field = 1;
                break;
            case R.id.tab_search_subject_no1:
                field = 2;
                break;
            case R.id.tab_search_subject_class_div1:
                field = 3;
                break;
            case R.id.tab_search_subject_sub_nm1:
                field = 4;
                break;
            case R.id.tab_search_subject_yr1:
                field = 5;
                break;
            case R.id.tab_search_subject_credit1:
                field = 6;
                break;
            case R.id.tab_search_subject_prof_nm1:
                field = 7;
                break;
            case R.id.tab_search_subject_class_nm1:
                field = 8;
                break;
            case R.id.tab_search_subject_tlsn_cnt1:
                field = 9;
                break;
            case R.id.tab_search_subject_tlsn_limit1:
                field = 10;
                break;
            default:
                return;
        }
        */

        isInverse = index == mTabSelection && !isInverse;
        mTabSelection = index;


        sendClickEvent("sort", index);

        textViews[index].setCompoundDrawablesWithIntrinsicBounds(
                AppUtil.getAttrValue(getActivity(), isInverse ? R.attr.menu_theme_ic_action_navigation_arrow_drop_up : R.attr.menu_theme_ic_action_navigation_arrow_drop_down), 0, 0, 0);
        tabStrips[index].setVisibility(View.VISIBLE);

        mSubjectAdapter.sort(SubjectItem2.getComparator(index, isInverse));
        mAminAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("mSubjectList", mSubjectList);
        outState.putString("action", mSearchConditionString);
        super.onSaveInstanceState(outState);
    }

    /*
    @Override
    public void onResume() {
        super.onResume();
        if (!mSubjectList.isEmpty())
            mTitleLayout.setVisibility(View.VISIBLE);
    }
    */

    @OnItemClick(R.id.tab_search_subject_list_view)
    public void onSubjectClicked(View v, int pos) {
        if (!mCoursePlanDialogFragment.isAdded()) {
            mCoursePlanDialogFragment.setSubjectItem(mSubjectList.get(pos));
            mCoursePlanDialogFragment.show(getFragmentManager(), "course");
        }
    }

    private void initDialog(View v) {
        mSearchDialog = new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.title_tab_search_subject)
                .setMessage(R.string.tab_book_subject_opt)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    InputMethodManager ipm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    ipm.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
                    execute();
                })
                .create();
    }

    private void execute() {
        mEmptyView.setVisibility(View.INVISIBLE);

        boolean culture = mDialogSpinner1.getSelectedItemPosition() == 0;
        Task<ArrayList<SubjectItem2>> request;

        String year = mDialogYearSpinner.getSelectedItem().toString();
        int term = mDialogTermSpinner.getSelectedItemPosition();
        String subjectName = mSearchEditText.getText().toString();

        if (culture)
            request = AppRequests.Subjects.requestCulture(year, term, getCultSubjectDiv(mDialogSpinner2.getSelectedItemPosition()), subjectName);
        else {
            Map<String, String> additionalParams;
            switch (selections[1]) {
                case R.array.search_subj_major_2_0_0:
                    additionalParams = getMajorDeptDiv(mDialogSpinner3.getSelectedItemPosition(), mDialogSpinner4.getSelectedItemPosition());
                    break;

                default:
                    additionalParams = getMajorDeptDiv2(selections[1], mDialogSpinner4.getSelectedItemPosition());
                    break;

            }
            request = AppRequests.Subjects.requestMajor(year, term, additionalParams, subjectName);
        }

        execute(request,
                result -> {
                    mSubjectAdapter.clear();
                    mSubjectAdapter.addAll(result);
                    mAminAdapter.reset();
                    mAminAdapter.notifyDataSetChanged();

                    //mSubjectAdapter.notifyDataSetChanged();

                    mScrollView.scrollTo(0, 0);
                    getTabParentView().scrollTo(0, 0);

                    if (mSubjectAdapter.isEmpty()) {
                        mEmptyView.setVisibility(View.VISIBLE);
                    }
            /*
            if (result.isEmpty()) {
                mTitleLayout.setVisibility(View.INVISIBLE);
            } else {
                mTitleLayout.setVisibility(View.VISIBLE);
            }
            */

                    AppUtil.showToast(getActivity(), getString(R.string.search_found_amount, result.size()), true);

                    mSearchConditionString = mDialogYearSpinner.getSelectedItem().toString()
                            + " / "
                            + mDialogTermSpinner.getSelectedItem().toString();
                    setSubtitleWhenVisible(mSearchConditionString);
                },
                this::simpleErrorRespond
        );
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
                sendClickEvent("option menu : search");
                mSearchDialog.show();
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
                ArrayAdapter<CharSequence> aa = createArrayAdapter(getActivity(), array);
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
                            break;
                        default:
                            return;
                    }
                    // 두 번째 spinner의 선택이 "대학"이 아닌경우
                    // 네 번째 spinner의 항목을 변경한다.
                    if (pos > 0) {
                        ArrayAdapter<CharSequence> aaa = createArrayAdapter(getActivity(), array2);
                        mDialogSpinner4.setAdapter(aaa);
                        selections[2] = array2;
                    }
                }
                // 위에서 판별한 결과에 따라 세 번째 spinner의 항목을 변경한다.
                selections[1] = array;
                ArrayAdapter<CharSequence> aa = createArrayAdapter(getActivity(), array);
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
                ArrayAdapter<CharSequence> aa = createArrayAdapter(getActivity(), array);
                mDialogSpinner4.setAdapter(aa);
                break;
            }
            default:
                break;
        }
    }

    private static ArrayAdapter<CharSequence> createArrayAdapter(Context context, int arrayResource) {
        return ArrayAdapter.createFromResource(context, arrayResource, R.layout.support_simple_spinner_dropdown_item);
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

    private static String getCultSubjectDiv(int subjectDiv) {
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

    private static ArrayMap<String, String> getMajorDeptDiv(int deptDiv, int subDept) {
        ArrayMap<String, String> table = new ArrayMap<>(3);
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
                    default:
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

                    default:
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

                    default:
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

    private static ArrayMap<String, String> getMajorDeptDiv2(int deptDiv, int subDept) {
        ArrayMap<String, String> table = new ArrayMap<>(3);
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
    protected CharSequence getSubtitle() {
        return mSearchConditionString;
    }

    @NonNull
    @Override
    public String getScreenNameForTracker() {
        return "TabSearchSubjectFragment";
    }
}
