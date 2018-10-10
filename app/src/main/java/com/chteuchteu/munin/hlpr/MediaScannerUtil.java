package com.chteuchteu.munin.hlpr;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;

import java.io.File;

/**
 * Util class
 *  Should be used every time a bitmap is saved into gallery
 */
public class MediaScannerUtil implements MediaScannerConnectionClient {
	private MediaScannerConnection mMs;
	private File mFile;
	private Context context;
	
	public MediaScannerUtil(Context context, File f) {
		this.mFile = f;
		this.context = context;
	}
	
	public void execute() {
		mMs = new MediaScannerConnection(context, this);
		mMs.connect();
	}
	
	@Override
	public void onMediaScannerConnected() {
		mMs.scanFile(mFile.getAbsolutePath(), null);
	}
	
	@Override
	public void onScanCompleted(String path, Uri uri) {
		mMs.disconnect();
	}
	
}