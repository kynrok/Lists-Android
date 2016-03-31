package pnixx.lists.adapter;

import android.util.AndroidRuntimeException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/**
 * Создание коллекции
 * Created by nixx on 17.12.15.
 */
public final class Collection<T extends AbstractRow> extends ArrayList<T> {

	private final Class<T> clazz;

	//Простая инициализация
	public Collection(Class<T> clz) {
		clazz = clz;
	}

	//Конструктор списка для адаптера
	public Collection(Class<T> clz, JSONArray r) throws JSONException {
		try {
			clazz = clz;

			//Проходим по списку элементов
			for(int i = 0; i < r.length(); i++) {
				add(newInstance(r.getJSONObject(i)));
			}

		} catch(Exception e) {
			throw new AndroidRuntimeException("Unable to instantiate collection " + clz.getName(), e);
		}
	}

	private T newInstance(JSONObject r) throws JSONException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		try {
			return clazz.getConstructor(JSONObject.class).newInstance(r);
		} catch( NoSuchMethodException e ) {
			T row = clazz.newInstance();
			row.parse(r);
			return row;
		}
	}

	//Добавление в список
	public void append(JSONArray r) throws JSONException {
		try {
			//Проходим по списку элементов
			for(int i = 0; i < r.length(); i++) {
				add(newInstance(r.getJSONObject(i)));
			}
		} catch(Exception e) {
			throw new AndroidRuntimeException("Unable to instantiate collection " + clazz.getName(), e);
		}
	}
}
