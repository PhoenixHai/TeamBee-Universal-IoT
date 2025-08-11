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

package cn.universal.persistence.entity.vo;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IoTDeviceGroupVO implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * 分组ID，非自增
   */
  private Long id;

  /**
   * 分组名称
   */
  private String groupName;

  /**
   * 分组标识
   */
  private String groupCode;

  /**
   * 群组描述
   */
  private String groupDescribe;

  /**
   * 父id
   */
  private Long parentId;

  /**
   * 是否有子分组
   */
  private Integer hasChild;

  /**
   * 分组级别
   */
  private Integer groupLevel;

  /**
   * 设备列表
   */
  private List<IoTDeviceVO> instanceVOS;

  /**
   * 分组子集
   */
  private List<IoTDeviceGroupVO> children;
}
