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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.stream.Stream;

/**
 * Lora平台常量
 *
 * @version 1.0 @Author Aleo
 * @since 2025/8/9 17:17
 */
public interface CTAIoTConstant {

  /**
   * 离线
   */
  int OFFLINE = 0;

  /**
   * 在线
   */
  int ONLINE = 1;

  static enum CommandRespons {
    DELIVERED,
    SUCCESSFUL,
    SENT,
    FAILED
  }

  public enum Encode {
    /**
     * hex 编码解码
     *
     * @see cn.hutool.core.util.HexUtil encodeHexStr
     * @see cn.hutool.core.util.HexUtil decodeHex
     */
    HEX,
    /**
     *
     */
    NULL
  }

  /**
   * 消息类型
   */
  static enum CTAIoTMessageType {
    /**
     * 在线离线
     */
    deviceOnlineOfflineReport,
    /**
     * 数据上报
     */
    dataReport,
    dataReportTupUnion,
    /**
     * 事件上报
     */
    eventReport,
    /**
     * 设备命令响应
     */
    commandResponse;

    private CTAIoTMessageType() {
    }

    @JsonCreator
    public static CTAIoTMessageType find(String value) {
      return Stream.of(values())
          .filter(e -> e.getValue().equalsIgnoreCase(value))
          .findFirst()
          .orElse(null);
    }

    @JsonValue
    public String getValue() {
      return super.toString().toLowerCase();
    }
  }
}
