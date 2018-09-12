package hu.itware.kite.service.orm.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Map;

import hu.itware.kite.service.orm.utils.gson.DateDeserializer;

/**
 * Created by batorig on 2015.09.08..
 */
public final class GSON {
	public static final Gson mGSON = new GsonBuilder().registerTypeAdapter(Date.class, new DateDeserializer()).create();

	private GSON() {

	}

	public static <T> T fromJson(JsonElement json, Type typeOfT) throws JsonSyntaxException {
		return mGSON.fromJson(json, typeOfT);
	}

	public static String toJson(Object src) {
		return mGSON.toJson(src);
	}

	public static String toJson(Object src, Type typeOfSrc) {
		return mGSON.toJson(src, typeOfSrc);
	}

	public static void toJson(Object src, Appendable writer) throws JsonIOException {
		mGSON.toJson(src, writer);
	}

	public static void toJson(Object src, Type typeOfSrc, Appendable writer) throws JsonIOException {
		mGSON.toJson(src, typeOfSrc, writer);
	}

	public static void toJson(Object src, Type typeOfSrc, JsonWriter writer) throws JsonIOException {
		mGSON.toJson(src, typeOfSrc, writer);
	}

	public static String toJson(JsonElement jsonElement) {
		return mGSON.toJson(jsonElement);
	}

	public static void toJson(JsonElement jsonElement, Appendable writer) throws JsonIOException {
		mGSON.toJson(jsonElement, writer);
	}

	public static void toJson(JsonElement jsonElement, JsonWriter writer) throws JsonIOException {
		mGSON.toJson(jsonElement, writer);
	}

	public static <T> T fromJson(String json, Class<T> classOfT) throws JsonSyntaxException {
		return mGSON.fromJson(json, classOfT);
	}

	public static <T> T fromJson(String json, Type typeOfT) throws JsonSyntaxException {
		return mGSON.fromJson(json, typeOfT);
	}

	public static <T> T fromJson(Reader json, Class<T> classOfT) throws JsonSyntaxException, JsonIOException {
		return mGSON.fromJson(json, classOfT);
	}

	public static <T> T fromJson(Reader json, Type typeOfT) throws JsonIOException, JsonSyntaxException {
		return mGSON.fromJson(json, typeOfT);
	}

	public static <T> T fromJson(JsonReader reader, Type typeOfT) throws JsonIOException, JsonSyntaxException {
		return mGSON.fromJson(reader, typeOfT);
	}

	public static <T> T fromJson(JsonElement json, Class<T> classOfT) throws JsonSyntaxException {
		return mGSON.fromJson(json, classOfT);
	}

	public static <T> List<T> toList(String json, Class<T> typeClass)
	{
		return mGSON.fromJson(json, new JsonArraylist<T>(typeClass));
	}

	public static LinkedTreeMap<String, String> toMap(String json) {
		Type type = new TypeToken<Map<String, String>>(){}.getType();
		return mGSON.fromJson(json, type);
	}
}
