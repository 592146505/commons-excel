package org.gwcslife.platform.commons.util.execl.model;


import lombok.Getter;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 表头
 *
 * @author roamer
 * @version V1.0
 * @date 2019-04-01 13:42
 */
public class Header implements Serializable {
    private static final long serialVersionUID = 3099427506156496986L;

    @Getter
    private final List<String> header;

    @Getter
    private final List<String> classList;

    @Getter
    private final List<Map<String, String>> issues;

    public Header(List<String> header) {
        this.header = header;
        // 初始化分类
        classList = header.stream().filter(h -> h.startsWith("疾病分类")).collect(Collectors.toList());
        // 初始化题目
        issues = new ArrayList<>();
        for (int i = 0; i < header.size(); i++) {
            String h = header.get(i);
            Map<String, String> map = new HashMap<>();
            if (h.startsWith("问题")) {
                map.put(h, header.get(++i));
                issues.add(map);
            }
        }
    }

    /**
     * 获取表头个数
     *
     * @return {@code int}
     */
    public int getHeaderCount() {
        return header.size();
    }

    /**
     * 获取分类个数
     *
     * @return {@code int}
     */
    public int getClassCount() {
        return classList.size();
    }

    /**
     * 获取问题个数
     *
     * @return {@code int}
     */
    public int getIssueCount() {
        return issues.size();
    }
}
