package org.gwcslife.platform.commons.util;

/**
 * 字符串工具类
 *
 * @author roamer
 * @version V1.0
 * @date 2019-04-03 15:22
 */
public abstract class StringUtils {
    /**
     * 是否为空
     *
     * @param str 断言字符
     *
     * @return {@code true}
     */
    public static boolean isBlank(String str) {
        return str == null || "".equals(str.trim());
    }

    /**
     * 是否不为空
     *
     * @param str 断言字符
     *
     * @return {@code true}
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
}
