package pnixx.lists.adapter;

import android.content.Context;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;

/**
 * User: P.Nixx
 * Date: 09.11.12
 * Time: 12:14
 */
public class PageScrolling implements AbsListView.OnScrollListener {
	private boolean mIsScrolling;
	private ArrayAdapter<?> adapter;
	private final Context context;

	public PageScrolling(Context context) {
		this.context = context;
	}

	public void setAdapter(ArrayAdapter<?> adapter) {
		this.adapter = adapter;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// Set scrolling to true only if the user has flinged the
		// ListView away, hence we skip downloading a series
		// of unnecessary bitmaps that the user probably
		// just want to skip anyways. If we scroll slowly it
		// will still download bitmaps - that means
		// that the application won't wait for the user
		// to lift its finger off the screen in order to
		// download.
		if( scrollState == SCROLL_STATE_FLING /*|| scrollState == SCROLL_STATE_TOUCH_SCROLL*/ ) {
			mIsScrolling = true;
		} else {
			mIsScrolling = false;
			if( adapter != null ) {
				adapter.notifyDataSetChanged();
			}
		}
	}

	@Override
	public void onScroll(AbsListView absListView, int i, int i1, int i2) {
		// TODO Auto-generated method stub
	}

	public boolean isScrolling() {
		return mIsScrolling;
	}

}
