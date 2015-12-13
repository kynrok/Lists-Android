package pnixx.lists.cache;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.animation.AlphaAnimation;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;

/**
 * User: nixx
 * Date: 11.04.13
 * Time: 12:34
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class BitmapLoadAsync extends AsyncTask<String, Void, Bitmap> {
	private final WeakReference<ImageCacheView> imageViewReference;
	private final boolean is_animate;
	private String url = null;
	private final int w;
	private final int h;

	//Инициализация
	public BitmapLoadAsync(ImageCacheView imageView, int width, int height, boolean is_animate) {
		// Use a WeakReference to ensure the ImageView can be garbage collected
		imageViewReference = new WeakReference<ImageCacheView>(imageView);
		w = width;
		h = height;
		this.is_animate = is_animate;
	}

	// Decode image in background.
	@Override
	protected Bitmap doInBackground(String... params) {
		url = params[0];

		//Добавляем в массив загрузок
		Cache.getInstance().mCurrentTasks.add(Cache.getUrlHash(url, w, h));

		//Пытаемся получить изображение с диска
		Bitmap bitmap = Cache.getBitmapDisk(Cache.getUrlHash(url, w, h));

		//Если изображение не найдено
		if( bitmap == null ) {
			HttpURLConnection conn = null;
			BufferedInputStream buf_stream = null;
			try {
				conn = (HttpURLConnection) new URL(url).openConnection();
				conn.setDoInput(true);
				conn.setRequestProperty("Connection", "Keep-Alive");
				int TIMEOUT_VALUE = 1000;
				conn.setConnectTimeout(TIMEOUT_VALUE);
				conn.setReadTimeout(TIMEOUT_VALUE);
				conn.connect();
				buf_stream = new BufferedInputStream(conn.getInputStream(), 8192);
				bitmap = decodeSampledBitmap(buf_stream);
				buf_stream.close();
				conn.disconnect();
				buf_stream = null;
				conn = null;
			} catch( SocketTimeoutException e ) {
				Log.e("Bitmap", "SocketTimeoutException: " + url);
			} catch( MalformedURLException ex ) {
				Log.e("Bitmap", "Url parsing was failed: " + url);
			} catch( IOException ex ) {
				Log.d("Bitmap", url + " does not exists");
			} catch( OutOfMemoryError e ) {
				Log.w("Bitmap", "Out of memory!!!");
			} finally {
				if( buf_stream != null ) {
					try {
						buf_stream.close();
					} catch( IOException ex ) {
					}
				}
				if( conn != null ) {
					conn.disconnect();
				}
			}
		} else {

			//Добавляем в память
			Cache.addBitmap(Cache.getUrlHash(url, w, h), bitmap);
		}
		return bitmap;
	}

	// Once complete, see if ImageView is still around and set bitmap.
	@Override
	protected void onPostExecute(Bitmap bitmap) {
		final String imageKey = Cache.getUrlHash(url, w, h);
		Cache.getInstance().mCurrentTasks.remove(imageKey);

		//Выполняем все листенеры
		if( !Cache.getInstance().mTasksHandler.isEmpty() && Cache.getInstance().mTasksHandler.containsKey(imageKey) ) {
			ArrayList<BitmapLoadListener> listeners = Cache.getInstance().mTasksHandler.get(imageKey);
			for( BitmapLoadListener listener : listeners) {
				listener.execute(bitmap);
			}

			//Удаляем из массива
			Cache.getInstance().mTasksHandler.remove(imageKey);
		}

		//Отображаем изображение
		if( bitmap != null ) {
			final ImageCacheView imageView = imageViewReference.get();

			//Если ссылка на объект еще не умерла и url изображения совпадает с загружаемым
			if( imageView != null && (imageView.url != null && imageView.url.equals(url)) ) {

				//Если нужна анимация
				if( is_animate && !imageView.is_loaded ) {
					//Создаём анимацию
					AlphaAnimation animate = new AlphaAnimation(0, 1);
					animate.setDuration(300);
					imageView.startAnimation(animate);
				}

				//Вставляем изображение
				imageView.setImageBitmap(bitmap);
				imageView.is_loaded = true;
			}
		}
	}

	/**
	 * Получение изображения из ресурса нужного размера
	 *
	 * @param is InputStream
	 * @return Bitmap
	 */
	public Bitmap decodeSampledBitmap(InputStream is) {

		// Decode bitmap with inSampleSize set
		Bitmap bitmap = BitmapFactory.decodeStream(is, null, Cache.getOpts());

		//Add to cache
		if( bitmap != null ) {

			//Преобразуем ширину и высоту
			if( bitmap.getWidth() != w || bitmap.getHeight() != h ) {
				bitmap = Bitmap.createScaledBitmap(bitmap, w, h, false);
			}

			//Сохраняем в кэш
			Cache.addBitmap(Cache.getUrlHash(url, w, h), bitmap);
		}

		//Возвращаем
		return bitmap;
	}
}
