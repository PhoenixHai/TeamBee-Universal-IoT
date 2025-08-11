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
public class Read extends CommonEntity {

  public void setObjInstId(Integer objInstId) {
    this.objInstId = objInstId;
  }

  public void setResId(Integer resId) {
    this.resId = resId;
  }

  /**
   * @param imei  设备IMEI
   * @param objId 读对象ID 其他可选参数Object Instance ID,Rescource ID可以通过相关set函数设置
   */
  public Read(String imei, Integer objId) {
    this.imei = imei;
    this.objId = objId;
  }

  @Override
  public String toUrl() {
    StringBuilder url = new StringBuilder(Config.getDomainName());
    url.append("/nbiot?imei=").append(this.imei);
    url.append("&obj_id=").append(this.objId);
    if (this.objInstId != null) {
      url.append("&obj_inst_id=").append(this.objInstId);
    }
    if (this.resId != null) {
      url.append("&res_id=").append(this.resId);
    }
    return url.toString();
  }
}
