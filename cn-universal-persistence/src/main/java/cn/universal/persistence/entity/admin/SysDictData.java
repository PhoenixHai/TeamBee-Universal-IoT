/*
 *
 * Copyright (c) 2025, iot-Universal. All Rights Reserved.
 *
 * @Description: 本文件由 Aleo 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: Aleo
 * @Email: wo8335224@gmail.com
 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.persistence.entity.admin;

import cn.universal.core.iot.constant.UserConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 字典数据表 sys_dict_data @Author ruoyi
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@Table(name = "sys_dict_data")
public class SysDictData implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * 字典编码
   */
  //  @Excel(name = "字典编码", cellType = Excel.ColumnType.NUMERIC)
  @Id
  private Long dictCode;

  /**
   * 字典排序
   */
  //  @Excel(name = "字典排序", cellType = Excel.ColumnType.NUMERIC)
  @Column(name = "dict_sort")
  private Long dictSort;

  /**
   * 字典标签
   */
  //  @Excel(name = "字典标签")
  @NotBlank(message = "字典标签不能为空")
  @Size(min = 0, max = 100, message = "字典标签长度不能超过100个字符")
  @Column(name = "dict_label")
  private String dictLabel;

  /**
   * 字典键值
   */
  //  @Excel(name = "字典键值")
  @NotBlank(message = "字典键值不能为空")
  @Column(name = "dict_value")
  @Size(min = 0, max = 100, message = "字典键值长度不能超过100个字符")
  private String dictValue;

  /**
   * 字典类型
   */
  //  @Excel(name = "字典类型")
  @NotBlank(message = "字典类型不能为空")
  @Column(name = "dict_type")
  @Size(min = 0, max = 100, message = "字典类型长度不能超过100个字符")
  private String dictType;

  /**
   * 样式属性（其他样式扩展）
   */
  @Size(min = 0, max = 100, message = "样式属性长度不能超过100个字符")
  @Column(name = "css_class")
  private String cssClass;

  /**
   * 表格字典样式
   */
  @Column(name = "list_class")
  private String listClass;

  /**
   * 是否默认（Y是 N否）
   */
  //  @Excel(name = "是否默认", readConverterExp = "Y=是,N=否")
  @Column(name = "is_default")
  private String isDefault;

  /**
   * 状态（0正常 1停用）
   */
  //  @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
  private String status;

  /**
   * 创建者
   */
  //  @TableField(fill = FieldFill.INSERT)
  @Column(name = "create_by")
  private String createBy;

  /**
   * 创建时间
   */
  //  @TableField(fill = FieldFill.INSERT)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Column(name = "create_time")
  private Date createTime;

  /**
   * 更新者
   */
  //  @TableField(fill = FieldFill.INSERT_UPDATE)
  @Column(name = "update_by")
  private String updateBy;

  /**
   * 更新时间
   */
  //  @TableField(fill = FieldFill.INSERT_UPDATE)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Column(name = "update_time")
  private Date updateTime;

  /**
   * 备注
   */
  private String remark;

  /**
   * 请求参数
   */
  //  @TableField(exist = false)
  private Map<String, Object> params = new HashMap<>();

  public boolean getDefault() {
    return UserConstants.YES.equals(this.isDefault);
  }
}
