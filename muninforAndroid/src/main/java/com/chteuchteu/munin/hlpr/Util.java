package com.chteuchteu.munin.hlpr;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.Vibrator;
import androidx.appcompat.app.ActionBar;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chteuchteu.munin.R;
import com.chteuchteu.munin.obj.MuninPlugin;
import com.chteuchteu.munin.obj.MuninPlugin.Period;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class Util {
	public static final class UI {
		/**
		 * Prepares a Gmail-style progressbar on the actionBar
		 * Should be call in onCreate
		 * @param activity Activity
		 */
		public static ProgressBar prepareGmailStyleProgressBar(final Activity activity, final ActionBar actionBar) {
			// create new ProgressBar and style it
			final ProgressBar progressBar = new ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal);
			progressBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 24));
			progressBar.setProgress(0);
			progressBar.setVisibility(View.GONE);

			// retrieve the top view of our application
			final FrameLayout decorView = (FrameLayout) activity.getWindow().getDecorView();
			decorView.addView(progressBar);

			// Here we try to position the ProgressBar to the correct position by looking
			// at the position where content area starts. But during creating time, sizes
			// of the components are not set yet, so we have to wait until the components
			// has been laid out
			// Also note that doing progressBar.setY(136) will not work, because of different
			// screen densities and different sizes of actionBar
			ViewTreeObserver observer = progressBar.getViewTreeObserver();
			observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
				@SuppressWarnings("deprecation")
				@Override
				public void onGlobalLayout() {
					View contentView = decorView.findViewById(android.R.id.content);
					int y = Util.getStatusBarHeight(activity) + actionBar.getHeight();

					progressBar.setY(y + contentView.getY() - 10);
					progressBar.setProgressDrawable(activity.getResources().getDrawable(
							R.drawable.progress_horizontal_holo_no_background_light));

					ViewTreeObserver observer = progressBar.getViewTreeObserver();
					observer.removeGlobalOnLayoutListener(this);
				}
			});

			return progressBar;
		}
	}

    /**
     * @deprecated - don't use custom fonts
     */
	public static final class Fonts {
		/* ENUM Custom Fonts */
		public enum CustomFont {
			RobotoCondensed_Regular("RobotoCondensed-Regular.ttf"),
			RobotoCondensed_Bold("RobotoCondensed-Bold.ttf"),
            /**
             * @deprecated - Use android:textStyle="bold" instead
             */
			Roboto_Medium("Roboto-Medium.ttf");

			final String file;
			CustomFont(String fileName) { this.file = fileName; }
			public String getValue() { return this.file; }
		}

		/* Fonts */
		public static void setFont(Context c, ViewGroup g, CustomFont font) {
			Typeface mFont = Typeface.createFromAsset(c.getAssets(), font.getValue());
			setFont(g, mFont);
		}

		public static void setFont(Context c, TextView t, CustomFont font) {
			Typeface mFont = Typeface.createFromAsset(c.getAssets(), font.getValue());
			t.setTypeface(mFont);
		}

		public static void setFont(Context c, Button t, CustomFont font) {
			Typeface mFont = Typeface.createFromAsset(c.getAssets(), font.getValue());
			t.setTypeface(mFont);
		}

		private static void setFont(ViewGroup group, Typeface font) {
			int count = group.getChildCount();
			View v;
			for (int i = 0; i < count; i++) {
				v = group.getChildAt(i);
				if (v instanceof TextView)
					((TextView) v).setTypeface(font);
				else if (v instanceof ViewGroup)
					setFont((ViewGroup) v, font);
			}
		}
	}

	public static int pxToDp(int px) {
		return (int) (px / Resources.getSystem().getDisplayMetrics().density);
	}

	public static int[] getDeviceSize(Context c) {
		int[] r = new int[2];
		DisplayMetrics dm = c.getResources().getDisplayMetrics();
		r[0] = dm.widthPixels;
		r[1] = dm.heightPixels;
		return r;
	}

	public enum TransitionStyle { DEEPER, SHALLOWER }
	public static void setTransition(Activity activity, TransitionStyle transitionStyle) {
        int enterAnim = -1;
        int exitAnim = -1;

        switch (transitionStyle) {
            case DEEPER:
                enterAnim = R.anim.deeper_in;
                exitAnim = R.anim.deeper_out;
                break;
            case SHALLOWER:
                enterAnim = R.anim.shallower_in;
                exitAnim = R.anim.shallower_out;
                //enterAnim = android.R.anim.slide_in_left;
                //exitAnim = android.R.anim.slide_out_right;
                break;

        }

        activity.overridePendingTransition(enterAnim, exitAnim);
	}

	public static int getStatusBarHeight(Context c) {
		int result = 0;
		int resourceId = c.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0)
			result = c.getResources().getDimensionPixelSize(resourceId);
		return result;
	}

	public static boolean isOnline(Context c) {
		ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return netInfo != null && netInfo.isConnectedOrConnecting();
	}

	public static Period getDefaultPeriod(Context context) {
		return Period.get(Settings.getInstance(context).getString(Settings.PrefKeys.DefaultScale));
	}

	public static Bitmap removeBitmapBorder(Bitmap original) {
		if (original != null && original.getPixel(0, 0) == 0xFFCFCFCF) {
			try {
				return Bitmap.createBitmap(original, 2, 2, original.getWidth()-4, original.getHeight()-4);
			} catch (OutOfMemoryError | Exception ignored) {
				return original;
			}
		}
		// if null or does not needs to be cropped
		return original;
	}

	public static Bitmap dropShadow(Bitmap src) {
		if (src == null)
			return null;

		try {
			// Parameters
			int verticalPadding = 10;
			int horizontalPadding = 10;
			int radius = 3;
			int color = 0x44000000;

			// Create result bitmap
			Bitmap bmOut = Bitmap.createBitmap(src.getWidth() + horizontalPadding, src.getHeight() + verticalPadding, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bmOut);
			canvas.drawColor(0, PorterDuff.Mode.CLEAR);
			Paint ptBlur = new Paint();
			ptBlur.setMaskFilter(new BlurMaskFilter(radius, Blur.OUTER));
			int[] offsetXY = new int[2];
			// Capture alpha into a bitmap
			Bitmap bmAlpha = src.extractAlpha(ptBlur, offsetXY);
			Paint ptAlphaColor = new Paint();
			ptAlphaColor.setColor(color);
			canvas.drawBitmap(bmAlpha, 0, 0, ptAlphaColor);
			bmAlpha.recycle();
			// Paint image source
			canvas.drawBitmap(src, radius, radius, null);
			return bmOut;
		} catch (Exception ex) {
			return src;
		}
	}

	/**
	 * Remove legend and useless padding from graphs
	 *  (but keep graph name)
	 * @param src Bitmap
	 * @return New graph
	 */
	public static Bitmap extractGraph(Bitmap src) {
		if (src == null)
			return null;

		try {
			final int LEFT_PADDING = 15; // px
			final int RIGHT_PADDING = 20; // px
			final int TOP_PADDING = 0; // px
			final int HEIGHT = 225; // px
			return Bitmap.createBitmap(src, LEFT_PADDING, TOP_PADDING, src.getWidth() - (LEFT_PADDING + RIGHT_PADDING), HEIGHT);
		} catch (Exception ex) {
			return src;
		}
	}

	public static class URLManipulation {
		public static String setHttps(String url) {
			if (url.contains("http://"))
				url = url.replaceAll("http://", "https://");
			url = Util.URLManipulation.setPort(url, 443);
			return url;
		}

		/**
		 * Returns https:// from https://test.com
		 * @param url https://test.com
		 * @return https://
		 */
		public static String getScheme(String url) {
			try {
				URL u = new URL(url);
				return u.getProtocol() + "://";
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return "http://";
			}
		}

		/**
		 * Returns 4948 from http://test.fr:4948/
		 * @param url http://test.fr
		 * @return 80
		 */
		public static int getPort(String url) {
			try {
				URL u = new URL(url);
				int port = u.getPort();
				return port != -1 ? port : 80;
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return 80;
			}
		}

		private static String setPort(String url, int port) {
			URL _url;
			try {
				_url = new URL(url);
			} catch (MalformedURLException e) {
				return url;
			}
			if (_url.getPort() == port)
				return url;
			return _url.getProtocol() + "://" + _url.getHost() + ":" + port + _url.getFile();
		}

		public static String getHostFromUrl(String url) { return getHostFromUrl(url, url); }
		public static String getHostFromUrl(String url, String defaultUri) {
			try {
				URI uri = new URI(url);
				String domain = uri.getHost();
				return domain.startsWith("www.") ? domain.substring(4) : domain;
			} catch (Exception ex) {
				ex.printStackTrace();
				return defaultUri;
			}
		}
	}

	public static final class HDGraphs {
		private static float getScreenDensity(Context context) {
			return context.getResources().getDisplayMetrics().density;
		}

		public static int[] getBestImageDimensions(View imageView, Context context) {
			int[] res = new int[2];

			float screenDensity = getScreenDensity(context);
			if (screenDensity < 1)
				screenDensity = 1;

			int dimens_x = imageView.getMeasuredWidth();
			int dimens_y = imageView.getMeasuredHeight();

			// Apply density
			dimens_x = (int) (dimens_x/screenDensity);
			dimens_y = (int) (dimens_y/screenDensity);

			// Limit ratio
			if (dimens_y != 0) {
				double minRatio = ((double)360) / 210;
				double currentRatio = ((double)dimens_x) / dimens_y;

				if (currentRatio < minRatio) {
					// Adjust height
					dimens_y = (int) (dimens_x/minRatio);
				}
			}

			res[0] = dimens_x;
			res[1] = dimens_y;
			return res;
		}
	}

	public static void hideKeyboard(Activity activity, EditText editText) {
		InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}

	/**
	 * "apache" => "Apache"
	 * "some words" => "Some words"
	 * @param original Original string
	 * @return Capitalized string
	 */
	@SuppressLint("DefaultLocale")
	public static String capitalize(String original) {
		if (original.length() < 2)
			return original;

		return original.substring(0, 1).toUpperCase() + original.substring(1);
	}

	/**
	 * Extended boolean, especially useful when we don't know something's state at first
	 * For example: documentation availability: UNKNOWN => (TRUE/FALSE)
	 */
	public enum SpecialBool { UNKNOWN, TRUE, FALSE }

	public static String readFromAssets(Context context, String file) {
		try {
			InputStream is = context.getAssets().open(file);
			int size = is.available();

			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();

			return new String(buffer);
		} catch (IOException ex) {
			ex.printStackTrace();
			return "";
		}
	}

	public static String getAppVersion(Context context) {
		String versionName;
		try {
			versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException e) {
			versionName = "";
		}
		return versionName;
	}

	public static String getAndroidVersion() {
		String str = "Android " + Build.VERSION.RELEASE;

		// Get "KitKat"
		Field[] fields = Build.VERSION_CODES.class.getFields();
		for (Field field : fields) {
			String fieldName = field.getName();
			int fieldValue = -1;

			try {
				fieldValue = field.getInt(new Object());
			} catch (IllegalArgumentException | IllegalAccessException | NullPointerException e) {
				e.printStackTrace();
			}

            if (fieldValue == Build.VERSION.SDK_INT)
				str += " " + fieldName;
		}

		return str;
	}

	public static boolean isPackageInstalled (String packageName, Context c) {
		PackageManager pm = c.getPackageManager();
		try {
			pm.getPackageInfo(packageName, PackageManager.GET_META_DATA);
		} catch (PackageManager.NameNotFoundException e) {
			return false;
		}
		return true;
	}

	public static List<View> getViewsByTag(ViewGroup root, String tag) {
		List<View> views = new ArrayList<>();
		final int childCount = root.getChildCount();
		for (int i=0; i<childCount; i++) {
			final View child = root.getChildAt(i);
			if (child instanceof ViewGroup)
				views.addAll(Util.getViewsByTag((ViewGroup) child, tag));

			final Object tagObj = child.getTag();
			if (tagObj != null && tagObj.equals(tag))
				views.add(child);
		}
		return views;
	}

	/**
	 * Returns the first-level child of the parent, with the type 'type'
	 * @param parent root view
	 * @param type view type (EditText, ImageView, ...)
	 * @return may be null
	 */
	public static View getChild(ViewGroup parent, Class<?> type) {
		for (int i=0; i<parent.getChildCount(); i++) {
			View child = parent.getChildAt(i);

			if (child.getClass() == type)
				return child;
			else if (child instanceof ViewGroup) {
				View child2 = getChild((ViewGroup) child, type);
				if (child2 != null)
					return child2;
			}
		}

		return null;
	}

	/**
	 * Returns the bitmap position inside an imageView.
	 * @param imageView source ImageView
	 * @return 0: left, 1: top, 2: width, 3: height
	 */
	public static int[] getBitmapPositionInsideImageView(ImageView imageView) {
		int[] ret = new int[4];

		if (imageView == null || imageView.getDrawable() == null)
			return ret;

		// Get image dimensions
		// Get image matrix values and place them in an array
		float[] f = new float[9];
		imageView.getImageMatrix().getValues(f);

		// Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
		final float scaleX = f[Matrix.MSCALE_X];
		final float scaleY = f[Matrix.MSCALE_Y];

		// Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
		final Drawable d = imageView.getDrawable();
		final int origW = d.getIntrinsicWidth();
		final int origH = d.getIntrinsicHeight();

		// Calculate the actual dimensions
		final int actW = Math.round(origW * scaleX);
		final int actH = Math.round(origH * scaleY);

		ret[2] = actW;
		ret[3] = actH;

		// Get image position
		// We assume that the image is centered into ImageView
		int imgViewW = imageView.getWidth();
		int imgViewH = imageView.getHeight();

		int top = (imgViewH - actH)/2;
		int left = (imgViewW - actW)/2;

		ret[0] = left;
		ret[1] = top;

		return ret;
	}

	/**
	 * Returns a string containing the date (from timestamp) using the device locale
	 * @param timestamp long
	 * @return String
	 */
	public static String prettyDate(long timestamp) {
		return DateFormat.getDateTimeInstance().format(new Date(timestamp*1000));
	}

	public static final class Animations {
		public enum CustomAnimation {
			FADE_IN, FADE_OUT,
			SLIDE_IN, SLIDE_OUT
		}
		public enum AnimationSpeed {
			SLOW(1000), MEDIUM(300), FAST(100);

			private int duration;
			AnimationSpeed(int duration) { this.duration = duration; }
			public int getDuration() { return this.duration; }
		}

		public static void reveal_show(Context context, View view, int[] center, int finalRadius, CustomAnimation fallbackAnimation) {
			if (Build.VERSION.SDK_INT >= 21) {
				Animator anim = ViewAnimationUtils.createCircularReveal(view, center[0], center[1], 0, finalRadius);
				view.setVisibility(View.VISIBLE);
				anim.start();
			}
			else
				Animations.animate(context, view, fallbackAnimation);
		}

		public static void reveal_hide(Context context, final View view, int[] center, int initialRadius, CustomAnimation fallbackAnimation) {
			if (Build.VERSION.SDK_INT >= 21) {
				Animator anim = ViewAnimationUtils.createCircularReveal(view, center[0], center[1], initialRadius, 0);
				anim.addListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						super.onAnimationEnd(animation);
						view.setVisibility(View.GONE);
					}
				});

				anim.start();
			}
			else
				Animations.animate(context, view, fallbackAnimation, AnimationSpeed.MEDIUM, new Runnable() {
					@Override
					public void run() {
						view.setVisibility(View.GONE);
					}
				});
		}

		public static void animate(Context context, View view, CustomAnimation animation) { animate(context, view, animation, AnimationSpeed.MEDIUM, null); }
		public static void animate(Context context, final View view, CustomAnimation animation,
		                           AnimationSpeed animationSpeed, final Runnable onAnimationEnd) {
			if (view == null)
				return;

			switch (animation) {
				case FADE_IN:
					AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
					fadeIn.setDuration(animationSpeed.getDuration());
					fadeIn.setAnimationListener(new Animation.AnimationListener() {
						@Override public void onAnimationStart(Animation animation) { }
						@Override
						public void onAnimationEnd(Animation animation) {
							if (onAnimationEnd != null)
								onAnimationEnd.run();
						}
						@Override public void onAnimationRepeat(Animation animation) { }
					});
					view.startAnimation(fadeIn);

					break;
				case FADE_OUT:
					AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
					fadeOut.setDuration(animationSpeed.getDuration());
					fadeOut.setAnimationListener(new Animation.AnimationListener() {
						@Override public void onAnimationStart(Animation animation) { }
						@Override
						public void onAnimationEnd(Animation animation) {
							if (onAnimationEnd != null)
								onAnimationEnd.run();
						}
						@Override public void onAnimationRepeat(Animation animation) { }
					});
					view.startAnimation(fadeOut);

					break;
				case SLIDE_IN: {
					Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
					Point size = new Point();
					display.getSize(size);
					int screenH = size.y;
					TranslateAnimation a1 = new TranslateAnimation(
							Animation.RELATIVE_TO_SELF, 0,
							Animation.RELATIVE_TO_SELF, 0,
							Animation.ABSOLUTE, screenH,
							Animation.RELATIVE_TO_SELF, 0);
					a1.setDuration(300);
					a1.setFillAfter(true);
					a1.setInterpolator(new AccelerateDecelerateInterpolator());
					view.setVisibility(View.VISIBLE);
					view.startAnimation(a1);
					break;
				}
				case SLIDE_OUT: {
					Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
					Point size = new Point();
					display.getSize(size);
					int screenH = size.y;
					TranslateAnimation a1 = new TranslateAnimation(
							Animation.RELATIVE_TO_SELF, 0,
							Animation.RELATIVE_TO_SELF, 0,
							Animation.RELATIVE_TO_SELF, 0,
							Animation.ABSOLUTE, screenH);
					a1.setDuration(300);
					a1.setInterpolator(new AccelerateDecelerateInterpolator());
					a1.setAnimationListener(new Animation.AnimationListener() {
						@Override public void onAnimationStart(Animation animation) { }
						@Override public void onAnimationEnd(Animation animation) {
							view.setVisibility(View.GONE);
						}
						@Override public void onAnimationRepeat(Animation animation) { }
					});
					view.startAnimation(a1);

					break;
				}
			}
		}
	}

	public interface ProgressNotifier { void notify(int progress, int total); }

	@SuppressWarnings("deprecation")
	public static void removeOnGlobalLayoutListener(View v, ViewTreeObserver.OnGlobalLayoutListener listener){
		if (Build.VERSION.SDK_INT < 16)
			v.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
		else
			v.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
	}

	public enum DeviceSizeCategory { SMALL, NORMAL, LARGE, XLARGE, UNKNOWN }
	public static DeviceSizeCategory getDeviceSizeCategory(Context context) {
		int screenLayout = context.getResources().getConfiguration().screenLayout;
		if ((screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE)
			return DeviceSizeCategory.XLARGE;
		else if ((screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE)
			return DeviceSizeCategory.LARGE;
		else if ((screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL)
			return DeviceSizeCategory.NORMAL;
		else if ((screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL)
			return DeviceSizeCategory.SMALL;
		else
			return DeviceSizeCategory.UNKNOWN;
	}

	/**
	 * Save Bitmap on SD card (Activity_GraphView)
	 * We assume that we own both READ_ & WRITE_EXTERNAL_STORAGE permissions
	 */
	public static String saveBitmap(Context context, Bitmap bitmap, MuninPlugin plugin, Period period) {
		String root = Environment.getExternalStorageDirectory().toString();
		File dir = new File(root + "/muninForAndroid/");
		if (!dir.exists() || !dir.isDirectory()) {
			if (!dir.mkdir())
				return null;
		}

		String pluginName = plugin.getFancyName();

		String fileName1 = plugin.getInstalledOn().getParent().getName() + "." + plugin.getInstalledOn().getName()
				+ " - " + pluginName + " - " + period.getLabel(context) + " ";
		String fileName2 = "01.png";
		File file = new File(dir, fileName1 + fileName2);
		int i = 1; 	String i_s;
		while (file.exists()) {
			if (i<99) {
				if (i<10)	i_s = "0" + i;
				else		i_s = "" + i;
				fileName2 = i_s + ".png";
				file = new File(dir, fileName1 + fileName2);
				i++;
			}
			else
				break;
		}
		if (file.exists()) {
			if (!file.delete())
				return null;
		}

		try {
			FileOutputStream out = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();

			// Make the image appear in gallery
			new MediaScannerUtil(context, file).execute();

			return fileName1 + fileName2;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String pluginsListAsString(List<MuninPlugin> list) {
		String str = "";

		for (MuninPlugin plugin : list) {
			if (list.indexOf(plugin) != list.size()-1)
				str += plugin.getFancyName() + ", ";
			else
				str += plugin.getFancyName();
		}

		return str;
	}

    public static Rect locateView(View v) {
        int[] loc_int = new int[2];
        if (v == null) return null;
        try {
            v.getLocationOnScreen(loc_int);
        } catch (NullPointerException npe) {
            // Happens when the view doesn't exist on screen anymore.
            return null;
        }
        Rect location = new Rect();
        location.left = loc_int[0];
        location.top = loc_int[1];
        location.right = location.left + v.getWidth();
        location.bottom = location.top + v.getHeight();
        return location;
    }

    public static void vibrate(Context context, long milliseconds) {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (v.hasVibrator())
            v.vibrate(milliseconds);
    }

	public static boolean nullOrEmpty(String str) {
		return str == null || str.equals("");
	}

	public static CharSequence[] stringArrayToCharSequenceArray(Object[] stringArray) {
		CharSequence[] charSequenceArray = new CharSequence[stringArray.length];

		for (int i=0; i<stringArray.length; i++)
			charSequenceArray[i] = (String) stringArray[i];

		return charSequenceArray;
	}

	public static boolean isWifiConnected(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		return activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
	}

	/**
	 * Removes all occurrences of each pattern in str
	 * @param str String
	 * @param patterns String array
     * @return String
     */
	public static String removeAll(String str, String[] patterns) {
		for (String pattern : patterns)
			str = str.replace(pattern, "");
		return str;
	}

	public static void addShortcutToHomescreen(Context context, String name, Intent shortcutIntent) {
		Intent addIntent = new Intent();
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
				Intent.ShortcutIconResource.fromContext(context,
						R.drawable.launcher_icon));

		addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
		addIntent.putExtra("duplicate", false);
		context.sendBroadcast(addIntent);
	}
}
