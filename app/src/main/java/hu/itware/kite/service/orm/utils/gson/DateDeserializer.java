package hu.itware.kite.service.orm.utils.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Date;

import hu.itware.kite.service.orm.utils.DateUtils;

/**
 * Created by batorig on 2015.09.18..
 */
public class DateDeserializer implements JsonDeserializer<Date> {

	/** Length of short pattern. */
	private static final int SHORT_LENGTH = "yyyy-MM-dd HH:mm:ss".length();

	@Override
	public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		String dateString = json.getAsString();
		if (dateString == null) {
			return null;
		}
		try {
			if (dateString.length() > SHORT_LENGTH) {
				return DateUtils.getDfLong().parse(dateString);
			} else {
				return DateUtils.getDfShort().parse(dateString);
			}
		} catch (ParseException e) {
			throw new JsonParseException(e.getMessage(), e);
		} catch (Exception e) {
			throw new JsonParseException(e.getMessage(), e);
		}
	}
}
