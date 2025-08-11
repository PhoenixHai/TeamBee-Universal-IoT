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

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.github.cm.heclouds.onenet.studio.api.entity.application.project.AddDeviceRequest;
import com.github.cm.heclouds.onenet.studio.api.entity.application.project.AddDeviceResponse;
import com.github.cm.heclouds.onenet.studio.api.entity.application.project.ErrorData;
import java.util.ArrayList;
import java.util.List;

public class AddDeviceProjectRequest extends AddDeviceRequest {

  private List<String> devices = new ArrayList<>();

  /**
   * 设置项目ID
   *
   * @param projectId 项目ID
   */
  public void setProjectId(String projectId) {
    bodyParam("project_id", projectId);
  }

  /**
   * 设置产品ID
   *
   * @param productId 产品ID
   */
  public void setProductId(String productId) {
    bodyParam("product_id", productId);
  }

  /**
   * 设置添加的设备名称集合
   *
   * @param devices 添加的设备名称集合
   */
  public void setDevices(List<String> devices) {
    this.devices = devices;
    bodyParam("devices", this.devices);
  }

  /**
   * 添加设备名称
   *
   * @param device 设备名称
   */
  public void addDevice(String device) {
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
  protected Class<AddDeviceResponse> getResponseType() {
    return AddDeviceResponse.class;
  }

  @Override
  protected AddDeviceResponse newResponse(String responseBody) {
    AddDeviceResponse response = new AddDeviceResponse();
    if (StrUtil.isEmpty(responseBody)) {
      return response;
    }
    JSONObject jsonObject = JSONObject.parseObject(responseBody);
    if (!jsonObject.containsKey("error_data")) {
      return response;
    }
    List<ErrorData> errorData = jsonObject.getJSONArray("error_data").toJavaList(ErrorData.class);
    response.addAll(errorData);
    return response;
  }
}
