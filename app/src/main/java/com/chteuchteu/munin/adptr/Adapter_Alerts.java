package com.chteuchteu.munin.adptr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chteuchteu.munin.MuninFoo;
import com.chteuchteu.munin.R;
import com.chteuchteu.munin.hlpr.Util;
import com.chteuchteu.munin.obj.MuninNode;
import com.chteuchteu.munin.ui.Activity_AlertsPlugins;

import java.util.ArrayList;
import java.util.List;

public class Adapter_Alerts {
	private List<AlertPart> parts;
	private Context context;
	private LayoutInflater layoutInflater;

	public enum ListItemSize { REDUCED, EXPANDED }
	private ListItemSize listItemSize;

	public enum ListItemPolicy { SHOW_ALL, HIDE_NORMAL }
	private ListItemPolicy listItemPolicy;

	// Colors list - fetched from resources at runtime
    private int COLOR_BG_CRITICAL;
    private int COLOR_BG_WARNING;
    private int COLOR_BG_OK;
    private int COLOR_BG_UNDEFINED;
    private int COLOR_NODENAME_TEXT_COLOR;
    private int COLOR_MASTERNAME_TEXT_COLOR;

	public Adapter_Alerts(Context context, List<MuninNode> items,
	                      ListItemSize listItemSize, ListItemPolicy listItemPolicy) {
		this.context = context;
		this.listItemSize = listItemSize;
		this.listItemPolicy = listItemPolicy;
		this.parts = new ArrayList<>();

		for (MuninNode node : items)
			this.parts.add(new AlertPart(node, this));

        // Resolve colors from resources
        this.COLOR_BG_CRITICAL = context.getResources().getColor(R.color.alerts_bg_color_critical);
        this.COLOR_BG_WARNING = context.getResources().getColor(R.color.alerts_bg_color_warning);
        this.COLOR_BG_OK = context.getResources().getColor(R.color.alerts_bg_color_ok);
        this.COLOR_BG_UNDEFINED = context.getResources().getColor(R.color.alerts_bg_color_undefined);
        this.COLOR_NODENAME_TEXT_COLOR = context.getResources().getColor(R.color.alerts_servername_text_color);
        this.COLOR_MASTERNAME_TEXT_COLOR = context.getResources().getColor(R.color.alerts_mastername_text_color);
	}

	public View getView(int position, ViewGroup parent) {
		AlertPart alertPart = parts.get(position);
		if (this.layoutInflater == null)
			this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View view = layoutInflater.inflate(R.layout.alerts_part, parent, false);
		return alertPart.inflate(context, view);
	}

    public void updateViews(int from, int to) {
        for (int i=from; i<=to; i++)
            parts.get(i).updateView();
    }
	public void updateViews() {
		for (AlertPart alertPart : parts)
			alertPart.updateView();
	}

	public void updateViewsPartial() {
		for (AlertPart alertPart : parts)
			alertPart.updateViewPartial();
	}

	public void setAllGray() {
		for (AlertPart part : parts)
			part.setGray();
	}

	public void setListItemSize(ListItemSize val) {
		this.listItemSize = val;
		for (AlertPart part : parts)
			part.onListItemSizeChange();
	}
	public ListItemSize getListItemSize() { return this.listItemSize; }

	public void setListItemPolicy(ListItemPolicy val) { this.listItemPolicy = val; }
	public ListItemPolicy getListItemPolicy() { return this.listItemPolicy; }

	public boolean isEverythingOk() {
		for (AlertPart alertPart : parts) {
			if (!alertPart.isEverythingOk())
				return false;
		}
		return true;
	}

    public boolean shouldDisplayEverythingsOkMessage() {
        return this.listItemPolicy == ListItemPolicy.HIDE_NORMAL && isEverythingOk();
    }

	public class AlertPart {
		private boolean viewInflated;
		private Adapter_Alerts adapter;
		private MuninNode node;

		private LinearLayout part;
		private RelativeLayout cardHeader;
		private TextView masterName;
		private TextView nodeName;
		private LinearLayout criticals;
		private TextView criticalsAmount;
		private TextView criticalsLabel;
		private TextView criticalsPluginsList;
		private LinearLayout warnings;
		private TextView warningsAmount;
		private TextView warningsLabel;
		private TextView warningsPluginsList;
		private boolean everythingsOk;

		public AlertPart(MuninNode node, Adapter_Alerts adapter) {
			this.node = node;
			this.adapter = adapter;
			this.everythingsOk = true;
			this.viewInflated = false;
		}

		public View inflate(final Context context, View v) {
			if (viewInflated)
				return v;

			viewInflated = true;
			part 					= (LinearLayout) v.findViewById(R.id.alerts_part);
			cardHeader             = (RelativeLayout) v.findViewById(R.id.cardHeader);
			masterName             = (TextView) v.findViewById(R.id.alerts_part_masterName);
			nodeName 			= (TextView) v.findViewById(R.id.alerts_part_serverName);
			criticals 				= (LinearLayout) v.findViewById(R.id.alerts_part_criticals);
			criticalsAmount 		= (TextView) v.findViewById(R.id.alerts_part_criticalsNumber);
			criticalsLabel 		= (TextView) v.findViewById(R.id.alerts_part_criticalsLabel);
			criticalsPluginsList 	= (TextView) v.findViewById(R.id.alerts_part_criticalsPluginsList);
			warnings 				= (LinearLayout) v.findViewById(R.id.alerts_part_warnings);
			warningsAmount		= (TextView) v.findViewById(R.id.alerts_part_warningsNumber);
			warningsLabel 			= (TextView) v.findViewById(R.id.alerts_part_warningsLabel);
			warningsPluginsList 	= (TextView) v.findViewById(R.id.alerts_part_warningsPluginsList);

			part.setVisibility(View.GONE);
			nodeName.setText(node.getName());
			masterName.setText(node.getParent().getName());
			cardHeader.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					MuninFoo.getInstance().setCurrentNode(node);
					context.startActivity(new Intent(context, Activity_AlertsPlugins.class));
					Util.setTransition((Activity) context, Util.TransitionStyle.DEEPER);
				}
			});

			Util.Fonts.setFont(context, criticals, Util.Fonts.CustomFont.RobotoCondensed_Regular);
			Util.Fonts.setFont(context, warnings, Util.Fonts.CustomFont.RobotoCondensed_Regular);

			return v;
		}

		public void updateView() {
			int nbErrors = node.getErroredPlugins().size();
			int nbWarnings = node.getWarnedPlugins().size();

			// We set view states for both ListItemSize.REDUCED _and_ EXPANDED
			// when possible, to make switching from one to another easier

			if (node.reachable == Util.SpecialBool.TRUE) {
				everythingsOk = nbErrors == 0 && nbWarnings == 0;

				if (nbErrors > 0) {
					criticals.setBackgroundColor(COLOR_BG_CRITICAL);
					criticalsPluginsList.setText(Util.pluginsListAsString(node.getErroredPlugins()));
				}
				else
					criticals.setBackgroundColor(COLOR_BG_OK);

				if (nbWarnings > 0) {
					warnings.setBackgroundColor(COLOR_BG_WARNING);
					warningsPluginsList.setText(Util.pluginsListAsString(node.getWarnedPlugins()));
				}
				else
					warnings.setBackgroundColor(COLOR_BG_OK);

				criticalsAmount.setText(String.valueOf(nbErrors));
				criticalsLabel.setText(context.getString(
						nbErrors == 1 ? R.string.text50_1 // critical
								: R.string.text50_2 // criticals
				));

				warningsAmount.setText(String.valueOf(nbWarnings));
				warningsLabel.setText(context.getString(
						nbWarnings == 1 ? R.string.text51_1 // warning
								: R.string.text51_2 // warnings
				));

				if (adapter.getListItemSize() == ListItemSize.REDUCED) {
					cardHeader.setBackgroundColor(Color.TRANSPARENT);

					if (nbErrors > 0 || nbWarnings > 0) {
						cardHeader.setBackgroundColor(nbErrors > 0 ? COLOR_BG_CRITICAL : COLOR_BG_WARNING);

						nodeName.setTextColor(Color.WHITE);
						masterName.setTextColor(Color.WHITE);
					}
				}
			}
			else if (node.reachable == Util.SpecialBool.FALSE) {
				everythingsOk = true;
				criticalsPluginsList.setText("");
				warningsPluginsList.setText("");
				criticalsAmount.setText("?");
				warningsAmount.setText("?");
				criticalsLabel.setText(context.getString(R.string.text50_2));
				warningsLabel.setText(context.getString(R.string.text51_2));
				criticals.setBackgroundColor(COLOR_BG_UNDEFINED);
				warnings.setBackgroundColor(COLOR_BG_UNDEFINED);
			}

			updateViewPartial();
		}

		public void updateViewPartial() {
			boolean hideNormal = adapter.getListItemPolicy() == ListItemPolicy.HIDE_NORMAL;

			int nbErrors = node.getErroredPlugins().size();
			int nbWarnings = node.getWarnedPlugins().size();
			boolean hasErrorsOrWarnings = nbErrors > 0 || nbWarnings > 0;

			cardHeader.setClickable(hasErrorsOrWarnings);
			part.setVisibility(hideNormal && !hasErrorsOrWarnings ? View.GONE : View.VISIBLE);
		}

		public void setGray() {
			criticals.setBackgroundColor(COLOR_BG_UNDEFINED);
			warnings.setBackgroundColor(COLOR_BG_UNDEFINED);
		}

		public void onListItemSizeChange() {
			switch (adapter.getListItemSize()) {
				case REDUCED:
					criticals.setVisibility(View.GONE);
					warnings.setVisibility(View.GONE);

					int i_criticalsAmount = getIntFromTextView(criticalsAmount);
					int i_warningsAmount = getIntFromTextView(warningsAmount);

					cardHeader.setBackgroundColor(Color.TRANSPARENT);
					if (i_criticalsAmount > 0 || i_warningsAmount > 0) {
						cardHeader.setBackgroundColor(i_criticalsAmount > 0 ? COLOR_BG_CRITICAL : COLOR_BG_WARNING);

						masterName.setTextColor(Color.WHITE);
						nodeName.setTextColor(Color.WHITE);
					}
					break;
				case EXPANDED:
					criticals.setVisibility(View.VISIBLE);
					warnings.setVisibility(View.VISIBLE);
					cardHeader.setBackgroundColor(Color.TRANSPARENT);
					masterName.setTextColor(COLOR_MASTERNAME_TEXT_COLOR);
					nodeName.setTextColor(COLOR_NODENAME_TEXT_COLOR);
					break;
			}
		}

		public int getIntFromTextView(TextView tv) {
			String txt = tv.getText().toString();
			if (txt.isEmpty() || txt.equals("?"))
				return -1;
			return Integer.parseInt(txt);
		}

		public boolean isEverythingOk() { return this.everythingsOk; }
	}
}
