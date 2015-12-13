package pnixx.lists.cache;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * User: nixx
 * Date: 11.04.13
 * Time: 12:55
 */
public class ImageCacheView extends ImageView {

	public String url = null;
	public boolean is_loaded = false;

	public ImageCacheView(Context context) {
		super(context);
	}

	public ImageCacheView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ImageCacheView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
}
