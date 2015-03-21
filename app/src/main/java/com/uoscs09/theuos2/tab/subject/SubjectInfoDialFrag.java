package com.uoscs09.theuos2.tab.subject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.javacan.asyncexcute.AsyncCallback;
import com.javacan.asyncexcute.AsyncExecutor;
import com.nhaarman.listviewanimations.appearance.AnimationAdapter;
import com.nhaarman.listviewanimations.appearance.simple.AlphaInAnimationAdapter;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.common.ListViewBitmapWriteTask;
import com.uoscs09.theuos2.http.HttpRequest;
import com.uoscs09.theuos2.parse.ParserSubjectInfo;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.OApiUtil;
import com.uoscs09.theuos2.util.OApiUtil.Semester;
import com.uoscs09.theuos2.util.PrefUtil;
import com.uoscs09.theuos2.util.StringUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.Callable;

/**
 * 수업 계획서를 보여주는 DialogFragment<br>
 * {@code Fragment.setArgument()}와 {@code Bundle.putPacelable()}<br>
 * 을 통해 수업계획서를 보여줄 SubjectItem과 학기 정보를 전달해야 한다
 */
@Deprecated
public class SubjectInfoDialFrag extends DialogFragment implements AsyncCallback<ArrayList<String>>, Callable<ArrayList<String>>,OnClickListener {
	/** 교과목 객체 */
	private SubjectItem item;
	/** OAPI에 Query할 매개변수들 */
	private Hashtable<String, String> params;
	private ArrayList<String> infoList;
	/** 비동기 작업 객체, OAPI를 query할 때 사용된다. */
	protected AsyncExecutor<ArrayList<String>> ex;
	/** Progress */
	private Dialog mProgressDialog;
	/** Listview */
	private ListView listView;
	/** ListView Adapter */
	private ArrayAdapter<String> adapter;
	private AnimationAdapter aAdapter;

    private final ParserSubjectInfo mParser = new ParserSubjectInfo();

	private final static String URL = "http://wise.uos.ac.kr/uosdoc/api.ApiApiCoursePlanView.oapi";
	private final static String INFO = "info";
	private final static String TITLE = "수업계획서";

	public static void showDialog(FragmentManager fm, SubjectItem item,Context context, int term, String year) {
		Bundle b = new Bundle();
		b.putParcelable(OApiUtil.SUBJECT_NAME, item);
		b.putInt(OApiUtil.TERM, term);
		b.putString(OApiUtil.YEAR, year);

		DialogFragment f = (DialogFragment) fm.getFragment(b, "info");

		if (f == null)
			f = (DialogFragment) Fragment.instantiate(context,SubjectInfoDialFrag.class.getName(), b);

		f.show(fm, "info");
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Bundle b = getArguments();
		item = b.getParcelable(OApiUtil.SUBJECT_NAME);
		Semester semester = Semester.values()[b.getInt(OApiUtil.TERM)];
		if (item == null)
			this.dismiss();

		params = new Hashtable<>(5);
		params.put(OApiUtil.API_KEY, OApiUtil.UOS_API_KEY);
		params.put(OApiUtil.TERM, semester.code);
		params.put(OApiUtil.SUBJECT_NO, item.infoArray[3]);
		params.put(OApiUtil.CLASS_DIV, item.infoArray[4]);
		params.put(OApiUtil.YEAR, b.getString(OApiUtil.YEAR));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,	Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.dialog_subject_info, container,false);
		Context context = getActivity();
		listView = (ListView) v	.findViewById(R.id.dialog_etc_subject_info_list_view);

		v.findViewById(R.id.dialog_etc_subject_info_image_button_save).setOnClickListener(this);

		if (savedInstanceState != null) {
			infoList = savedInstanceState.getStringArrayList(INFO);
		} else {
			infoList = new ArrayList<>();
		}
		mProgressDialog = AppUtil.getProgressDialog(context, false,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if (ex != null && !ex.isCancelled()) {
							ex.cancel(true);
						}
						dismiss();
					}
				});

		adapter = new SubjectInfoAdapter(context,R.layout.list_layout_subject_info, infoList);
		aAdapter = new AlphaInAnimationAdapter(adapter);
		aAdapter.setAbsListView(listView);
		listView.setAdapter(aAdapter);
		aAdapter.notifyDataSetChanged();
		return v;
	}

	/** 저장 버튼을 누르면 호출되는 Callback */
	@Override
	public void onClick(View v) {
        String dir = PrefUtil.getPictureSavedPath(getActivity()) + TITLE + '_' + item.infoArray[5] + '_' + item.infoArray[8] + '_' + item.infoArray[4] + ".jpeg";
		ListViewBitmapWriteTask task = new ListViewBitmapWriteTask(dir, listView);
		task.execute();
	}

	@Override
	public Dialog getDialog() {
        return new MaterialDialog.Builder(getActivity())
                .title(TITLE)
                .build();
	}

	@Override
	public void onResume() {
		if (infoList.size() == 0) {
			ex = new AsyncExecutor<ArrayList<String>>()
                    .setCallable(this)
					.setCallback(this);
			ex.executeOnExecutor(AsyncExecutor.THREAD_POOL_EXECUTOR);
			mProgressDialog.show();
		}

		super.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putStringArrayList(INFO, infoList);
		super.onSaveInstanceState(outState);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<String> call() throws Exception {
		String body = HttpRequest.getBody(URL, StringUtil.ENCODE_EUC_KR, params, StringUtil.ENCODE_EUC_KR);
		return mParser.parse(body);
	}

	@Override
	public void onResult(ArrayList<String> result) {
		infoList = result;
		getDialog().setTitle(TITLE);
		adapter.clear();
		adapter.addAll(result);
		adapter.notifyDataSetChanged();
		aAdapter.notifyDataSetChanged();
	}

	@Override
	public void exceptionOccured(Exception e) {
		if (e instanceof IOException) {
			AppUtil.showInternetConnectionErrorToast(getActivity(), true);
		} else {
			AppUtil.showErrorToast(getActivity(), e, isVisible());
		}
		this.dismiss();
	}

	@Override
	public void cancelled() {
	}

	@Override
	public void onPostExcute() {
		mProgressDialog.dismiss();
	}
}
