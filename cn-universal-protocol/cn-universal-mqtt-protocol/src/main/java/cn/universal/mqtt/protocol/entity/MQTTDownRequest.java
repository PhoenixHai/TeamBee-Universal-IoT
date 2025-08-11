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

package cn.universal.mqtt.protocol.entity;

import cn.hutool.json.JSONUtil;
import cn.universal.core.iot.message.DownCommonData;
import cn.universal.persistence.base.BaseDownRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * http下行参数
 *
 * @version 1.0 @Author Aleo
 * @since 2025/8/19 11:19
 */
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
public class MQTTDownRequest extends BaseDownRequest {

  private String downResult;
  private DownCommonData downCommonData;

  private String downTopic;

  public DownCommonData getDownCommonData() {
    if (getData() != null) {
      this.downCommonData = JSONUtil.toBean(getData(), DownCommonData.class);
      return downCommonData;
    }
    return new DownCommonData();
  }
}
