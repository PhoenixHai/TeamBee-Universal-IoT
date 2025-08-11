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
public enum NBStatus {
  HTTP_REQUEST_ERROR(1, "http request error"),
  LOAD_CONFIG_ERROR(2, "load config error");
  private String error;
  private int errorNo;

  NBStatus(int errorNo, String error) {
    this.error = error;
    this.errorNo = errorNo;
  }

  public String getError() {
    return error;
  }

  public int getErrorNo() {
    return errorNo;
  }
}
