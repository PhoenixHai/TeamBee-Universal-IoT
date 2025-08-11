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

package cn.tcp.protocol.config;

import cn.universal.core.protocol.ProtocolModuleInfo;
import org.springframework.stereotype.Component;

/**
 * TCP 协议模块信息
 *
 * @version 2.0 @Author Aleo
 * @since 2025/1/2
 */
@Component
public class TcpModuleInfo implements ProtocolModuleInfo {

  @Override
  public String getCode() {
    return "tcp";
  }

  @Override
  public String getName() {
    return "TCP直连";
  }

  @Override
  public String getDescription() {
    return "提供面向连接的可靠字节流传输，支持二进制/16进制/JSON报文解析等";
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
    return false; // TCP是可选协议
  }

  @Override
  public ProtocolCategory getCategory() {
    return ProtocolCategory.TRANSPORT;
  }
}