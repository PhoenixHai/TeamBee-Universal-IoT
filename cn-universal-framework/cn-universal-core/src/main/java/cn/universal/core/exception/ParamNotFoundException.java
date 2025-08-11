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
 * 参数未能找到异常 @Author Aleo
 *
 * @since 2023/11/23 9:36
 */
public class ParamNotFoundException extends RuntimeException {

  public ParamNotFoundException(String message) {
    super(message);
  }
}
