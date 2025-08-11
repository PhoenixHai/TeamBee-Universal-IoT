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

package cn.universal.persistence.common.inteceptor;

import cn.hutool.core.lang.Snowflake;
import tk.mybatis.mapper.genid.GenId;

/**
 * todo @Author zhaodexuan
 *
 * @description: 无
 * @since 2025/12/30 14:22
 */
public class SQenGenId implements GenId<Long> {

  public static final Snowflake snowflake = new Snowflake();

  @Override
  public Long genId(String s, String s1) {
    return snowflake.nextId();
  }
}
