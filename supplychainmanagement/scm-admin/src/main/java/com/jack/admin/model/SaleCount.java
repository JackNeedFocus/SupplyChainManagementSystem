package com.jack.admin.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 乐字节  踏实教育 用心服务
 *
 * @author 乐字节--老李
 * @version 1.0
 */
@Data
public class SaleCount {
    private float amountCost; //

    private float amountSale; // 销售总金额

    private float amountProfit; // 销售利润

    private String date; // 日期
}
