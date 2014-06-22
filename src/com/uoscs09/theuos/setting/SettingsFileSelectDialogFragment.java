package com.uoscs09.theuos.setting;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.PrefUtil;

/**
 * 설정 화면에서 <b>'이미지 저장 경로'</b> 를 선택하면 전환되는 Fragment<br>
 * dialog 형식으로 동작하며 아래와 같은 기능을 제공한다.<br>
 * <li>현재 설정된 경로를 보여주는 기능</li> <li>
 * 디렉토리 탐색 기능</li> <li>새 폴더 생성 기능</li> <li>디렉토리 경로 선택 기능</li><br>
 * <br>
 * <br>
 * 
 * 디렉토리 선택버튼을 눌렀을 시 디렉토리 경로가 preference에 <br>
 * {@code PrefUtil.KEY_SAVE_ROUTE}를 키값으로 해서 저장되어야 한다. <br>
 * <br>
 * 
 * 취소한 경우 preference의 값은 바뀌지 않는다.
 */
public class SettingsFileSelectDialogFragment extends DialogFragment {
	protected TextView pathTextView;
	protected String path;
	protected List<File> list;
	protected ArrayAdapter<File> adapter;
	protected final String ROOT = "/";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// getActivity().getActionBar().setTitle(R.string.setting_save_route);
		list = new ArrayList<File>();
		path = getPathFromPref(getActivity());
	}

	/** Dialog의 View를 생성한다. */
	private View createView() {
		int layout, listLayout;
		switch (AppUtil.theme) {
		case Black:
			layout = R.layout.dialog_setting_save_route_dark;
			listLayout = R.layout.list_layout_save_route_dark;
			break;
		default:
			layout = R.layout.dialog_setting_save_route;
			listLayout = R.layout.list_layout_save_route;
			break;
		}

		adapter = new FileListAdapter(getActivity(), listLayout, list);
		View rootView = View.inflate(getActivity(), layout, null);
		rootView.findViewById(R.id.dialog_setting_save_route_button_up)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (path.equals(ROOT))
							loadFileListToListView(new File(ROOT));
						else
							loadFileListToListView(new File(path)
									.getParentFile());
					}
				});
		ListView listView = (ListView) rootView
				.findViewById(R.id.dialog_setting_save_route_listView);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				loadFileListToListView((File) arg0.getItemAtPosition(position));
			}
		});
		listView.setAdapter(adapter);
		pathTextView = (TextView) rootView
				.findViewById(R.id.dialog_setting_save_route_text_path);
		pathTextView.setText(path);
		return rootView;
	}

	@Override
	public void onResume() {
		loadFileListToListView(new File(path));
		super.onResume();
	}

	protected void loadFileListToListView(File file) {
		if (file == null) {
			AppUtil.showToast(getActivity(),
					R.string.setting_save_route_error_parent, true);
			return;
		}
		if (file.isDirectory()) {
			adapter.clear();
			File[] files = file.listFiles();
			if (files == null)
				return;
			for (File f : files) {
				if (!f.isHidden() && f.isDirectory())
					list.add(f);
			}
			// adapter.addAll(files);

			Collections.sort(list, caseIgnoreComparator);
			adapter.notifyDataSetChanged();
			path = file.getAbsolutePath();
			pathTextView.setText(path);
		}
	}

	protected Comparator<File> caseIgnoreComparator = new Comparator<File>() {
		@Override
		public int compare(File lhs, File rhs) {
			return lhs.getName().compareToIgnoreCase(rhs.getName());
		}
	};

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new AlertDialog.Builder(getActivity())
				.setTitle(R.string.setting_save_route)
				.setView(createView())
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								putPathToPref(getActivity(), path);
							}
						}).setNegativeButton(android.R.string.cancel, null)
				.create();
	}

	protected void putPathToPref(Context context, String path) {
		PrefUtil.getInstance(context).put(PrefUtil.KEY_SAVE_ROUTE, path);
	}

	private String getPathFromPref(Context context) {
		String defaultRoute = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES).toString();
		return PrefUtil.getInstance(context).get(PrefUtil.KEY_SAVE_ROUTE,
				defaultRoute);
	}
}
