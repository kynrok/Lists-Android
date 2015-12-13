package pnixx.lists.core;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

/**
 * Получение параметров окна
 * User: P.Nixx
 * Date: 26.10.12
 * Time: 16:47
 */
public class WindowsParam {

	private static WindowsParam instance;
	private PackageInfo pInfo;
	private float scale;
	private Context context;

	private WindowsParam(Context context) {
		this.context = context;

		try {
			pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
		} catch( PackageManager.NameNotFoundException e ) {
			e.printStackTrace();
		}

		//Получаем размер масштабирования
		scale = context.getResources().getDisplayMetrics().density;
	}

	//Получение ссылки на экран
	public static Display getDisplay() {
		return ((WindowManager) instance.context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
	}

	//Получение ориентации экрана
	public static int getOrient() {
		return instance.context.getResources().getConfiguration().orientation;
	}

	//Инициализация
	public static void init(Context context) {
		instance = new WindowsParam(context);
	}

	public static int getWidth() {
		return getDisplay().getWidth();
	}

	public static int getHeight() {
		return getDisplay().getHeight();
	}

	public static int getVersionCode() {
		return instance.pInfo.versionCode;
	}
	public static String getVersionName() {
		return instance.pInfo.versionName;
	}

	public static float getScale() {
		return instance.scale;
	}

	//Cache method
	public static boolean internetIsAvailable() {
		ConnectivityManager cm = (ConnectivityManager) instance.context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if( netInfo != null && netInfo.isConnected() ) {
			return true;
		}
		return false;
	}

	//Если подключены через wifi
	public static boolean isWifiConnected() {
		ConnectivityManager cm = (ConnectivityManager) instance.context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return mWifi != null && mWifi.isConnected() && mWifi.isConnectedOrConnecting();
	}

	//Получаем размер экрана
	@TargetApi(Build.VERSION_CODES.DONUT)
	public static int getScreenSize() {
		return (instance.context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK);
	}

	public static Context getContext() {
		return instance.context;
	}

	@TargetApi(Build.VERSION_CODES.DONUT)
	public static boolean isDebug() {
		return (0 != (instance.context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
	}
}
