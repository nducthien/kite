package hu.itware.kite.service.utils;

import android.content.Context;
import android.content.res.Resources;
import android.text.InputFilter;
import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.regex.Pattern;

import hu.itware.kite.service.orm.model.Partner;

public final class StringUtils {
	
	public static final Pattern NORMALIZER = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

	private static final Pattern SZIG_PATTERN_A = Pattern.compile("^[0-9]{6}[\\- ]?[a-zA-Z]{2}$");
	private static final Pattern SZIG_PATTERN_B = Pattern.compile("^[a-zA-Z]{2}[\\- ]?(?:M{0,4}CM|CD|D?C{0,3}XC|XL|L?X{0,3}IX|IV|V?I{0,3}[\\- ]?)?[0-9]{6}$");

	private StringUtils() {

	}

	public static HashMap<String, String> buildMap(Context context, int stringMapId) {

		Resources resources = context.getResources();
		String[] array = resources.getStringArray(stringMapId);

		HashMap<String, String> result = new HashMap<String, String>();
		if (array != null) {
			for (String item : array) {
				String[] mapItem = item.split("\\|");
				result.put(mapItem[0], mapItem[1]);
			}
		}

		return result;
	}

	public static boolean isSzemlyiIgazolvanySzam(String szig) {
		return SZIG_PATTERN_A.matcher(szig).matches() ||  SZIG_PATTERN_B.matcher(szig).matches() || "".equals(szig);
	}

	public static boolean isEmpty(String s) {
		return s == null || s.trim().length() == 0 || "null".equalsIgnoreCase(s);
	}

	public static String emptyString(int length, char c) {
		return new String(new char[length]).replace('\0', c);
	}

	public static String fillLength(String original, int length) {
		return fillLength(original, length, false);
	}

	public static String fillLength(String original, int length, boolean left) {
		if (original == null) {
			return emptyString(length, ' ');
		}

		if (original.length() >= length) {
			return original;
		}

		if (!left) {
			return original + emptyString(length - original.length(), ' ');
		} else {
			return emptyString(length - original.length(), ' ') + original;
		}
	}

	public static String clearText(String s) {

		if (s == null) {
			return s;
		}

		String result = s.toLowerCase()
				.replaceAll("[õöóő]", "o")
				.replaceAll("á", "a")
				.replaceAll("[ûüúű]", "u")
				.replaceAll("[í]", "i")
				.replaceAll("[é]", "e");
		return result;
	}

	public static final String md5(final String s) {
		final String MD5 = "MD5";
		try {
			// create MD5 Hash
			MessageDigest digest = java.security.MessageDigest.getInstance(MD5);
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			// create Hex String
			StringBuilder hexString = new StringBuilder();
			for (byte aMessageDigest : messageDigest) {
				String h = Integer.toHexString(0xFF & aMessageDigest);
				while (h.length() < 2)
					h = "0" + h;
				hexString.append(h);
			}
			return hexString.toString().toUpperCase();

		} catch (NoSuchAlgorithmException e) {
			Log.e("StringUtils", "No such method", e);
		}
		return "";
	}

	public static String collateName(String... items) {
		
		if (items == null) {
			return null;
		}
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < items.length; i++) {
			if (items[i] != null) {
				sb.append(items[i]);
			}
			
			if (i < items.length - 1) {
				sb.append(" ");
			}
			
		}
		
		String nfdNormalizedString = Normalizer.normalize(sb.toString().toUpperCase(), Normalizer.Form.NFD); 
	    return NORMALIZER.matcher(nfdNormalizedString).replaceAll("");
	}

	public static String[] splitName(String name) {
		String[] parts = new String[2];

		if (!isEmpty(name)) {
			parts[0] = name.substring(0, name.length() > Partner.PARTNER_NAME_LENGTH ? Partner.PARTNER_NAME_LENGTH : name.length()).trim();
			parts[1] = name.length() > Partner.PARTNER_NAME_LENGTH ? name.substring(Partner.PARTNER_NAME_LENGTH, name.length()).trim() : null;
		}

		return parts;
	}

	public static InputFilter[] addInputFilterToArray(InputFilter[] array, InputFilter filterToAdd) {
		InputFilter[] newArray = new InputFilter[(array == null ? 0 : array.length) + (filterToAdd == null ? 0 : 1)];

		if (array != null && array.length > 0) {
			System.arraycopy(array, 0, newArray, 0, array.length);
		}

		if (filterToAdd != null) {
			newArray[newArray.length - 1] = filterToAdd;
		}

		return newArray;
	}

	public static String breakLongString(String text, int maxLength) {
		String[] parts = text.split(" ");
		String result = "";
		for (String part : parts) {
			if (part.length() > maxLength) {
				String broken = "";
				while (part.length() > 0) {
					String subpart = part.substring(0, part.length() > maxLength ? maxLength : part.length());
					broken += (broken.length() > 0 ? " " : "") + subpart;
					if (part.equals(subpart)) {
						break;
					} else {
						part = part.substring(maxLength);
					}
				}
				result += (result.length() > 0 ? " " : "") + broken;
			} else {
				result += (result.length() > 0 ? " " : "") + part;
			}
		}
		return result;
	}

	public static String encodeToBase64(String s) {
		if (s == null || s.isEmpty()) {
			return s;
		}

		byte[] b = s.getBytes();
		return Base64.encodeToString(b, Base64.NO_WRAP);
	}
}
