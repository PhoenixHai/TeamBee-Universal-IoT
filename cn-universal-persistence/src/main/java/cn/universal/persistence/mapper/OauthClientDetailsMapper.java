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

package cn.universal.persistence.mapper;

import cn.universal.persistence.common.BaseMapper;
import cn.universal.persistence.entity.OauthClientDetails;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OauthClientDetailsMapper extends BaseMapper<OauthClientDetails> {

  @Select("SELECT * FROM oauth_client_details WHERE client_id = #{clientId}")
  OauthClientDetails findByClientId(@Param("clientId") String clientId);

  int deleteByClientIds(String[] ids);
}
