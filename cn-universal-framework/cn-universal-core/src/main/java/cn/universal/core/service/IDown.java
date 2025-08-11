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

package cn.universal.core.service;

import cn.hutool.json.JSONObject;
import cn.universal.core.base.R;

/**
 * （下行）平台到设备
 */
public interface IDown {

  /**
   * 返回服务名称
   *
   * <p>通过服务名动态选择实现类
   *
   * @return
   */
  String name();

  /**
   * 返回服务code
   *
   * <p>通过服务名动态选择实现类
   *
   * @return
   */
  String code();

  /**
   * 下行处理
   *
   * @param msg
   * @return
   */
  R down(String msg);

  /**
   * 产品级下行处理
   *
   * @param msg
   * @return
   */
  default R downPro(String msg) {
    return null;
  }

  default R down(JSONObject msg) {
    return null;
  }

  /**
   * 保存设备云端指令
   *
   * @param productKey 产品key
   * @param deviceId   设备序列号
   */
  default void storeCommand(String productKey, String deviceId, Object data) {
  }
}
