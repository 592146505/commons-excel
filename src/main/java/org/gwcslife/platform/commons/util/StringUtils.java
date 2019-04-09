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
     * @param cs 断言字符
     *
     * @return {@code true}
     */
    public static boolean isBlank(CharSequence cs) {
        return org.apache.commons.lang3.StringUtils.isBlank(cs);
    }

    /**
     * 是否不为空
     *
     * @param cs 断言字符
     *
     * @return {@code true}
     */
    public static boolean isNotBlank(CharSequence cs) {
        return !isBlank(cs);
    }
}
