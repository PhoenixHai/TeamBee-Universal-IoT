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

/**
 * 编解码异常 @Author Aleo
 *
 * @since 2023/11/23 14:01
 */
public class CodecException extends RuntimeException {

  public CodecException(String message) {
    super(message);
  }

  public CodecException(Throwable throwable) {
    super(throwable);
  }
}
