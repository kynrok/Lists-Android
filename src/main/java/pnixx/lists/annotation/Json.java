package pnixx.lists.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * User: nixx
 * Date: 15.06.15
 * Time: 15:41
 * Contact: http://vk.com/djnixx
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Json {

	//Значение ключа в объекте JSON, если не указано, то берется идентично имени поля в классе
	String value() default "";
}
