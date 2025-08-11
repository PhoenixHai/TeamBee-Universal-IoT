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

import com.github.cm.heclouds.onenet.studio.api.entity.common.CreateDeviceRequest;

public class AddDeviceRequest extends CreateDeviceRequest {

  /**
   * 设置设备名称参数
   *
   * @param deviceName 设备名称
   */
  public void setDeviceName(String deviceName) {
    bodyParam("device_name", deviceName);
  }

  /**
   * 设置设备描述参数
   *
   * @param desc 设备描述
   */
  public void setDesc(String desc) {
    bodyParam("desc", desc);
  }

  /**
   * 设置设备imei
   *
   * @param imei imei
   */
  public void setImei(String imei) {
    bodyParam("imei", imei);
  }

  /**
   * 设置设备imsi
   *
   * @param imsi imsi
   */
  public void setImsi(String imsi) {
    bodyParam("imsi", imsi);
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
