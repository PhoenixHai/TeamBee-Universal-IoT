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

package cn.universal.persistence.common;

import java.io.Serializable;
import tk.mybatis.mapper.common.ConditionMapper;
import tk.mybatis.mapper.common.IdsMapper;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

/**
 * 继承自己的 BaseMapper
 *
 * @version 1.0 @Author Aleo
 * @since 2025/8/11 16:59
 */
public interface BaseMapper<T extends Serializable>
    extends Mapper<T>, MySqlMapper<T>, ConditionMapper<T>, IdsMapper<T> {}
