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

package cn.universal.core.exception;

/** 文件信息异常类 @Author ruoyi */
public class FileException extends BaseException {

  private static final long serialVersionUID = 1L;

  public FileException(String code, Object[] args) {
    super("file", code, args, null);
  }
}
