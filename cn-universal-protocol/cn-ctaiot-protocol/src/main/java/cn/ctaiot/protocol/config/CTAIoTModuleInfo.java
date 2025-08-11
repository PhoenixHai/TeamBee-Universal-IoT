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

package cn.ctaiot.protocol.config;

import cn.universal.core.protocol.ProtocolModuleInfo;
import org.springframework.stereotype.Component;

/**
 * CT-AIoT 协议模块信息
 *
 * @version 1.0 @Author Aleo
 * @since 2025/1/2
 */
@Component
public class CTAIoTModuleInfo implements ProtocolModuleInfo {

  @Override
  public String getCode() {
    return "ctaiot";
  }

  @Override
  public String getName() {
    return "CT-AIoT (天翼物联)";
  }

  @Override
  public String getDescription() {
    return "集成中国电信物联网开放平台，支持产品、设备管理、数据上报及命令下发";
  }

  @Override
  public String getVersion() {
    return "1.5";
  }

  @Override
  public String getVendor() {
    return "中国电信";
  }

  @Override
  public boolean isCore() {
    return false; // CT-AIoT是可选的第三方平台协议
  }

  @Override
  public ProtocolCategory getCategory() {
    return ProtocolCategory.PLATFORM;
  }
}
