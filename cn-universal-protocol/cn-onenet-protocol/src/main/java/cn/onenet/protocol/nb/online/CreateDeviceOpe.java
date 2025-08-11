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

package cn.onenet.protocol.nb.online;

import cn.hutool.json.JSONObject;
import cn.onenet.protocol.nb.entity.CommonEntity;
import cn.onenet.protocol.nb.utils.HttpSendCenter;
import okhttp3.Callback;

/**
 * Created by zhuocongbin date 2018/3/16 apiKey: the product of api-key which can be found on
 * OneNET
 */
public class CreateDeviceOpe extends BasicOpe {

  public CreateDeviceOpe(String apiKey) {
    super(apiKey);
  }

  @Override
  public JSONObject operation(CommonEntity commonEntity, JSONObject body) {
    return HttpSendCenter.post(this.apiKey, commonEntity.toUrl(), body);
  }

  @Override
  public void operation(CommonEntity commonEntity, JSONObject body, Callback callback) {
    HttpSendCenter.postAsync(this.apiKey, commonEntity.toUrl(), body, callback);
  }
}
