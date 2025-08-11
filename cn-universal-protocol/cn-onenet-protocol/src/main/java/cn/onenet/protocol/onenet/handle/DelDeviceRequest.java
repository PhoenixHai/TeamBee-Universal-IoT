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

package cn.onenet.protocol.onenet.handle;

import com.github.cm.heclouds.onenet.studio.api.entity.common.DeleteDeviceRequest;

public class DelDeviceRequest extends DeleteDeviceRequest {

  /**
   * 设置设备名称参数
   *
   * @param deviceName 设备名称
   */
  public void setDeviceName(String deviceName) {
    bodyParam("device_name", deviceName);
  }

  /**
   * 设置鉴权
   *
   * @param Authorization 安全鉴权
   */
  public void setAuthorization(String Authorization) {
    header("Authorization", Authorization);
  }
}
