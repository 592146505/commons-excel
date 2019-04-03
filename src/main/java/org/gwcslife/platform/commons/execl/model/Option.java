package org.gwcslife.platform.commons.execl.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.gwcslife.platform.commons.util.StringUtils;

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

    /** 选项代码 */
    private String code;

    /** 流向 */
    private String flow;

    /** 下一个问题 */
    private Issue next;

    /** 除外代码 */
    private String exceptCode;

    /**
     * 设置核保结论
     *
     * <p>该方法将会判断核保结论，自动设置流向</p>
     *
     * @param conclusion 核保结论
     * @param remark     例外代码
     *
     * @return {@code this}
     */
    public Option setConclusion(String conclusion, String remark) {
        if (StringUtils.isNotBlank(conclusion)) {
            if ("正常承保".equals(conclusion)) {
                this.setFlow("F05");
            } else if ("非常遗憾，被保险人无法投保该险种。".equals(conclusion)) {
                this.setFlow("F01");
            } else {
                // 存在备注，则为除外
                if (StringUtils.isNotBlank(remark)) {
                    this.setFlow("F04");
                    remark = remark.trim().replace("，", ",");
                    this.setExceptCode(remark);
                }
            }
        }
        return this;
    }

}
