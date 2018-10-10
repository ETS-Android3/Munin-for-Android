package com.chteuchteu.munin.ntfs.poll;

import android.app.Service;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.chteuchteu.munin.hlpr.Settings;

/**
 * Notifications Service
 * Launched when enabling notifications from the app or
 * on device boot using BootReceiver class
 */
public class Service_PollNotifications extends Service {
	public WakeLock mWakeLock;

	/**
	 * Simply return null, since our Service will not be communicating with
	 * any other components. It just does its work silently.
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@SuppressWarnings("deprecation")
	private void handleIntent(Intent intent) {
		// obtain the wake lock
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getPackageName());
		mWakeLock.acquire();

		// check the global background data setting
		ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		if (!cm.getBackgroundDataSetting()) {
			stopSelf();
			return;
		}

		NetworkInfo mWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		boolean wifiOnly = Settings.getInstance(this).getBool(Settings.PrefKeys.Notifs_Poll_WifiOnly);

		if (!wifiOnly || mWifi.isConnected())
			new PollTask(this).execute();
		else {
			if (mWakeLock.isHeld())
				mWakeLock.release();
			stopSelf();
		}
	}

	/**
	 * Returning START_NOT_STICKY tells the system to not restart the
	 * service if it is killed because of poor resource (memory/cpu) conditions.
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		handleIntent(intent);
		return START_NOT_STICKY;
	}

	/**
	 * In onDestroy() we release our wake lock. This ensures that whenever the
	 * Service stops (killed for resources, stopSelf() called, etc.), the wake
	 * lock will be released.
	 */
	public void onDestroy() {
		super.onDestroy();
		if (mWakeLock.isHeld())
			mWakeLock.release();
	}
}
