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

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.github.cm.heclouds.onenet.studio.api.entity.common.BatchCreateDevicesRequest;
import com.github.cm.heclouds.onenet.studio.api.entity.common.BatchCreateDevicesResponse;
import com.github.cm.heclouds.onenet.studio.api.entity.common.Device;
import com.github.cm.heclouds.onenet.studio.api.entity.common.DeviceDetail;
import java.util.ArrayList;
import java.util.List;

public class AddBatchDeviceRequest extends BatchCreateDevicesRequest {

  private List<Device> devices = new ArrayList<>();

  /**
   * 设置批量创建的设备信息集合
   *
   * @param devices 批量创建的设备信息集合
   */
  public void setDevices(List<Device> devices) {
    this.devices = devices;
    bodyParam("devices", this.devices);
  }

  /**
   * 添加要批量创建的设备
   *
   * @param device 设备信息
   */
  public void addDevice(Device device) {
    devices.add(device);
    bodyParam("devices", devices);
  }

  /**
   * 设置鉴权
   *
   * @param Authorization 安全鉴权
   */
  public void setAuthorization(String Authorization) {
    header("Authorization", Authorization);
  }

  @Override
  protected Class<BatchCreateDevicesResponse> getResponseType() {
    return BatchCreateDevicesResponse.class;
  }

  @Override
  protected BatchCreateDevicesResponse newResponse(String responseBody) {
    BatchCreateDevicesResponse response = new BatchCreateDevicesResponse();
    JSONObject jsonObject = JSON.parseObject(responseBody);
    List<DeviceDetail> deviceDetails =
        JSON.parseArray(jsonObject.getString("list"), DeviceDetail.class);
    response.addAll(deviceDetails);
    return response;
  }
}
