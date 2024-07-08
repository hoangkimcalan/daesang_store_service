package com.epay.ewallet.store.daesang.utility;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Utils {

    private static final char[] SOURCE_CHARACTERS = {
            'a', 'à', 'ả', 'ã', 'á', 'ạ',
            'ă', 'ằ', 'ẳ', 'ẵ', 'ắ', 'ặ',
            'â', 'ầ', 'ẩ', 'ẫ', 'ấ', 'ậ',
            'e', 'è', 'ẻ', 'ẽ', 'é', 'ẹ',
            'ê', 'ề', 'ể', 'ễ', 'ế', 'ệ',
            'i', 'ì', 'ỉ', 'ĩ', 'í', 'ị',
            'o', 'ò', 'ỏ', 'õ', 'ó', 'ọ',
            'ô', 'ồ', 'ổ', 'ỗ', 'ố', 'ộ',
            'ơ', 'ờ', 'ở', 'ỡ', 'ớ', 'ợ',
            'u', 'ù', 'ủ', 'ũ', 'ú', 'ụ',
            'ư', 'ừ', 'ử', 'ữ', 'ứ', 'ự',
            'y', 'ỳ', 'ỷ', 'ỹ', 'ý', 'ỵ',
            'A', 'À', 'Ả', 'Ã', 'Á', 'Ạ',
            'Ă', 'Ằ', 'Ẳ', 'Ẵ', 'Ắ', 'Ặ',
            'Â', 'Ầ', 'Ẩ', 'Ẫ', 'Ấ', 'Ậ',
            'E', 'È', 'Ẻ', 'Ẽ', 'É', 'Ẹ',
            'Ê', 'Ề', 'Ể', 'Ễ', 'Ế', 'Ệ',
            'I', 'Ì', 'Ỉ', 'Ĩ', 'Í', 'Ị',
            'O', 'Ò', 'Ỏ', 'Õ', 'Ó', 'Ọ',
            'Ô', 'Ồ', 'Ổ', 'Ỗ', 'Ố', 'Ộ',
            'Ơ', 'Ờ', 'Ở', 'Ỡ', 'Ớ', 'Ợ',
            'U', 'Ù', 'Ủ', 'Ũ', 'Ú', 'Ụ',
            'Ư', 'Ừ', 'Ử', 'Ữ', 'Ứ', 'Ự',
            'Y', 'Ỳ', 'Ỷ', 'Ỹ', 'Ý', 'Ỵ',
            'd', 'đ', 'D', 'Đ'
    };

    private static final char[] DESTINATION_CHARACTERS = {
            'a', 'a', 'a', 'a', 'a', 'a',
            'a', 'a', 'a', 'a', 'a', 'a',
            'a', 'a', 'a', 'a', 'a', 'a',
            'e', 'e', 'e', 'e', 'e', 'e',
            'e', 'e', 'e', 'e', 'e', 'e',
            'i', 'i', 'i', 'i', 'i', 'i',
            'o', 'o', 'o', 'o', 'o', 'o',
            'o', 'o', 'o', 'o', 'o', 'o',
            'o', 'o', 'o', 'o', 'o', 'o',
            'u', 'u', 'u', 'u', 'u', 'u',
            'u', 'u', 'u', 'u', 'u', 'u',
            'y', 'y', 'y', 'y', 'y', 'y',
            'A', 'A', 'A', 'A', 'A', 'A',
            'A', 'A', 'A', 'A', 'A', 'A',
            'A', 'A', 'A', 'A', 'A', 'A',
            'E', 'E', 'E', 'E', 'E', 'E',
            'E', 'E', 'E', 'E', 'E', 'E',
            'I', 'I', 'I', 'I', 'I', 'I',
            'O', 'O', 'O', 'O', 'O', 'O',
            'O', 'O', 'O', 'O', 'O', 'O',
            'O', 'O', 'O', 'O', 'O', 'O',
            'U', 'U', 'U', 'U', 'U', 'U',
            'U', 'U', 'U', 'U', 'U', 'U',
            'Y', 'Y', 'Y', 'Y', 'Y', 'Y',
            'd', 'd', 'D', 'D'
    };

    public static char removeAccent(char ch) {
        // int index = Arrays.binarySearch(SOURCE_CHARACTERS, ch);
        int index = -1;
        for (int i = 0; i < SOURCE_CHARACTERS.length; i++) {
            if (SOURCE_CHARACTERS[i] == (ch)) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            ch = DESTINATION_CHARACTERS[index];
        }
        return ch;
    }

    public static String removeAccent(String str) {
        StringBuilder sb = new StringBuilder(str);
        for (int i = 0; i < sb.length(); i++) {
            sb.setCharAt(i, removeAccent(sb.charAt(i)));
        }
        return sb.toString();
    }

    public static String formatName(String name) {
        name = name.trim().replaceAll(" +", " ");
        name = Utils.removeAccent(name);
        return name.toUpperCase();
    }

    public static String getDeviceIdFromHeader(Map<String, String> header) {
        String deviceId = "";

        Gson gson = new Gson();
        Map<String, String> device = gson.fromJson(header.get("device"),
                new TypeToken<Map<String, String>>() {
                }.getType());
        if (device != null) {
            deviceId = device.get("deviceId");
        }

        return deviceId;
    }
    
    public static boolean isStringEmpty(String s) {
    	return s == null || s.trim().isEmpty();
    }
    
    /**
     * Convert a string to an array of string by splitting the source string by separator and removing all of the space
     * @param source
     * @param separator
     * @return String[]
     */
    public static String[] convertStringToArray(String source, char separator) {
    	String[] output = null;
    	if (source == null || source.isEmpty())
    		return output;
    	output = source.replaceAll(" ", "").split(String.valueOf(separator));
    	return output;
    }

}
