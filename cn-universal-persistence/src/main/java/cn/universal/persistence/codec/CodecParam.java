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

package cn.universal.persistence.codec;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 请求参数实体 @Author Aleo
 *
 * @since 2023/11/23 9:10
 */
@Data
@AllArgsConstructor
public class CodecParam {

  /** 唯一编号 */
  private String codeKey;

  /** 编解码内容 */
  private Object codeBody;
}
