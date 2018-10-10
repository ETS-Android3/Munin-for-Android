package com.chteuchteu.munin.adptr;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

public class Adapter_IconList extends ArrayAdapter<String> {
	private List<Integer> images;
	
	public Adapter_IconList(Context context, String[] items, Integer[] images) {
		super(context, android.R.layout.select_dialog_item, items);
		this.images = Arrays.asList(images);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		TextView textView = (TextView) view.findViewById(android.R.id.text1);
		textView.setCompoundDrawablesWithIntrinsicBounds(images.get(position), 0, 0, 0);
		textView.setCompoundDrawablePadding(
				(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getContext().getResources().getDisplayMetrics()));
		return view;
	}
}