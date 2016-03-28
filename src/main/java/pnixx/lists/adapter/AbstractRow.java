package pnixx.lists.adapter;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;

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
	protected void parse(JSONObject r) throws JSONException {

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

						//Если указано создание массива с классом
						if( ArrayList.class.isAssignableFrom(field.getType()) ) {
							ArrayList rows = (ArrayList) field.getType().newInstance();
							JSONArray array = (JSONArray) value;
							Class clz = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
							for( int i = 0; i < array.length(); i++ ) {
								rows.add(clz.getConstructor(JSONObject.class).newInstance(array.get(i)));
							}
							field.set(this, rows);
							continue;
						}

						try {
							//Пробуем в лоб инициализировать класс
							field.set(this, field.getType().getConstructor(value.getClass()).newInstance(value));
						} catch(NoSuchMethodException e) {

							//Если у поля указан тип строки
							if( String.class.isAssignableFrom(field.getType()) ) {
								field.set(this, String.valueOf(value));
							} else {
								field.set(this, value);
							}
						}
					}
				} catch( IllegalArgumentException e ) {
					Log.e("Row", e.getMessage() + ": " + key + "=" + value, e);
				} catch( IllegalAccessException e ) {
					Log.e("Row", e.getMessage(), e);
				} catch(InstantiationException e) {
					Log.e("Row", e.getMessage(), e);
				} catch(NoSuchMethodException e) {
					Log.e("Row", e.getMessage(), e);
				} catch(InvocationTargetException e) {
					Log.e("Row", e.getMessage(), e);
				}
			}
		}
	}
}
