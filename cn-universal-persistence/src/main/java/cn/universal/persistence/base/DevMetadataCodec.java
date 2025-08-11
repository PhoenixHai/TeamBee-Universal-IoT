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

/**
 * @version 1.0 @Author Aleo
 * @since 2025/8/9 19:09
 */
public interface DevMetadataCodec<T> {

  default String getId() {
    return this.getClass().getSimpleName();
  }

  default String getName() {
    return this.getId();
  }

  T decode(String var1);

  String encode(T var1);
}
