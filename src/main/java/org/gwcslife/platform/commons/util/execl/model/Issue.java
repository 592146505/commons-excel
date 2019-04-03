package org.gwcslife.platform.commons.util.execl.model;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 问题
 *
 * @author roamer
 * @version V1.0
 * @date 2019-04-01 11:19
 */
@Data
@ToString(exclude = {"type"})
public class Issue implements Serializable {

    private static final long serialVersionUID = -3715183351530890412L;
    /** 标题 */
    private String title;

    /** 分类/问题 */
    private String type;

    /** 选项 */
    private List<Option> options = new ArrayList<>();

    /** 子节点 */
    private List<Issue> children = new ArrayList<>();

    public void addOption(Option option) {
        options.add(option);
    }


}
