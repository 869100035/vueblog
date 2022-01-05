package com.markerhub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_con_record")
public class Records implements Serializable {

    @TableId(value = "id",type = IdType.AUTO)
    private String id;

    private String userId;
    @NotBlank
    private String conType;
    @NotBlank
    private String title;
    @Digits(integer = 8,fraction = 2,message = "金额格式不正确")
    private BigDecimal conMoney;

    private String conTime;

    private String remark;

    private String crtTime;

    private String updTime;
}
