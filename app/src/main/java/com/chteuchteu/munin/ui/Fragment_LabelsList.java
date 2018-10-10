package com.chteuchteu.munin.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.chteuchteu.munin.MuninFoo;
import com.chteuchteu.munin.R;
import com.chteuchteu.munin.adptr.Adapter_SelectableList;
import com.chteuchteu.munin.obj.Label;

import java.util.ArrayList;
import java.util.List;

public class Fragment_LabelsList extends Fragment {
	public static final String SELECT_CURRENT_ITEM = "selectCurrentItem";
	private MuninFoo muninFoo;
	private Context context;
	private ILabelsActivity activity;
	private Adapter_SelectableList selectableAdapter;
	private View view;
	private ListView listView;
	/**
	 * Don't select current item on Activity_Labels with FULLSCREEN layoutType
	 */
	private boolean selectCurrentItem;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = (ILabelsActivity) activity;
		this.context = activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		muninFoo = MuninFoo.getInstance();

		this.view = inflater.inflate(R.layout.fragment_labelslist, container, false);
		this.listView = (ListView) view.findViewById(R.id.listview);
		if (getArguments() != null)
			this.selectCurrentItem = getArguments().getBoolean(SELECT_CURRENT_ITEM, true);

		updateListView();

		activity.onLabelsFragmentLoaded();

		return view;
	}

	public void setSelectedItem(int position) {
		if (selectCurrentItem)
			selectableAdapter.setSelectedItem(position);
	}

	public void updateListView() {
		List<String> list = new ArrayList<>();

		view.findViewById(R.id.no_label).setVisibility(muninFoo.labels.isEmpty() ? View.VISIBLE : View.GONE);

		if (muninFoo.labels.size() > 0) {
			for (int i=0; i<muninFoo.labels.size(); i++)
				list.add(muninFoo.labels.get(i).getName());

			selectableAdapter = new Adapter_SelectableList(context, R.layout.labelselection_list, R.id.line_a, list);
			listView.setAdapter(selectableAdapter);

			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
					activity.onLabelClick(muninFoo.labels.get(position));
					if (selectCurrentItem)
						selectableAdapter.setSelectedItem(position);
				}
			});

			listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(final AdapterView<?> adapter, View view, int pos, long arg) {
					final TextView labelNameTextView = (TextView) view.findViewById(R.id.line_a);
					final String labelName = labelNameTextView.getText().toString();

					// Display actions list
					AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
					final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
							context, android.R.layout.simple_list_item_1);
					arrayAdapter.add(context.getString(R.string.rename_label));
					arrayAdapter.add(context.getString(R.string.delete));

					builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
								case 0: // Rename label
									LayoutInflater inflater = LayoutInflater.from(context);
									ViewGroup view = (ViewGroup) inflater.inflate(R.layout.dialog_edittext, null, false);
									final EditText input = (EditText) view.findViewById(R.id.input);
									input.setText(labelName);

									new AlertDialog.Builder(context)
											.setTitle(R.string.rename_label)
											.setView(view)
											.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
												public void onClick(DialogInterface dialog, int whichButton) {
													String value = input.getText().toString();
													if (!value.equals(labelName)) {
														Label label = muninFoo.getLabel(labelName);
														if (label != null) {
															label.setName(value);
															MuninFoo.getInstance(context).sqlite.dbHlpr.updateLabel(label);
															labelNameTextView.setText(value);
															activity.unselectLabel();
															updateListView();
														}

													}
													dialog.dismiss();
												}
											}).setNegativeButton(R.string.text64, new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int whichButton) {
										}
									}).show();
									break;
								case 1: // Delete label
									new AlertDialog.Builder(context)
											.setTitle(R.string.delete)
											.setMessage(R.string.text82)
											.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog, int which) {
													Label label = muninFoo.getLabel(labelName);

													muninFoo.removeLabel(label);
													updateListView();
												}
											})
											.setNegativeButton(R.string.no, null)
											.show();

									break;
							}
						}
					});
					builderSingle.show();

					return true;
				}
			});
		}
	}
}
