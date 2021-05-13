package com.jack.admin.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * <p>
 * 客户退货单表
 * </p>
 *
 * @author Jack
 * @since 2021-05-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_customer_return_list")
@ApiModel(value="CustomerReturnList对象", description="客户退货单表")
public class CustomerReturnList implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "实付金额")
    private Float amountPaid;

    @ApiModelProperty(value = "应付金额")
    private Float amountPayable;

    @ApiModelProperty(value = "退货日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date customerReturnDate;

    @ApiModelProperty(value = "退货单号")
    private String customerReturnNumber;

    @ApiModelProperty(value = "备注")
    private String remarks;

    @ApiModelProperty(value = "交易状态")
    private Integer state;

    @ApiModelProperty(value = "操作用户")
    private Integer userId;

    @ApiModelProperty(value = "客户id")
    private Integer customerId;


}