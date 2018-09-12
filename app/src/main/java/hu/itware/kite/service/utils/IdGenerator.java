package hu.itware.kite.service.utils;

import hu.itware.kite.service.orm.model.Uzletkoto;
import hu.itware.kite.service.services.LoginService;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

public final class IdGenerator {

	private static String CHARS = "0123456789ABCDEFGHIJKLMNPQRSTUVWXYZ";
	
	@SuppressLint("TrulyRandom")
	private static final SecureRandom random = new SecureRandom();


	private static final SimpleDateFormat DF = new SimpleDateFormat("yyyyMMdd");

	private static final SimpleDateFormat DF_IN = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss SSS");


	private IdGenerator() {

	}

	/**
	 * Generate ID with mobileszkoz.azon + random generated value
	 * @param context the context where can get the mobileszkoz.azon (2 character)
	 * @param length the total length of the id inculde mobileszkoz.azon
	 * @return the generated azon
	 */
	public static String generate(Context context, int length) {

		Uzletkoto uzletkoto = LoginService.getManager(context);
		if (uzletkoto == null || uzletkoto.azon == null || uzletkoto.azon.length() >= length) {
			return new BigInteger((length) * 4 , random).toString(16).toUpperCase();
		}

		int lengthDiff = length - uzletkoto.azon.length();
		int max = (int)Math.pow(CHARS.length() - 1d, lengthDiff);
		String v =  new BigInteger("" + random.nextInt(max)).toString(35).toUpperCase();
		while (v.length() < lengthDiff) {
			v = "0" + v;
		}
		return uzletkoto.azon + v.replace("O", "Z");
	}

	/**
	 * Generate an unique id based on the current time and Uzletkoto azon
	 * @param context Application Context
	 * @return the generated ID.
	 */
	public static String generate(Context context) {
		String azon = LoginService.getManager(context).azon;
		if (azon == null || azon.isEmpty()) {
			azon = "XX";
			Log.e("KITE.IDGENERATOR", "Uzletkoto.azon field is null. Set it to default='XX'");
		}
		return generate(azon, Calendar.getInstance());
	}

	/**
	 * Generate an unique id based on the current time and Uzletkoto azon
	 * @param azon Uzletkoto azon
	 * @return the generated ID.
	 */
	public static String generate(String azon) {
		return generate(azon, Calendar.getInstance());
	}

	/**
	 * Generate an unique id based on the current time and Uzletkoto azon
	 * @param azon Uzletkoto azon
	 * @param calendar current calendar
	 * @return the generated ID.
	 */
	public static String generate(String azon, Calendar calendar) {
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int min = calendar.get(Calendar.MINUTE);
		int sec = calendar.get(Calendar.SECOND);
		int ms = calendar.get(Calendar.MILLISECOND);
		int secTotal = hour * 60 * 60 + min * 60 + sec;
		return String.format("%s%s%05d%03d", azon, DF.format(calendar.getTime()), secTotal, ms);
	}

	/**
	 * Generate id from the date string (for testing only)
	 * @param azon the Uzletkoto azon
	 * @param dateStr the date string in format "yyyy.MM.dd HH:mm:ss SSS"
	 * @return the generated ID.
	 */
	private static String generate(String azon, String dateStr) {
		try {
			Date date = DF_IN.parse(dateStr);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			return generate(azon, calendar);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Generate ID, starts with the @base string and the TOTAL length with @length
	 * @param base the prefix of the generated id
	 * @param length the total length of the generated id (include the prefix)
	 * @return generated id
	 */
	@Deprecated
	public static String generate(String base, int length) {

		int lengthDiff = length - base.length();
		if (lengthDiff <= 0) {
			return base;
		}

		int max = (int)Math.pow(CHARS.length() - 1d, lengthDiff);
		String v =  new BigInteger("" + random.nextInt(max)).toString(35).toUpperCase();
		while (v.length() < lengthDiff) {
			v = "0" + v;
		}
		return base + v.replace("O", "Z");
	}

	@Deprecated
	public static String generateGEW(String base, String sequence, int length) {
		if ("".equals(sequence)) {
			return base + leftPad("1", length);
		}
		int next = parseBase35(sequence.substring(base.length())) + 1;
		return base + leftPad(toBase35(next), length);
	}

	public static String generateNextInSequence(String base, String sequence, int length) {
		if ("".equals(sequence)) {
			return base + leftPad("1", length);
		}
		sequence = sequence.substring(base.length());
		String result = "";
		int digitCount = countDigits(sequence);
		String digits = sequence.substring(sequence.length() - digitCount);
		String alpha = sequence.substring(0, sequence.length() - digitCount);
		long number;
		try {
			number = Long.valueOf(digits) + 1;
		} catch (Exception e) {
			number = 1;
		}
		if (number == Math.pow(10, digitCount) || number == Math.pow(10, length)) {
			boolean increase = true;
			String newAlpha = "";
			for (int i = alpha.length() - 1; i > -1; i--) {
				int c = alpha.charAt(i);
				if (increase) {
					c++;
					if (c < 'Z' + 1) {
						increase = false;
					} else {
						c = 'A';
					}
				}
				newAlpha = (char) c + newAlpha;
			}
			if (increase) {
				newAlpha = "A" + newAlpha;
			}
			result = newAlpha + leftPad("1", length - newAlpha.length());
		} else {
			result = alpha + leftPad(Long.toString(number), length - alpha.length());
		}
		return base + result;
	}

	private static int countDigits(String s) {
		int count = 0;
		for (int i = 0, len = s.length(); i < len; i++) {
			if (Character.isDigit(s.charAt(i))) {
				count++;
			}
		}
		return count;
	}

	private static String toBase35(int num) {
		String result = "";
		while(num > 0) {
			result = CHARS.charAt(num%CHARS.length()) + result;
			num = (int)Math.floor(num/CHARS.length());
		}
		return result;
	}

	private static int parseBase35(String num) {
		int result = 0;
		for (int i = num.length()-1; i >= 0; i--) {
			result += CHARS.indexOf(num.charAt(i)) * Math.pow(CHARS.length(), num.length() - i - 1);
		}
		return result;
	}

	private static String leftPad(String s, int length) {
		int l = s.length();
		for (int i = 0; i < length - l; i++) {
			s = "0" + s;
		}
		return s;
	}

	@Deprecated
	public static String generateOLD(Context context, int length) {
		
		Uzletkoto uzletkoto = LoginService.getManager(context);
		if (uzletkoto == null || uzletkoto.nui == null) {
			new BigInteger((length + 2) * 4 , random).toString(16).toUpperCase();
		}
		
		String nui = uzletkoto.nui;
		if (nui.length() > 2) {
			nui = nui.substring(0, 2);
		}
		
		return nui + new BigInteger(length * 4 , random).toString(16).toUpperCase();
	}

}
