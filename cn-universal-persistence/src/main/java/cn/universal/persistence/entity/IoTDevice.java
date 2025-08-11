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

package cn.universal.persistence.entity;

import cn.universal.core.annotation.Excel;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "iot_device")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IoTDevice implements Serializable {

  private static final long serialVersionUID = 1L;
  @Id
  private Long id;

  /**
   * 对外设备唯一标识符
   */
  @Column(name = "iot_id")
  private String iotId;

  /**
   * 设备自身序号
   */
  @Excel(name = "设备序列号")
  @Column(name = "device_id")
  private String deviceId;

  /**
   * 实例名称
   */
  // @Excel(name = "实例名称")
  @Column(name = "instance")
  private String instance;

  /**
   * 归属应用
   */
  @Column(name = "application")
  private String application;

  /**
   * 激活时间
   */
  @Column(name = "registry_time")
  private Integer registryTime;

  /**
   * 最后上线时间
   */
  @Column(name = "online_time")
  private Long onlineTime;

  /**
   * 第三方设备ID唯一标识符
   */
  @Column(name = "ext_device_id")
  private String extDeviceId;

  /**
   * 别名
   */
  @Column(name = "nick_name")
  private String nickName;

  /**
   * 设备名称
   */
  @Column(name = "product_name")
  private String productName;

  private Long features;

  /**
   * 网关产品ProductKey
   */
  @Excel(name = "网关产品ProductKey")
  @Column(name = "gw_product_key")
  private String gwProductKey;

  /**
   * 设备密钥
   */
  @Excel(name = "设备密钥")
  @Column(name = "device_secret")
  private String deviceSecret;

  /**
   * 产品key
   */
  @Column(name = "product_key")
  private String productKey;

  /**
   * 设备实例名称
   */
  @Excel(name = "设备名称")
  @Column(name = "device_name")
  private String deviceName;

  @Column(name = "creator_id")
  private String creatorId;

  @Column(name = "creator_name")
  private String creatorName;

  /**
   * 0-离线，1-在线
   */
  @Excel(name = "在线状态")
  private Boolean state;

  /**
   * 说明
   */
  @Excel(name = "备注")
  private String detail;

  @Column(name = "create_time")
  private Long createTime;

  /**
   * 派生元数据,有的设备的属性，功能，事件可能会动态的添加
   */
  @Column(name = "derive_metadata")
  private String deriveMetadata;

  /**
   * 其他配置
   */
  @Column(name = "configuration")
  private String configuration;

  /**
   * 区域ID
   */
  private String areasId;

  /**
   * 坐标
   */
  // @Excel(name = "设备坐标")
  private String coordinate;

  private String deviceNode;

  /**
   * 纬度
   */
  @Excel(name = "纬度")
  private transient String latitude;

  /**
   * 经度
   */
  @Excel(name = "经度")
  private transient String longitude;

  /**
   * 请求参数
   */
  @Builder.Default
  private Map<String, Object> params = new HashMap<>();

  /**
   * 接收额外参数
   */
  @Excel(name = "其他配置")
  @Builder.Default
  private Map<String, Object> otherParams = new HashMap<>();
}
