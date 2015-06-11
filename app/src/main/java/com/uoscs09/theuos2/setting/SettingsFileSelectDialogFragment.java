package com.uoscs09.theuos2.setting;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsArrayAdapter;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.PrefUtil;
import com.uoscs09.theuos2.util.TrackerUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 설정 화면에서 <b>'이미지 저장 경로'</b> 를 선택하면 전환되는 Fragment<br>
 * dialog 형식으로 동작하며 아래와 같은 기능을 제공한다.<br>
 * <li>현재 설정된 경로를 보여주는 기능</li> <li>
 * 디렉토리 탐색 기능</li> <li>새 폴더 생성 기능</li> <li>디렉토리 경로 선택 기능</li><br>
 * <br>
 * <br>
 * <p/>
 * 디렉토리 선택버튼을 눌렀을 시 디렉토리 경로가 preference 에 <br>
 * {@code PrefUtil.KEY_***_SAVE_PATH}를 키값으로 해서 저장되어야 한다. <br>
 * <br>
 * <p/>
 * 취소한 경우 preference 의 값은 바뀌지 않는다.
 */
public class SettingsFileSelectDialogFragment extends DialogFragment {
    private String path;
    private ArrayList<File> mFileList;
    private ArrayAdapter<File> mFileArrayAdapter;
    private final String ROOT = "/";
    private String PATH_KEY;
    private int titleId;
    private Toolbar mToolbar;

    public void setSavePathKey(String path) {
        this.PATH_KEY = path;
    }

    public void setTitleId(int id) {
        this.titleId = id;
    }

    private static final String TAG = "SettingsFileSelectDialogFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TrackerUtil.getInstance(this).sendVisibleEvent(TAG);

        mFileList = new ArrayList<>();
        path = getPathFromPref(getActivity());
    }

    /**
     * Dialog 의 View 를 생성한다.
     */
    private View createView() {
        mFileArrayAdapter = new FileListAdapter(getActivity(), mFileList);

        View rootView = View.inflate(getActivity(), R.layout.dialog_setting_save_route, null);

        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        mToolbar.setTitle(titleId);
        mToolbar.inflateMenu(R.menu.dialog_file_select);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.action_up:

                        if (path.equals(ROOT))
                            loadFileListToListView(new File(ROOT));
                        else
                            loadFileListToListView(new File(path).getParentFile());

                        return true;
                    default:
                        return false;
                }
            }
        });

        ListView listView = (ListView) rootView.findViewById(R.id.dialog_setting_save_route_listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                loadFileListToListView((File) arg0.getItemAtPosition(position));
            }
        });
        listView.setAdapter(mFileArrayAdapter);

        mToolbar.setSubtitle(path);

        return rootView;
    }

    @Override
    public void onResume() {
        loadFileListToListView(new File(path));
        super.onResume();
    }

    void loadFileListToListView(File file) {
        if (file == null) {
            AppUtil.showToast(getActivity(), R.string.setting_save_route_error_parent, true);
            return;
        }

        if (file.isDirectory()) {
            mFileArrayAdapter.clear();

            File[] files = file.listFiles();
            if (files == null)
                return;

            for (File f : files) {
                if (!f.isHidden() && f.isDirectory())
                    mFileList.add(f);
            }
            // mFileArrayAdapter.addAll(files);

            Collections.sort(mFileList, caseIgnoreComparator);
            mFileArrayAdapter.notifyDataSetChanged();
            path = file.getAbsolutePath();
            mToolbar.setSubtitle(path);
        }
    }

    private final Comparator<File> caseIgnoreComparator = new Comparator<File>() {
        @Override
        public int compare(File lhs, File rhs) {
            return lhs.getName().compareToIgnoreCase(rhs.getName());
        }
    };

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setView(createView())
                //.setIconAttribute(R.attr.theme_ic_action_image_image)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        putPathToPref(getActivity(), path);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    void putPathToPref(Context context, String path) {
        PrefUtil.getInstance(context).put(PATH_KEY, path);
    }

    private String getPathFromPref(Context context) {
        String defaultRoute = PrefUtil.getDefaultPath(PATH_KEY);
        return PrefUtil.getInstance(context).get(PATH_KEY, defaultRoute);
    }


    private static class FileListAdapter extends AbsArrayAdapter.SimpleAdapter<File> {

        public FileListAdapter(Context context, List<File> list) {
            super(context, R.layout.list_layout_save_route, list);
        }

        @Override
        public String getTextFromItem(File item) {
            return item.getName();
        }

        @Override
        public SimpleViewHolder getViewHolder(View convertView) {
            return new SettingsFileSelectDialogFragment.ViewHolder(convertView);
        }
    }

    private static class ViewHolder extends AbsArrayAdapter.SimpleViewHolder{

        public ViewHolder(View view) {
            super(view);
            textView.setGravity(Gravity.CENTER_VERTICAL);
        }
    }

}
