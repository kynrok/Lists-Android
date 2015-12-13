package pnixx.lists.cache;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

import pnixx.lists.core.WindowsParam;

/**
 * User: nixx
 * Date: 11.04.13
 * Time: 12:34
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class BitmapDecodeResourceAsync extends AsyncTask<Integer, Void, Bitmap> {
	private final WeakReference<ImageCacheView> imageCacheViewReference;
	private final WeakReference<ImageView> imageViewReference;
	private int data = 0;

	public BitmapDecodeResourceAsync(ImageCacheView imageView, boolean is_cache) {
		// Use a WeakReference to ensure the ImageView can be garbage collected
		imageCacheViewReference = new WeakReference<ImageCacheView>(imageView);
		imageViewReference = null;
	}

	public BitmapDecodeResourceAsync(ImageView imageView) {
		// Use a WeakReference to ensure the ImageView can be garbage collected
		imageViewReference = new WeakReference<ImageView>(imageView);
		imageCacheViewReference = null;
	}

	// Decode image in background.
	@Override
	protected Bitmap doInBackground(Integer... params) {
		data = params[0];

		//Получаем ресурс
		Bitmap bitmap = decodeSampledBitmapFromResource(WindowsParam.getContext().getResources(), data, 100, 100);

		//Добавляем в память
		Cache.addBitmap("resource_" + data, bitmap);

		//Возвращаем
		return bitmap;
	}

	// Once complete, see if ImageView is still around and set bitmap.
	@Override
	protected void onPostExecute(Bitmap bitmap) {
		if( imageCacheViewReference != null && bitmap != null ) {
			final ImageCacheView imageView = imageCacheViewReference.get();
			if( imageView != null ) {

				//Создаём анимацию
				//todo Можно включать, когда придумаю как сделать нормальный разделитель у юзеров
//				AlphaAnimation animate = new AlphaAnimation(0, 1);
//				animate.setDuration(500);
//				imageView.startAnimation(animate);

				//Вставляем изображение
				imageView.setImageBitmap(bitmap);
				imageView.is_loaded = true;
			}
		}

		//Для обычных изображений
		if( imageViewReference != null && bitmap != null ) {
			final ImageView imageView = imageViewReference.get();
			if( imageView != null ) {
				imageView.setImageBitmap(bitmap);
			}
		}
	}

	/**
	 * Получение изображения из ресурса нужного размера
	 *
	 * @param res       Resources
	 * @param resId     int
	 * @param reqWidth  int
	 * @param reqHeight int
	 * @return Bitmap
	 */
	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		// Calculate inSampleSize
		options.inSampleSize = Cache.calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}
}
