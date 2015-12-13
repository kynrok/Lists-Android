package pnixx.lists.cache;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import pnixx.lists.core.WindowsParam;

@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class Cache {
	public final String TAG;

	//Params
	private int mMaxWidth;
	private int mMaxHeight;
	private static Cache instance;
	private int image_size = -1;

	//Memory cache
	public LruCache<String, Bitmap> mBitmapCache;
	public ArrayList<String> mCurrentTasks;
	public HashMap<String, ArrayList<BitmapLoadListener>> mTasksHandler;

	//Disk cache
	private DiskCache mDiskCache;

	//AsyncTask
	private int AsyncTask = 0;
	private final static int MAX_ASYNC_COUNT = 30;

	private Cache(Context context, int size, int maxWidth, int maxHeight) {
		mMaxWidth = maxWidth;
		mMaxHeight = maxHeight;

		//Получаем ссылку на кэш пакета
		TAG = context.getApplicationContext().getPackageName() + ".Cache";

		//Init memory cache
		mBitmapCache = new LruCache<String, Bitmap>(size) {
			@Override
			protected int sizeOf(String key, Bitmap b) {
				// Assuming that one pixel contains four bytes.
				return b.getHeight() * b.getWidth() * 4;
			}
		};

		mCurrentTasks = new ArrayList<String>();

		//Init disk cache
		mDiskCache = new DiskCache(context);

		//Init task handler
		mTasksHandler = new HashMap<String, ArrayList<BitmapLoadListener>>();
	}

	//Инициализация
	public static void initialize(Context context, int size, int maxWidth, int maxHeight) {
		instance = new Cache(context, size, maxWidth, maxHeight);
	}

	public static Cache getInstance() {
		return instance;
	}

	//Добавление в память кэша
	public void addBitmapToCache(String key, Bitmap bitmap) {
		if( getBitmap(key) == null ) {
			Log.d(TAG, "Add key: " + key);
			mBitmapCache.put(key, bitmap);
		}
	}

	/**
	 * Добавление изображения в кеш
	 * @param key String
	 * @param bitmap Bitmap
	 */
	public static void addBitmap(String key, Bitmap bitmap) {
		if( getBitmap(key) == null ) {
			Log.v(instance.TAG, "Add key: " + key);
			instance.mBitmapCache.put(key, bitmap);
		}

		if( !instance.mDiskCache.exists(key) ) {
			Log.v(instance.TAG, "Add disk cache: " + key);
			instance.mDiskCache.writeFile(key, bitmap);
		}
	}

	/**
	 * Получение фотографии из кеша
	 * @param key String
	 * @return Bitmap
	 */
	public static Bitmap getBitmap(String key) {
		return instance.mBitmapCache.get(key);
	}

	/**
	 * Получение фотографии из кеша диска
	 * @param key String
	 * @return Bitmap
	 */
	public static Bitmap getBitmapDisk(String key) {
		return instance.mDiskCache.getBitmap(key);
	}

	public void setImageSize(int size) {
		image_size = size;
	}
	public int getImageSize() {
		return image_size;
	}

	//Проверяет есть ли изображение на диске в кэше
	public boolean diskExist(String key) {
		return mDiskCache.exists(key);
	}

	//Достаёт изображение с диска
	public Bitmap getDiskImage(String key) {
		return mDiskCache.getBitmap(key);
	}

	//Получаем настройки
	public static BitmapFactory.Options getOpts() {

		//Инициализируем настройки
		BitmapFactory.Options opts = new BitmapFactory.Options();

		//при нехватке памяти битмап будет выгружаться на диск
		opts.inPurgeable = true;
		opts.inTempStorage = new byte[16*1024];

		//Which kind of reference will be used to recover the Bitmap data after being clear, when it will be used in the future
		opts.inInputShareable = true;
		return opts;
	}

	/**
	 * Вычисление размера для получения фотографии
	 * @param options Options
	 * @param reqWidth int
	 * @param reqHeight int
	 * @return int
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and width
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}

	/**
	 * Вычисление размера для получения фотографии
	 * @param width int
	 * @param height int
	 * @param reqWidth int
	 * @param reqHeight int
	 * @return int
	 */
	public static int calculateInSampleSize(int width, int height, int reqWidth, int reqHeight) {
		// Raw height and width of image
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and width
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}

	/**
	 * Получение строки для сохранения в кэш
	 * @param url String
	 * @param w int
	 * @param h int
	 * @return String
	 */
	public static String getUrlHash(String url, int w, int h) {
		return url + ":" + w + "x" + h;
	}

	/**
	 * Загрузка изображения для ленты
	 * @param image_url String
	 * @param imageView ImageCacheView
	 * @param isScrolling boolean
	 * @param w int
	 * @param h int
	 */
	public void loadBitmap(String image_url, final ImageCacheView imageView, boolean isScrolling, int w, int h) {
		//Если изображение не указано
		if( image_url == null || image_url.equals("null") ) {
			setDefaultBitmap(imageView, w, h);
		} else {

			//Строка для хранения кэша
			final String imageKey = getUrlHash(image_url, w, h);
			final Bitmap bitmap = getBitmap(imageKey);

			//Устанавливаем url загрузки
			imageView.url = image_url;
			imageView.is_loaded = false;

			//Если получили изображение
			if( bitmap != null ) {

				//Вставляем
				imageView.setImageBitmap(bitmap);
				imageView.is_loaded = true;
			} else {

				//Очищаем анимацию
				imageView.clearAnimation();

				//Загружаем пустое изображение
				try {
					setDefaultBitmap(imageView, w, h);
				} catch( OutOfMemoryError e ) {
					Log.e(TAG, e.getMessage(), e);
				}

				//Пытаемся получить изображение
				if( !isScrolling && WindowsParam.internetIsAvailable() ) {

					//Если в текущий момент ссылка не загружается
					if( !mCurrentTasks.contains(imageKey) ) {

						//Если кол-во запросов не больше максимально возможных
						if( AsyncTask < MAX_ASYNC_COUNT ) {
							new BitmapLoadAsync(imageView, w, h, true) {
								@Override
								protected void onPostExecute(Bitmap bitmap) {
									super.onPostExecute(bitmap);
									AsyncTask -= 1;
								}
							}.execute(image_url);
							AsyncTask += 1;
						} else {
							Log.e(TAG, "Max Async Task");
						}
					} else {

						//Подписываем хандлер
						if( mTasksHandler.isEmpty() || !mTasksHandler.containsKey(imageKey) ) {
							mTasksHandler.put(imageKey, new ArrayList<BitmapLoadListener>());
						}

						//Добавляем прослушку
						mTasksHandler.get(imageKey).add(new BitmapLoadListener(imageView, image_url, false));
					}
				}
			}
		}
	}

	/**
	 * Загрузка ресурса с использованием кэша
	 * @param resource - ссылка на ресурс
	 */
	public Bitmap loadResource(int resource) {

		//Устанавливаем url загрузки
		String key = "resource_" + resource;

		if( getBitmap(key) != null ) {
			return getBitmap(key);
		} else {
			Bitmap bitmap = BitmapFactory.decodeResource(WindowsParam.getContext().getResources(), resource, getOpts());
			addBitmap(key, bitmap);
			return bitmap;
		}
	}

	/**
	 * Загрузка ресурса с использованием кэша
	 * @param resource - ссылка на ресурс
	 * @param imageView - ссылка на объект изображения
	 */
	public void loadResource(int resource, ImageView imageView) {

		//Устанавливаем url загрузки
		String key = "resource_" + resource;

		if( getBitmap(key) != null ) {
			imageView.setImageBitmap(getBitmap(key));
		} else {
			new BitmapDecodeResourceAsync(imageView).execute(resource);
		}
	}

	/**
	 * Загрузка ресурса с использованием кэша
	 * @param resource - ссылка на ресурс
	 * @param imageView - ссылка на объект изображения
	 */
	public void loadResource(int resource, ImageCacheView imageView) {

		//Устанавливаем url загрузки
		String key = "resource_" + resource;
		imageView.url = key;
		imageView.is_loaded = false;
		if( getBitmap(key) != null ) {
			imageView.setImageBitmap(getBitmap(key));
			imageView.is_loaded = true;
		} else {
			new BitmapDecodeResourceAsync(imageView, true).execute(resource);
		}
	}

	/**
	 * Загрузка ресурка из асетов
	 * @param image_path - путь до изображения
	 * @param imageView - ссылка на объект изображения
	 * @param width - ширина
	 * @param height - высота
	 */
	public void loadAsset(String image_path, ImageCacheView imageView, int width, int height) {

		//Ключь доступа в кеше
		String key = "asset_" + image_path + "_" + width + "_" + height;
		imageView.url = key;
		imageView.is_loaded = false;

		//Пробуем найти в кеше
		if( getBitmap(key) != null ) {
			imageView.setImageBitmap(getBitmap(key));
			imageView.is_loaded = true;
		} else {
			new BitmapDecodeAssetAsync(imageView, width, height).execute(image_path);
		}
	}

	//Устанавливаем стандартный пустой bitmap
	public void setDefaultBitmap(ImageView imageView, int w, int h) {
		Bitmap bitmap = getBitmap("default_bitmap");
		if( bitmap == null || bitmap.getWidth() != w || bitmap.getHeight() != h ) {
			bitmap = Bitmap.createBitmap(w < 1 ? 1 : w, h < 1 ? 1 : h, Bitmap.Config.ARGB_4444);
			Canvas canvas = new Canvas(bitmap);
			canvas.drawColor(Color.TRANSPARENT);
			imageView.setImageBitmap(bitmap);

			//Добавляем в кэш
			addBitmapToCache("default_bitmap", bitmap);
		} else {
			imageView.setImageBitmap(bitmap);
		}
	}

	public Bitmap sdcartBitmap(String path) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, mMaxWidth, mMaxHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;

		//Возвращаем изображение
		return BitmapFactory.decodeFile(path, options);
	}

	//Очистка кеша памяти
	public void clear() {
		Log.i(TAG, "Cache clear");
		mBitmapCache.evictAll();
	}

	public static void removeCache(String key) {
		Log.d(instance.TAG, "Remove key: " + key);
		instance.mBitmapCache.remove(key);
		instance.mDiskCache.removeFile(key);
	}

	/**
	 * Получение изображения из асета
	 *
	 * @param context    Context
	 * @param url        String
	 * @param max_width  int
	 * @param max_height int
	 * @return Bitmap
	 * @throws IOException
	 */
	public static Bitmap decodeSampledBitmapFromAsset(Context context, String url, int max_width, int max_height) throws IOException {

		//Получаем файл
		InputStream is = context.getAssets().open(url);

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(is, null, options);
		is.reset();

		// Calculate inSampleSize
		options.inSampleSize = Cache.calculateInSampleSize(options, max_width, max_height);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;

		return BitmapFactory.decodeStream(is, null, options);
	}
}
