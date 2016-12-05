package ru.pushkarev.LogsSearcher.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Created by Opushkarev on 25.11.2016.
 */
public class RegExUtils {
    private static Logger log = Logger.getLogger(RegExUtils.class.getName());

    private static HashMap<String, String> findstrReplaceMap = new HashMap<String, String>() {{
        put("\\d", "[0-9]");
        put("\\s", "[ ]");
        put("\\S", "[^ ]");
        put("\\w", "[abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_]");
        put("\\W", "[^abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_]");
    }};

    public static String convertToFindstrFormat(String rePattern) {
        String result = rePattern;

        if (rePattern != null) {
            for (Map.Entry entry : findstrReplaceMap.entrySet()) {
                String javaPattern = Pattern.quote(entry.getKey().toString());
                String findstPattern = entry.getValue().toString();
                result = result.replaceAll(javaPattern, findstPattern);
            }
        }

        return result;
    }

    public static boolean isValidRegExp(String searchText) throws PatternSyntaxException{
        try {
            Pattern.compile(searchText);  // test if expression is valid
        } catch (PatternSyntaxException e) {
            String msg = "Invalid RegExp syntax :" + e.getMessage();
            log.severe(msg);
//            throw new PatternSyntaxException(msg, searchText, 0);
            return false;
        }
        return true;
    }

    private RegExUtils() {}
}
