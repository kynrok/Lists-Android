package pnixx.lists.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;

/**
 * Отображает изображение в ленте, увеличивая пропорционально изображение по ширине и высоте
 * User: nixx
 * Date: 25.09.13
 * Time: 12:26
 */
public class ImageProportionView extends ImageCacheView {

	//Ширина и высота изображения
	private int bitmap_width;
	private int bitmap_height;

	public ImageProportionView(Context context) {
		super(context);
	}

	public ImageProportionView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ImageProportionView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void setImageBitmap(Bitmap bm) {

		//Сохраняем данные изображения
		if( bm != null ) {
			bitmap_width = bm.getWidth();
			bitmap_height = bm.getHeight();
		}

		//Выполняем стандартный метод
		super.setImageBitmap(bm);
	}

	/**
	 * Рассчитывает размеры кнопки таким образом, что ее высота никогда не будет меньше
	 * ее ширины. В остальном работает как метод onMeasure класса ImageButton
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		//Получаем ширину и высоту ImageView
		final int height = getMeasuredHeight();
		final int width = getMeasuredWidth();

		//Если было установлено изображение
		if( bitmap_width > 0 && bitmap_height > 0 ) {

			//Подсчитываем процент изменения
			float scale = (float) width / (float) bitmap_width;

			//Задаем новый размер
			setMeasuredDimension(width, (int) (bitmap_height * scale));
		}
	}
}
