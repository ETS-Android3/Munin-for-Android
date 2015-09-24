package com.chteuchteu.munin.hlpr;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.chteuchteu.munin.MuninFoo;
import com.chteuchteu.munin.R;
import com.chteuchteu.munin.async.DonateAsync;
import com.chteuchteu.munin.hlpr.Util.TransitionStyle;
import com.chteuchteu.munin.ui.Activity_Alerts;
import com.chteuchteu.munin.ui.Activity_GoPremium;
import com.chteuchteu.munin.ui.Activity_Grid;
import com.chteuchteu.munin.ui.Activity_Grids;
import com.chteuchteu.munin.ui.Activity_Labels;
import com.chteuchteu.munin.ui.Activity_Notifications;
import com.chteuchteu.munin.ui.Activity_Plugins;
import com.chteuchteu.munin.ui.Activity_Servers;
import com.chteuchteu.munin.ui.MuninActivity;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DrawerHelper {
	private AppCompatActivity activity;
	private Context context;
	private MuninFoo muninFoo;
	private MuninActivity currentActivity;
	private Drawer drawer;
	private HashMap<DrawerMenuItem, IDrawerItem> drawerItems;
	private Toolbar toolbar;

	public enum DrawerMenuItem {
		None,
		Graphs,
		Grids,
		Alerts,
		Labels,
		Servers,
		Notifications,
		Premium,
		Support,
		Donate
	}
	
	public DrawerHelper(AppCompatActivity activity, MuninFoo muninFoo, Toolbar toolbar) {
		this.drawerItems = new HashMap<>();
		this.activity = activity;
		this.muninFoo = muninFoo;
		this.context = activity;
		this.toolbar = toolbar;
		initDrawer();
	}
	
	public void reset() {
		initDrawer();
		setDrawerActivity(currentActivity);
	}
	
	public void setDrawerActivity(MuninActivity activity) {
		this.currentActivity = activity;
		DrawerMenuItem item = activity == null ? DrawerMenuItem.None : activity.getDrawerMenuItem();

		if (this.drawerItems.containsKey(item))
			this.drawer.setSelection(this.drawerItems.get(item));
	}

	public void toggle() {
		if (this.drawer.isDrawerOpen())
			this.drawer.closeDrawer();
		else
			this.drawer.openDrawer();
	}

	public DrawerLayout getDrawerLayout() {
		return this.drawer.getDrawerLayout();
	}

	private int getIntentFlag() {
		return this.currentActivity instanceof Activity_Grid ? Intent.FLAG_ACTIVITY_CLEAR_TOP
				: Intent.FLAG_ACTIVITY_NEW_TASK;
	}

	private void initDrawer() {
		activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);

		// Graphs
		this.drawerItems.put(DrawerMenuItem.Graphs,
				new PrimaryDrawerItem()
						.withName(R.string.button_graphs)
						.withIcon(CommunityMaterial.Icon.cmd_pulse)
						.withEnabled(muninFoo.getNodes().size() > 0)
		);

		// Grids
		this.drawerItems.put(DrawerMenuItem.Grids,
				new PrimaryDrawerItem()
						.withName(R.string.button_grid)
						.withIcon(CommunityMaterial.Icon.cmd_grid)
						.withEnabled(muninFoo.premium && muninFoo.getNodes().size() > 0)
		);

		// Alerts
		this.drawerItems.put(DrawerMenuItem.Alerts,
				new PrimaryDrawerItem()
						.withName(R.string.button_alerts)
						.withIcon(CommunityMaterial.Icon.cmd_alert_box)
						.withEnabled(muninFoo.getNodes().size() > 0)
		);

		// Labels
		this.drawerItems.put(DrawerMenuItem.Labels,
				new PrimaryDrawerItem()
						.withName(R.string.button_labels)
						.withIcon(CommunityMaterial.Icon.cmd_label)
						.withEnabled(muninFoo.getNodes().size() > 0)
		);

		// Servers
		this.drawerItems.put(DrawerMenuItem.Servers,
				new PrimaryDrawerItem()
						.withName(R.string.button_server)
						.withIcon(CommunityMaterial.Icon.cmd_view_list)
		);

		// Notifications
		this.drawerItems.put(DrawerMenuItem.Notifications,
				new PrimaryDrawerItem()
						.withName(R.string.button_notifications)
						.withIcon(CommunityMaterial.Icon.cmd_bell)
						.withEnabled(muninFoo.premium && muninFoo.getNodes().size() > 0)
		);

		// Premium
		this.drawerItems.put(DrawerMenuItem.Premium,
				new PrimaryDrawerItem()
						.withName(R.string.button_premium)
						.withIcon(CommunityMaterial.Icon.cmd_lock_open)
		);

		// Support
		this.drawerItems.put(DrawerMenuItem.Support,
				new SecondaryDrawerItem()
						.withName(R.string.support)
						.withIcon(CommunityMaterial.Icon.cmd_help)
		);

		// Donate
		this.drawerItems.put(DrawerMenuItem.Donate,
				new SecondaryDrawerItem()
						.withName(R.string.donate)
						.withIcon(CommunityMaterial.Icon.cmd_gift)
		);

		DrawerBuilder builder = new DrawerBuilder()
				.withActivity(this.activity)
				.withToolbar(this.toolbar)
				.withSelectedItem(-1);

		// Add items
		builder.addDrawerItems(
				this.drawerItems.get(DrawerMenuItem.Graphs),
				this.drawerItems.get(DrawerMenuItem.Grids),
				this.drawerItems.get(DrawerMenuItem.Alerts),
				this.drawerItems.get(DrawerMenuItem.Labels),
				this.drawerItems.get(DrawerMenuItem.Servers),
				this.drawerItems.get(DrawerMenuItem.Notifications));

		if (!muninFoo.premium)
			builder.addDrawerItems(this.drawerItems.get(DrawerMenuItem.Premium));

		builder.addDrawerItems(
				new DividerDrawerItem(),
				this.drawerItems.get(DrawerMenuItem.Support),
				this.drawerItems.get(DrawerMenuItem.Donate));

		builder.withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
			@Override
			public boolean onItemClick(View view, int index, IDrawerItem iDrawerItem) {
				switch (index) {
					case 0: // Graphs
						startActivity(Activity_Plugins.class);
						return true;
					case 1: // Grids
						startActivity(Activity_Grids.class);
						return true;
					case 2: // Alerts
						startActivity(Activity_Alerts.class);
						return true;
					case 3: // Labels
						startActivity(Activity_Labels.class);
						return true;
					case 4: // Servers
						startActivity(Activity_Servers.class);
						return true;
					case 5: // Notifications
						startActivity(Activity_Notifications.class);
						return true;
					case 6: // Premium
						// TODO
						startActivity(Activity_GoPremium.class);
						return true;
					case 7: // Support
						Intent send = new Intent(Intent.ACTION_SENDTO);
						String uriText = "mailto:" + Uri.encode("support@munin-for-android.com") +
								"?subject=" + Uri.encode("Support request");
						Uri uri = Uri.parse(uriText);

						send.setData(uri);
						activity.startActivity(Intent.createChooser(send, context.getString(R.string.choose_email_client)));
						return true;
					case 8: // Donate
						donate();
						return true;
					default:
						return false;
				}
			}
		});

		this.drawer = builder.build();
	}

	private void startActivity(Class<?> targetActivity) {
		if (((Object) activity).getClass() == targetActivity)
			closeDrawerIfOpen();
		else {
			Intent intent = new Intent(activity, targetActivity);
			intent.addFlags(getIntentFlag());
			activity.startActivity(intent);
			Util.setTransition(activity, TransitionStyle.DEEPER);
		}
	}

	private void donate() {
		new AlertDialog.Builder(activity)
				.setTitle(R.string.donate)
				.setMessage(R.string.donate_text)
				.setPositiveButton(R.string.donate, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						@SuppressLint("InflateParams")
                        View view = inflater.inflate(R.layout.dialog_donate, null);

						final Spinner spinnerAmount = (Spinner) view.findViewById(R.id.donate_amountSpinner);
						List<String> list = new ArrayList<>();
						String euroSlashDollar = "\u20Ac/\u0024";
						list.add("1 " + euroSlashDollar);
						list.add("2 " + euroSlashDollar);
						list.add("5 " + euroSlashDollar);
						list.add("20 " + euroSlashDollar);
						ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, list);
						dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
						spinnerAmount.setAdapter(dataAdapter);

						new AlertDialog.Builder(activity)
								.setTitle(R.string.donate)
								.setView(view)
								.setPositiveButton(R.string.donate, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										// Launch BillingService, and then purchase the thing
										String product = "";
										switch (spinnerAmount.getSelectedItemPosition()) {
											case 0: product = BillingService.DONATE_1; break;
											case 1: product = BillingService.DONATE_2; break;
											case 2: product = BillingService.DONATE_5; break;
											case 3: product = BillingService.DONATE_20; break;
										}
										new DonateAsync(activity, product).execute();
									}
								})
								.setNegativeButton(R.string.text64, null)
								.show();
					}
				})
				.setNegativeButton(R.string.text64, null)
				.show();
	}

    /**
     * Close drawer if it is open
     * @return boolean true if drawer has been closed
     */
	public boolean closeDrawerIfOpen() {
		if (this.drawer.isDrawerOpen()) {
			this.drawer.closeDrawer();
			return true;
		}
		return false;
	}
}
