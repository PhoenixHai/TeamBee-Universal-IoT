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

package cn.onenet.protocol.nb.entity;

import cn.hutool.json.JSONObject;

/** Created by zhuocongbin date 2018/3/16 */
public abstract class CommonEntity {

  // 设备imei号，平台唯一，必填参数
  protected String imei;
  // ISPO标准中的Object ID
  protected Integer objId;
  // ISPO标准中的Object Instance ID
  protected Integer objInstId;
  // ISPO标准中的Resource ID
  protected Integer resId;
  protected String expiredTime;

  public JSONObject toJsonObject() {
    return null;
  }

  public abstract String toUrl();
}
