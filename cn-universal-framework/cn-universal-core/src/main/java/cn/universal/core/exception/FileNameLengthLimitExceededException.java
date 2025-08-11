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

/** 文件名称超长限制异常类 @Author ruoyi */
public class FileNameLengthLimitExceededException extends FileException {

  private static final long serialVersionUID = 1L;

  public FileNameLengthLimitExceededException(int defaultFileNameLength) {
    super("upload.filename.exceed.length", new Object[] {defaultFileNameLength});
  }
}
