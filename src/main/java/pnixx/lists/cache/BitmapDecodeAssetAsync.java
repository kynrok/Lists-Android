package pnixx.lists.cache;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * User: nixx
 * Date: 10.02.15
 * Time: 18:09
 * Contact: http://vk.com/djnixx
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class BitmapDecodeAssetAsync extends AsyncTask<String, Void, Bitmap> {
	private final WeakReference<ImageCacheView> imageCacheViewReference;
	private final WeakReference<ImageView> imageViewReference;
	private final String key;
	private final int width;
	private final int height;
	private final Context context;

	public BitmapDecodeAssetAsync(ImageCacheView imageView, int width, int height) {
		key = imageView.url;
		// Use a WeakReference to ensure the ImageView can be garbage collected
		imageCacheViewReference = new WeakReference<>(imageView);
		imageViewReference = null;
		context = imageView.getContext();
		this.width = width;
		this.height = height;
	}

	// Decode image in background.
	@Override
	protected Bitmap doInBackground(String... params) {
		String url = params[0];

		try {
			//Получаем ресурс
			Bitmap bitmap = Cache.decodeSampledBitmapFromAsset(context, url, width, height);

			//Добавляем в память
			Cache.addBitmap(key, bitmap);

			//Возвращаем
			return bitmap;
		} catch( IOException e ) {
			Log.e("Bitmap", e.getMessage(), e);
		}
		return null;
	}

	// Once complete, see if ImageView is still around and set bitmap.
	@Override
	protected void onPostExecute(Bitmap bitmap) {
		if( bitmap != null ) {
			final ImageCacheView imageView = imageCacheViewReference.get();
			if( imageView != null ) {

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
}
