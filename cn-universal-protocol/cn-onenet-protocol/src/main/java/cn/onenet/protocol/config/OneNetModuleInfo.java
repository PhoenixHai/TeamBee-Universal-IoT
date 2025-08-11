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

package cn.onenet.protocol.config;

import cn.universal.core.protocol.ProtocolModuleInfo;
import org.springframework.stereotype.Component;

/**
 * OneNet 协议模块信息
 *
 * @version 1.0 @Author Aleo
 * @since 2025/1/2
 */
@Component
public class OneNetModuleInfo implements ProtocolModuleInfo {

  @Override
  public String getCode() {
    return "onenet";
  }

  @Override
  public String getName() {
    return "OneNet (移动)";
  }

  @Override
  public String getDescription() {
    return "中国移动物联网开放平台，提供设备接入、数据存储、应用开发等一站式物联网服务";
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  public String getVendor() {
    return "中国移动";
  }

  @Override
  public boolean isCore() {
    return false; // OneNet是可选的第三方协议
  }

  @Override
  public ProtocolCategory getCategory() {
    return ProtocolCategory.PLATFORM;
  }
}
