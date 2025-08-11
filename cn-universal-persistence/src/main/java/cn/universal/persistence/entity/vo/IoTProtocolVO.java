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

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "iot_device_protocol")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IoTProtocolVO implements Serializable {

  private static final long serialVersionUID = 1L;
  private String name;
  private String description;
  private Byte state;
  @Id private String id;
  private String type;
  private String configuration;
  private String example;
  private String fileName;
  private String url;
  private String jscript;
  private String needBs4Decode;
}
