package com.uoscs09.theuos.tab.timetable;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.method.TextKeyListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

import com.afollestad.materialdialogs.MaterialDialog;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.uoscs09.theuos.R;
import com.uoscs09.theuos.annotation.AsyncData;
import com.uoscs09.theuos.annotation.ReleaseWhenDestroy;
import com.uoscs09.theuos.base.AbsProgressFragment;
import com.uoscs09.theuos.common.AsyncLoader;
import com.uoscs09.theuos.common.AsyncLoader.OnTaskFinishedListener;
import com.uoscs09.theuos.common.ListViewBitmapWriteTask;
import com.uoscs09.theuos.http.TimeTableHttpRequest;
import com.uoscs09.theuos.parse.ParseTimetable;
import com.uoscs09.theuos.util.AppUtil;
import com.uoscs09.theuos.util.GraphicUtil;
import com.uoscs09.theuos.util.IOUtil;
import com.uoscs09.theuos.util.OApiUtil;
import com.uoscs09.theuos.util.OApiUtil.Semester;
import com.uoscs09.theuos.util.PrefUtil;
import com.uoscs09.theuos.util.StringUtil;

import org.apache.http.client.ClientProtocolException;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Callable;

@Deprecated
public class TabTimeTableFragment extends AbsProgressFragment<ArrayList<TimeTableItem>> implements View.OnClickListener {
    @ReleaseWhenDestroy
    private AlertDialog loginDialog;
    @AsyncData
    private ArrayList<TimeTableItem> mTimetableList;
    @ReleaseWhenDestroy
    protected ArrayAdapter<TimeTableItem> adapter;
    @ReleaseWhenDestroy
    protected View rootView;
    @ReleaseWhenDestroy
    protected EditText mWiseIdView, mWisePasswdView;
    @ReleaseWhenDestroy
    private Spinner mWiseTermSpinner;
    @ReleaseWhenDestroy
    private Spinner mWiseYearSpinner;
    @ReleaseWhenDestroy
    private AlertDialog deleteDialog;
    protected Semester semester;
    @ReleaseWhenDestroy
    protected ListView listView;
    @ReleaseWhenDestroy
    private TimeTableInfoCallback cb;
    private ViewGroup mToolBarParent;
    @ReleaseWhenDestroy
    private LinearLayout mTabParent;

    private final ParseTimetable mParser = new ParseTimetable();

    private boolean mIsOnLoad;
    private Map<String, Integer> colorTable;
    private String mTermText;
    private String mTimeTableYear;
    @ReleaseWhenDestroy
    private View emptyView;

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Context context = getActivity();
        if (savedInstanceState != null) {
            mTimetableList = savedInstanceState.getParcelableArrayList(IOUtil.FILE_TIMETABLE);
            colorTable = (Map<String, Integer>) savedInstanceState.getSerializable("color");
        } else {
            mTimetableList = new ArrayList<>();
            colorTable = new Hashtable<>();
        }
        cb = new TimeTableInfoCallback(context);

        PrefUtil pref = PrefUtil.getInstance(context);
        int termValue = pref.get("timetable_term", -1);
        if (termValue != -1) {
            semester = Semester.values()[termValue];
            cb.semester = semester;
        } else {
            semester = OApiUtil.getTerm();
            cb.semester = semester;
        }

        mTimeTableYear = pref.get("timetable_year", OApiUtil.getSemesterYear(semester));
        cb.year = mTimeTableYear;

        adapter = new TimetableAdapter(context, R.layout.list_layout_timetable, mTimetableList, colorTable, cb);

        initDialog();

        if (termValue != -1)
            setTermTextViewText(semester, context);
        super.onCreate(savedInstanceState);

        mToolBarParent = (ViewGroup) getActivity().findViewById(R.id.toolbar_parent);
        mTabParent = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.view_tab_timetable_toolbar_menu, mToolBarParent, false);
    }

    private void initDialog() {
        Context context = getActivity();
        View wiseDialogLayout = View.inflate(context, R.layout.dialog_timetable_wise_login, null);

        mWiseYearSpinner = (Spinner) wiseDialogLayout.findViewById(R.id.dialog_wise_spinner_year);
        mWiseYearSpinner.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, OApiUtil.getYears()));
        mWiseYearSpinner.setSelection(2);

        mWiseIdView = (EditText) wiseDialogLayout.findViewById(R.id.dialog_wise_id_input);
        mWisePasswdView = (EditText) wiseDialogLayout.findViewById(R.id.dialog_wise_passwd_input);
        mWiseTermSpinner = (Spinner) wiseDialogLayout.findViewById(R.id.dialog_wise_spinner_term);
        loginDialog = new MaterialDialog.Builder(context)
                .title(R.string.tab_timetable_wise_login_title)
                .customView(wiseDialogLayout, true)
                .positiveText(R.string.confirm)
                .positiveColorAttr(R.attr.colorPrimaryDark)
                .negativeText(R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        clearPassWd();
                    }

                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        String id = mWiseIdView.getText().toString();
                        Context context = getActivity();

                        if (id.equals("123456789") && mWisePasswdView.length() < 1) {
                            if (AppUtil.test) {
                                AppUtil.test = false;
                            } else {
                                AppUtil.test = true;
                                AppUtil.showToast(context, "test", isVisible());
                            }

                            PrefUtil.getInstance(context).put("test", AppUtil.test);
                            clearText();
                            return;
                        }

                        if (mWisePasswdView.length() < 1 || StringUtil.NULL.equals(id)) {
                            AppUtil.showToast(context, R.string.tab_timetable_wise_login_warning_null, true);
                            clearText();
                        } else {
                            execute();
                        }
                    }
                })
                .build();
    }


    private void clearText() {
        clearId();
        clearPassWd();
    }

    private void clearId() {
        if (mWiseIdView != null && mWiseIdView.length() > 0) {
            TextKeyListener.clear(mWiseIdView.getText());
        }
    }

    private void clearPassWd() {
        if (mWisePasswdView != null && mWisePasswdView.length() > 0) {
            TextKeyListener.clear(mWisePasswdView.getText());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(IOUtil.FILE_TIMETABLE, mTimetableList);
        outState.putSerializable("color", (Serializable) colorTable);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.tab_timetable, container, false);
        emptyView = rootView.findViewById(R.id.tab_timetable_empty);
        emptyView.setOnClickListener(this);
        listView = (ListView) rootView.findViewById(R.id.time_table_listView1);
        listView.setEmptyView(emptyView);
        listView.setAdapter(adapter);

        FloatingActionButton actionButton = (FloatingActionButton) rootView.findViewById(R.id.tab_timetable_action_btn);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRunning()) {
                    AppUtil.showToast(getActivity(), R.string.progress_ongoing, true);
                } else
                    loginDialog.show();
            }
        });

        registerProgressView(rootView.findViewById(R.id.progress_layout));
        return rootView;
    }

    @Override
    public void onResume() {
        if (adapter.isEmpty()) {
            mIsOnLoad = true;
            execute();
        }
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.tab_timetable, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
           /* case R.id.action_wise:
                if (isRunning()) {
                    AppUtil.showToast(getActivity(), R.string.progress_ongoing, true);
                } else
                    loginDialog.show();
                return true;
*/
            case R.id.action_delete:
                if (deleteDialog == null) {
                    initDeleteDialog();
                }

                deleteDialog.show();
                return true;

            case R.id.action_save:
                saveTimetableImage();
                return true;

            default:
                return false;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        addOrRemoveTabMenu(isVisibleToUser);
    }

    private void addOrRemoveTabMenu(boolean visible) {
        if (mToolBarParent == null || mTabParent == null)
            return;
        if (visible) {
            if (mTabParent.getParent() == null)
                mToolBarParent.addView(mTabParent);
        } else if (mToolBarParent.indexOfChild(mTabParent) > 0) {
            mToolBarParent.removeView(mTabParent);
        }
    }

    private void saveTimetableImage() {
        if (adapter.isEmpty()) {
            AppUtil.showToast(getActivity(), "시간표 정보가 없습니다.", true);
            return;
        }

        String dir = PrefUtil.getPictureSavedPath(getActivity()) + "timetable_" + mTimeTableYear + '_' + semester + '_' + String.valueOf(System.currentTimeMillis()) + ".png";

       TimeTableImageSaveTask task = new TimeTableImageSaveTask(dir, listView, mTabParent );
        task.execute();
    }

    @Override
    public void onTransactResult(ArrayList<TimeTableItem> result) {
        Context context = getActivity();
        if (result.isEmpty()) {
            if (!mIsOnLoad) {
                AppUtil.showToast(context, R.string.tab_timetable_wise_login_warning_fail, isMenuVisible());
            } else {
                emptyView.setVisibility(View.VISIBLE);
            }

            mIsOnLoad = false;
            return;
        }
        mIsOnLoad = false;
        mTimetableList.clear();
        mTimetableList.addAll(result);
        if (adapter != null)
            adapter.notifyDataSetChanged();

        setTermTextViewText(semester, context);
    }

    private void setTermTextViewText(Semester term, Context context) {
        mTermText = mTimeTableYear
                + " / "
                + context.getResources().getStringArray(R.array.terms)[term
                .ordinal()];
        setSubtitleWhenVisible(mTermText);
    }

    @Override
    protected void execute() {
        emptyView.setVisibility(View.INVISIBLE);

        super.execute();
    }

    @Override
    protected void onTransactPostExecute() {
        super.onTransactPostExecute();
        clearPassWd();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ArrayList<TimeTableItem> call() throws Exception {
        ArrayList<TimeTableItem> result;
        Context context = getActivity();

        // Fragment가 처음 Attach되었을 때, 파일에서 시간표을 읽어온다.
        if (mIsOnLoad) {
            result = readTimetable(context);

        } else {
            // 사용자가 WISE에 시간표 정보를 요청하였을 때
            semester = Semester.values()[mWiseTermSpinner.getSelectedItemPosition()];
            mTimeTableYear = mWiseYearSpinner.getSelectedItem().toString();

            String body = TimeTableHttpRequest.getHttpBodyPost(mWiseIdView.getText(), mWisePasswdView.getText(), semester, mTimeTableYear);
            cb.semester = semester;
            cb.year = mTimeTableYear;

            result = mParser.parse(body);

            PrefUtil pref = PrefUtil.getInstance(context);
            pref.put("timetable_term", semester.ordinal());
            pref.put("timetable_year", mTimeTableYear);
            TimeTableInfoCallback.clearAllAlarm(context);
            saveColorTable(context, makeColorTable(result));
        }

        // 시간표를 정상적으로 불러왔다면, 시간표를 저장하고,
        // 시간표의 과목과 과목의 색을 Mapping한다.
        if (!result.isEmpty()) {
            IOUtil.saveToFile(context, IOUtil.FILE_TIMETABLE, Activity.MODE_PRIVATE, result);
            colorTable.putAll(getColorTable(result, context));
        }
        return result;
    }

    @Override
    public void exceptionOccured(Exception e) {
        if (e instanceof ClientProtocolException || e instanceof NullPointerException) {
            AppUtil.showToast(getActivity(), R.string.tab_timetable_wise_login_warning_fail, isMenuVisible());
        } else {
            super.exceptionOccured(e);
        }
    }

    /**
     * 시간표 정보를 파일로부터 읽어온다.
     *
     * @return 시간표 정보 list, 파일이 없다면 빈 list
     */
    public static ArrayList<TimeTableItem> readTimetable(Context context) {
        ArrayList<TimeTableItem> list = IOUtil.readFromFileSuppressed(context, IOUtil.FILE_TIMETABLE);
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tab_timetable_empty:
                loginDialog.show();
                break;
            default:
                break;
        }
    }

    /**
     * 주어진 시간표정보를 통해 시간표 각 과목과 컬러를 mapping하는 Map을 작성한다.
     *
     * @param timetable 시간표
     * @return 과목이름이 Key이고, Value가 컬러를 가리키는 Integer인 Map<br>
     * * 컬러는 단순한 정수이며, AppUtil을 통해 Color integer를 얻어와야 한다.
     */
    public static Hashtable<String, Integer> makeColorTable(ArrayList<TimeTableItem> timetable) {
        Hashtable<String, Integer> table = new Hashtable<>();
        final int size = timetable.size();
        String name;
        TimeTableItem item;
        String[] array;
        int j = 0, h;
        for (int i = 0; i < size; i++) {
            item = timetable.get(i);
            array = new String[]{item.mon, item.tue, item.wed, item.thr, item.fri, item.sat};
            for (h = 0; h < array.length; h++) {
                name = OApiUtil.getSubjectName(array[h]);
                if (!name.equals(StringUtil.NULL) && !name.equals(array[h]) && !table.containsKey(name)) {
                    table.put(name, j++);
                }
            }
        }
        return table;
    }

    /**
     * 주어진 시간표정보를 통해 시간표 각 과목과 컬러를 mapping하는 Map을 파일에서 읽어오거나 작성한다.
     *
     * @param timetable 시간표
     * @return 시간표의 각 과목과 컬러를 mapping하는 Map
     */
    public static Hashtable<String, Integer> getColorTable(ArrayList<TimeTableItem> timetable, Context context) {
        Hashtable<String, Integer> table = readColorTableFromFile(context);

        if (table == null || table.size() == 0) {
            table = makeColorTable(timetable);
            saveColorTable(context, table);
        }
        return table;
    }

    /**
     * 주어진 시간표 컬러 Map을 파일로 저장한다.
     *
     * @param colorTable color map
     */
    public static void saveColorTable(Context context, Hashtable<String, Integer> colorTable) {
        IOUtil.saveToFileAsync(context, IOUtil.FILE_COLOR_TABLE, Activity.MODE_PRIVATE, colorTable, null);
    }

    /**
     * color map을 파일로 부터 읽어온다.
     */
    public static Hashtable<String, Integer> readColorTableFromFile(Context context) {
        return IOUtil.readFromFileSuppressed(context, IOUtil.FILE_COLOR_TABLE);
    }

    private void initDeleteDialog() {
        deleteDialog = new MaterialDialog.Builder(getActivity())
                .content(R.string.confirm_delete)
                .positiveText(android.R.string.ok)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);

                        AsyncLoader.excute(new Callable<Boolean>() {
                            @Override
                            public Boolean call() throws Exception {
                                boolean b = getActivity().deleteFile(IOUtil.FILE_TIMETABLE);
                                if (b) {
                                    getActivity().deleteFile(IOUtil.FILE_COLOR_TABLE);
                                }
                                return b;
                            }
                        }, new OnTaskFinishedListener() {
                            @Override
                            public void onTaskFinished(boolean isExceptionOccurred, Object data) {
                                boolean result = (Boolean) data;
                                if (!isExceptionOccurred && result) {
                                    adapter.clear();
                                    adapter.notifyDataSetChanged();
                                    mTimetableList.clear();

                                    Context context = getActivity();
                                    AppUtil.showToast(context, R.string.execute_delete, isVisible());

                                    TimeTableInfoCallback.clearAllAlarm(context);
                                    PrefUtil.getInstance(context).put("timetable_term", -1);
                                    mTermText = StringUtil.NULL;

                                } else {
                                    AppUtil.showToast(getActivity(), R.string.file_not_found, isMenuVisible());
                                }
                            }
                        });
                    }
                })
                .negativeText(R.string.cancel)
                .build();
    }

    @Override
    protected CharSequence getSubtitle() {
        return mTermText;
    }

    private static class TimeTableImageSaveTask extends ListViewBitmapWriteTask{
        private WeakReference<View> titleViewRef;
        public TimeTableImageSaveTask(String fileName, ListView listView, View titleView) {
            super(fileName, listView);
            titleViewRef = new WeakReference<>(titleView);
        }

        @Override
        public Bitmap getBitmap() {
            Bitmap capture = null, titleBitmap = null, bitmap;
            View title = null;
            try {
                capture = super.getBitmap();
                title = titleViewRef.get();

                title.setDrawingCacheEnabled(true);
                title.buildDrawingCache(true);
                titleBitmap = title.getDrawingCache(true);
                if (titleBitmap == null)
                    titleBitmap = GraphicUtil.createBitmapFromView(title);
                bitmap = GraphicUtil.merge(titleBitmap, capture);

                return bitmap;
            } finally {
                if (capture != null)
                    capture.recycle();
                if (titleBitmap != null)
                    titleBitmap.recycle();
                if (title != null) {
                    title.destroyDrawingCache();
                    title.setDrawingCacheEnabled(false);
                }
            }
        }
    }
}
