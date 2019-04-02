package org.gwcslife.platform.commons.util.execl.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 选项
 *
 * @author roamer
 * @version V1.0
 * @date 2019-04-01 14:12
 */
@Data
@Accessors(chain = true)
public class Option implements Serializable {
    private static final long serialVersionUID = 998844023568822743L;

    /**
     * 选项内容
     */
    private String content;

    /** 流向 */
    private String flow;

    /** 下一个问题 */
    private Issue next;

//    public Option setFlow(String conclusion){
//        if ()
//        return this;
//    }
}
