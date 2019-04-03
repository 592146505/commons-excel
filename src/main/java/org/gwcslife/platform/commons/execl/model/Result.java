package org.gwcslife.platform.commons.execl.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 结果集
 *
 * @author roamer
 * @version V1.0
 * @date 2019-04-01 13:50
 */
@Data
public class Result implements Serializable {

    private static final long serialVersionUID = -1689000076715261893L;

    private final Header header;

}
