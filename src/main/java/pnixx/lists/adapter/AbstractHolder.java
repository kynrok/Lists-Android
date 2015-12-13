package pnixx.lists.adapter;

import android.content.Context;
import android.view.View;

/**
 * User: nixx
 * Date: 17.06.13
 * Time: 10:31
 */
public abstract class AbstractHolder<Row> {
	protected View view;

	//Конструктор
	public AbstractHolder(View view) {
		this.view = view;
	}

	//Достаем вьюху
	public View getView() {
		return view;
	}

	public abstract void setRow(Context context, Row row, boolean is_scrolling);
}
