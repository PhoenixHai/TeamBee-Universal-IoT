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

package cn.imoulife.protocol.config;

import cn.universal.core.protocol.ProtocolModuleInfo;
import org.springframework.stereotype.Component;

/**
 * ImouLife 协议模块信息
 *
 * @version 1.0 @Author Aleo
 * @since 2025/1/2
 */
@Component
public class ImouLifeModuleInfo implements ProtocolModuleInfo {

  @Override
  public String getCode() {
    return "imoulife";
  }

  @Override
  public String getName() {
    return "ImouLife (乐橙)";
  }

  @Override
  public String getDescription() {
    return "大华乐橙云平台，专业的视频监控云服务平台，提供设备管理、视频存储、智能分析等服务";
  }

  @Override
  public String getVersion() {
    return "1.0";
  }

  @Override
  public String getVendor() {
    return "大华股份";
  }

  @Override
  public boolean isCore() {
    return false; // ImouLife是可选的第三方协议
  }

  @Override
  public ProtocolCategory getCategory() {
    return ProtocolCategory.PLATFORM;
  }
}
