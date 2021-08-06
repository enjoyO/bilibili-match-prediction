package top.misec.utils;

public class StringUtils {

    public static boolean isBlank(String str) {
        return str==null || str.trim().length()==0 || str.trim().equalsIgnoreCase("null");
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
}
