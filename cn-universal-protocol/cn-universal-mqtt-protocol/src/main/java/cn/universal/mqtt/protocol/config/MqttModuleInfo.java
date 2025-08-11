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

package cn.universal.mqtt.protocol.config;

import cn.universal.core.protocol.ProtocolModuleInfo;
import org.springframework.stereotype.Component;

/**
 * MQTT 协议模块信息
 *
 * @version 2.0 @Author Aleo
 * @since 2025/1/2
 */
@Component
public class MqttModuleInfo implements ProtocolModuleInfo {

  @Override
  public String getCode() {
    return "mqtt";
  }

  @Override
  public String getName() {
    return "MQTT";
  }

  @Override
  public String getDescription() {
    return "消息队列遥测传输协议，轻量级的发布/订阅消息传输协议，是物联网通信的核心协议";
  }

  @Override
  public String getVersion() {
    return "2.0";
  }

  @Override
  public String getVendor() {
    return "Universal IoT";
  }

  @Override
  public boolean isCore() {
    return true; // MQTT是核心协议
  }

  @Override
  public ProtocolCategory getCategory() {
    return ProtocolCategory.MESSAGING;
  }
}