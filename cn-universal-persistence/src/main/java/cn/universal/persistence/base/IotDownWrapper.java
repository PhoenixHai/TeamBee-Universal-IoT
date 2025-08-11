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

package cn.universal.persistence.base;

import cn.universal.core.base.R;
import cn.universal.core.iot.message.DownRequest;
import cn.universal.persistence.entity.IoTDevice;
import cn.universal.persistence.entity.IoTProduct;

/**
 * @version 1.0 @Author Aleo
 * @since 2023/8/10
 */
public interface IotDownWrapper {

  /**
   * 用于全局调用的处理
   *
   * @param product
   * @param downRequest
   * @return
   */
  default R beforeDownAction(IoTProduct product, Object data, DownRequest downRequest) {
    return null;
  }


  /**
   * 用于全局调用的处理
   *
   * @param product
   * @param ioTDevice
   * @param downRequest
   * @return
   */
  default R beforeFunctionOrConfigDown(
      IoTProduct product, IoTDevice ioTDevice, DownRequest downRequest) {
    return null;
  }
}
