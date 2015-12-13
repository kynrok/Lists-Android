package pnixx.lists.cache;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.view.animation.AlphaAnimation;

import java.lang.ref.WeakReference;

/**
 * User: nixx
 * Date: 15.04.13
 * Time: 14:00
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class BitmapLoadListener extends AsyncTask<Bitmap, Void, Bitmap> {

	private final WeakReference<ImageCacheView> imageViewReference;
	private final boolean is_animate;
	private final String url;

	public BitmapLoadListener(ImageCacheView imageView, String url, boolean is_animate) {
		// Use a WeakReference to ensure the ImageView can be garbage collected
		imageViewReference = new WeakReference<>(imageView);
		this.url = url;
		this.is_animate = is_animate;
	}

	public BitmapLoadListener(ImageCacheView imageView, String url) {
		// Use a WeakReference to ensure the ImageView can be garbage collected
		imageViewReference = new WeakReference<>(imageView);
		this.url = url;
		is_animate = false;
	}

	@Override
	protected Bitmap doInBackground(Bitmap... bimaps) {
		return bimaps[0];
	}

	@Override
	protected void onPostExecute(Bitmap bitmap) {
		//Отображаем изображение
		if( bitmap != null ) {
			final ImageCacheView imageView = imageViewReference.get();

			//Если ссылка на объект еще не умерла и url изображения совпадает с загружаемым
			if( imageView != null && (imageView.url != null && imageView.url.equals(url)) ) {
				//Если нужна анимация
				if( is_animate ) {
					//Создаём анимацию
					AlphaAnimation animate = new AlphaAnimation(0, 1);
					animate.setDuration(500);
					imageView.startAnimation(animate);
				}

				//Вставляем изображение
				imageView.setImageBitmap(bitmap);
				imageView.is_loaded = true;
			}
		}
	}
}
