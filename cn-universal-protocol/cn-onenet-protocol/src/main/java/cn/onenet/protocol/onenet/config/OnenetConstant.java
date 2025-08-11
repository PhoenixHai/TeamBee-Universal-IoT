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

package cn.onenet.protocol.onenet.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.stream.Stream;

/**
 * 移动平台常量
 *
 * @version 1.0 @Author Aleo
 * @since 2025/8/9 17:17
 */
public interface OnenetConstant {

  public static final String PLATFORM_CODE = "onenet";

  /** 离线 */
  public static int OFFLINE = 0;

  /** 在线 */
  public static int ONLINE = 1;

  /** 消息类型 */
  public static enum OnenetMessageType {
    /** 在线离线 */
    deviceOnlineOfflineReport,
    /** 数据上报 */
    dataReport;

    private OnenetMessageType() {}

    @JsonCreator
    public static OnenetMessageType find(String value) {
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
