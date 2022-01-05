package com.markerhub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_con_type")
public class ConType {

    @TableId(value = "id",type = IdType.AUTO)
    private String id;

    private String userId;

    private String typeCd;

    private String typeName;

    private String crtTime;

    private String updTime;
}
