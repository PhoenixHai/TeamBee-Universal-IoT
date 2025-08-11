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

package cn.onenet.protocol.nb.exception;

/**
 * Created by zhuocongbin date 2018/3/15
 */
public class OnenetNBException extends RuntimeException {

  private NBStatus status;
  private String message = null;

  public OnenetNBException(NBStatus status) {
    this.status = status;
  }

  public OnenetNBException(NBStatus status, String message) {
    this.status = status;
    this.message = message;
  }

  public int getErrorNo() {
    return status.getErrorNo();
  }

  public String getError() {
    if (message != null) {
      return status.getError() + ": " + message;
    } else {
      return status.getError();
    }
  }
}
