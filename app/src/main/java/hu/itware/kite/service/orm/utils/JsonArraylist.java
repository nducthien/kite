package hu.itware.kite.service.orm.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by batorig on 2015.09.08..
 */
public class JsonArraylist<T> implements ParameterizedType {

	private Class<?> wrapped;

	public JsonArraylist(Class<T> wrapper)
	{
		this.wrapped = wrapper;
	}

	@Override
	public Type[] getActualTypeArguments()
	{
		return new Type[] { wrapped };
	}

	@Override
	public Type getRawType()
	{
		return List.class;
	}

	@Override
	public Type getOwnerType()
	{
		return null;
	}
}
