package com.jack.admin.query;

import lombok.Data;

@Data
public class DamageListQuery extends BaseQuery{
    private String startDate;
    private String endDate;
}
