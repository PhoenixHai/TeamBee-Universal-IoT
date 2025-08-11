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

package cn.imoulife.protocol.entity;

import lombok.Data;

/**
 * @Author 🐤 zhongxin
 *
 * @email ✉ asimooc@foxmail.com
 * @since ⏰ 2018/11/21
 */
@Data
public class RespResult {

  private String code;
  private String msg;
  private Object data;
}
