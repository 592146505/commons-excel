package org.gwcslife.platform.commons.execl.model;


import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final List<String> classList = new ArrayList<>();

    @Getter
    private final List<Map<String, String>> issues = new ArrayList<>();

    @Getter
    private int conclusionColumn = -1;

    public Header(List<String> header) {
        this.header = header;
        // 初始化分类
        for (int i = 0; i < header.size(); i++) {
            String h = header.get(i);
            if (h.startsWith("病种分类")) {
                classList.add(h);
            } else if (h.startsWith("问题")) {
                Map<String, String> map = new HashMap<>();
                map.put(h, header.get(++i));
                issues.add(map);
            } else if (h.startsWith("核保结论")) {
                conclusionColumn = i;
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
     * 获取分类开始列
     * <p>开始列从0开始</p>
     *
     * @return {@code int}
     */
    public int getClassStartColumn() {
        return 1;
    }

    /**
     * 获取分类开始列
     * <p>开始列从0开始</p>
     *
     * @return {@code int}
     */
    public int getIssueStartColumn() {
        return getClassStartColumn() + classList.size();
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
