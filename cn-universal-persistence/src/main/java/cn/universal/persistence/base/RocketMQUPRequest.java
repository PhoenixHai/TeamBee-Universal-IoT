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

package cn.universal.persistence.base;

import cn.universal.persistence.dto.IoTDeviceDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @version 1.0 @Author Aleo
 * @since 2023/1/12
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RocketMQUPRequest extends BaseUPRequest {

  private IoTDeviceDTO devBO;

  /**
   * 指令
   */
  private String commandId;

  /**
   * 指令
   */
  private Integer commandStatus;
}
