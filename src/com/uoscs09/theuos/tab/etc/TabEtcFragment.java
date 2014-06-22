package com.uoscs09.theuos.tab.etc;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.util.AppUtil;

public class TabEtcFragment extends Fragment implements View.OnClickListener {
	public static final String ETC_BUTTON = "etc_button";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.tab_etc, container, false);
		GridView gridView = (GridView) rootView.findViewById(R.id.tab_etc_grid_view);
		
		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.tab_etc, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(getActivity(), EtcActivity.class);
		intent.putExtra(ETC_BUTTON, v.getId());
		startActivity(intent);
	}
}
