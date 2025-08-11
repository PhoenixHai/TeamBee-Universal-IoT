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

package cn.onenet.protocol.nb.samples;

import cn.hutool.json.JSONObject;
import cn.onenet.protocol.nb.entity.Device;
import cn.onenet.protocol.nb.entity.Observe;
import cn.onenet.protocol.nb.entity.Read;
import cn.onenet.protocol.nb.entity.Resources;
import cn.onenet.protocol.nb.online.CreateDeviceOpe;
import cn.onenet.protocol.nb.online.ExecuteOpe;
import cn.onenet.protocol.nb.online.ObserveOpe;
import cn.onenet.protocol.nb.online.ReadOpe;
import cn.onenet.protocol.nb.online.ResourcesOpe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zhuocongbin date 2018/3/15
 */
public class ApiSample {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApiSample.class);

  public void main(String[] args) {
    String apiKey = "EM7X=z8G0sSpiRRlnkmwFTnrXMU=";
    String imei = "*******";
    Integer objId = 3200;
    Integer objInstId = 0;
    Integer readResId = 5500;
    Integer executeResId = 5501;
    Integer writeResId = 5750;
    Integer writeMode = 2;
    // Create device
    CreateDeviceOpe deviceOpe = new CreateDeviceOpe(apiKey);
    Device device = new Device("测试", "366322456556501", "366322456556501");
    JSONObject jsonObject =
        deviceOpe.operation(device, device.toJsonObject()).getJSONObject("data");
    String a = jsonObject.getStr("device_id");
    LOGGER.info(deviceOpe.operation(device, device.toJsonObject()).toString());
    // Read
    ReadOpe readOperation = new ReadOpe(apiKey);
    Read read = new Read(imei, objId);
    read.setObjInstId(objInstId);
    read.setResId(readResId);
    LOGGER.info(readOperation.operation(read, null).toString());

    // Execute
    ExecuteOpe executeOpe = new ExecuteOpe(apiKey);
    // Execute execute = new Execute(imei, objId, objInstId, executeResId);
    // 下发命令内容，JSON格式
    JSONObject body = new JSONObject();
    body.set("args", "ping");
    // LOGGER.info(executeOpe.operation(execute, body).toString());
    // Resource
    ResourcesOpe resourcesOpe = new ResourcesOpe(apiKey);
    Resources resources = new Resources(imei);
    LOGGER.info(resourcesOpe.operation(resources, null).toString());
    // Observe
    ObserveOpe observeOpe = new ObserveOpe(apiKey);
    Observe observe = new Observe(imei, objId, false);
    LOGGER.info(observeOpe.operation(observe, null).toString());
  }
}
