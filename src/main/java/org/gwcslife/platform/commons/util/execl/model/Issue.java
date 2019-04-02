package org.gwcslife.platform.commons.util.execl.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 问题
 *
 * @author roamer
 * @version V1.0
 * @date 2019-04-01 11:19
 */
@Data
public class Issue implements Serializable {

    private static final long serialVersionUID = -3715183351530890412L;
    /** 标题 */
    private String title;

    /** 分类/问题 */
    private String type;

    /** 选项 */
    private List<Option> option;

    /** 子节点 */
    private List<Issue> children;


}
