package pnixx.lists.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * User: P.Nixx
 * Date: 02.01.13
 * Time: 17:30
 */
public abstract class AbstractAdapter<Row, Holder extends AbstractHolder<Row>> extends ArrayAdapter {

	protected Context context;
	protected int res;
	protected ArrayList<Row> objects;
	protected static AbstractAdapter instance;
	protected PageScrolling scrolling;
	protected Holder holder;
	protected Row object;

	//Callback на прокрутку к концу списка
	protected OnEndPage onEndPage;
	public static class OnEndPage {
		public void run() {
			instance.onEndPage = null;
		}
	}

	public AbstractAdapter(Context context, int res, ArrayList<Row> objects) {
		super(context, res, objects);
		this.context = context;
		this.res = res;
		this.objects = objects;
		instance = this;
	}

	//Стандартный конструктор
	public AbstractAdapter(Context context, int res, ArrayList<Row> objects, PageScrolling scrolling) {
		super(context, res, objects);
		this.context = context;
		this.res = res;
		this.objects = objects;
		this.scrolling = scrolling;
		instance = this;
		if( scrolling != null ) {
			scrolling.setAdapter(this);
		}
	}

	//Устанавливает колбек на прокрутку к концу страницы
	public void setOnEndPage(OnEndPage onEndPage) {
		this.onEndPage = onEndPage;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if( position >= objects.size() - 1 && onEndPage != null ) {
			onEndPage.run();
			onEndPage = null;
		}

		View row = convertView;

		//Получаем холдер
		if( row == null ) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(res, parent, false);
			holder = initHolder(row);
			row.setTag(holder);
		} else {
			holder = (Holder) row.getTag();
		}

		//Получаем строку объекта
		object = getItem(position);

		//Заполняем данными
		holder.setRow(context, object, scrolling != null && scrolling.isScrolling());

		//Возвращаем вьюху
		return row;
	}

	@Override
	public Row getItem(int i) {
		return (Row) super.getItem(i);
	}

	//Инициализиация холдера
	public abstract Holder initHolder(View view);
}
