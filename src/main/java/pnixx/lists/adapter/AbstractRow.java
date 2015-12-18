package pnixx.lists.adapter;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;

import pnixx.lists.annotation.Json;

/**
 * User: nixx
 * Date: 15.06.15
 * Time: 16:15
 * Contact: http://vk.com/djnixx
 */
public abstract class AbstractRow {

	//Пустой конструктор
	public AbstractRow() {}

	//Constructor
	public AbstractRow(JSONObject r) throws JSONException {
		parse(r);
	}

	//Парсинг
	protected final void parse(JSONObject r) throws JSONException {

		//Получаем список полей
		Field[] fields = this.getClass().getFields();

		//Проходим по списку
		for( Field field : fields ) {
			if( field.isAnnotationPresent(Json.class) ) {
				Json json = field.getAnnotation(Json.class);
				String key = json.value().equals("") ? field.getName() : json.value();
				Object value = r.isNull(key) ? null : r.get(key);
				try {
					if( value != null ) {
						field.set(this, value);
					}
				} catch( IllegalArgumentException e ) {
					Log.e("Row", e.getMessage() + ": " + key + "=" + value, e);
				} catch( IllegalAccessException e ) {
					Log.e("Row", e.getMessage(), e);
				}
			}
		}
	}
}
