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

package cn.onenet.protocol.nb.entity;

import cn.onenet.protocol.nb.config.Config;

/**
 * Created by zhuocongbin date 2018/3/15
 */
public class Execute extends CommonEntity {

  /**
   * @param imei                   设备IMEI号，必填
   * @param objId，下发命令的对象ID,必填
   * @param objInstId，下发命令的实例ID,必填
   * @param resId，下发命令的资源ID,必填
   */
  public Execute(String imei, Integer objId, Integer objInstId, Integer resId, String expiredTime) {
    this.imei = imei;
    this.objId = objId;
    this.objInstId = objInstId;
    this.resId = resId;
    this.expiredTime = expiredTime;
  }

  @Override
  public String toUrl() {
    StringBuilder url = new StringBuilder(Config.getDomainName());
    url.append("/nbiot/execute/offline?imei=").append(this.imei);
    url.append("&expired_time=").append(this.expiredTime);
    url.append("&obj_id=").append(this.objId);
    url.append("&obj_inst_id=").append(this.objInstId);
    url.append("&res_id=").append(this.resId);
    return url.toString();
  }
}
