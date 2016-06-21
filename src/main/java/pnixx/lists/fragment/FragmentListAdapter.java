package pnixx.lists.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;

import pnixx.lists.R;
import pnixx.lists.adapter.AbstractAdapter;
import pnixx.lists.adapter.AbstractRow;
import pnixx.lists.adapter.Collection;
import pnixx.lists.adapter.PageScrolling;

/**
 * User: nixx
 * Date: 22.03.13
 * Time: 16:31
 */
public abstract class FragmentListAdapter<Row extends AbstractRow> extends ListFragment {

	protected ListView list;
	protected Activity activity;
	protected boolean isActive;
	protected Collection<Row> rows;
	protected PageScrolling pageScrolling;
	protected AbstractAdapter adapter;
	protected View footer_loader;
	private boolean is_footer_attach;
	protected int page = 1;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		isActive = true;
		activity = getActivity();

		//Получаем объекты
		list = getListView();

		//Устанавливаем параметры для списка
		list.setDividerHeight(0);

		//Получаем шаблон для загрузчика
		footer_loader = activity.getLayoutInflater().inflate(R.layout.footer_loader, list, false);

		//Биндим скроллинг
		pageScrolling = new PageScrolling(activity);
		list.setOnScrollListener(pageScrolling);
	}

	@Override
	public void onPause() {
		isActive = false;
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		isActive = true;
	}

	//Получаем данные страницы
	protected abstract void getPage();

	//Установка адаптера
	protected void setAdapter(AbstractAdapter adapter) {
		pageScrolling.setAdapter(adapter);
		list.addFooterView(footer_loader);
		setListAdapter(adapter);
		list.removeFooterView(footer_loader);
	}

	//Устанавливает данные
	protected void setRows(JSONArray r) throws JSONException {
		removeFooterLoader();

		//Если первая страница и были данные
		//	if( page == 1 && rows.size() > 0 ) {
		rows.clear();
		//}

		//Добавляем строки

		rows.append(r);
		adapter.notifyDataSetChanged();
	}

	//Добавление футера
	protected void addFooterLoader() {
		list.addFooterView(footer_loader);
		is_footer_attach = true;
	}

	//Удаление футера
	protected void removeFooterLoader() {
		if( is_footer_attach ) {
			list.removeFooterView(footer_loader);
			is_footer_attach = false;
		}
	}

	//Устанавливаем коллбек на прокрутку страницы
	protected void setCallbackOnEndPageTrack() {
//		if( isActive ) {
//			adapter.setOnEndPage(new AbstractAdapter.OnEndPage() {
//				@Override
//				public void run() {
//					super.run();
//					page += 1;
//					addFooterLoader();
//					getPage();
//				}
//			});
//		}
	}
}