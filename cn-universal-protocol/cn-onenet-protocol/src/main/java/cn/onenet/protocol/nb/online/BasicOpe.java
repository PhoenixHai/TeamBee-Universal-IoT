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
import okhttp3.Callback;

/**
 * Created by zhuocongbin date 2018/3/16
 */
public abstract class BasicOpe {

  protected String apiKey;

  public BasicOpe(String apiKey) {
    this.apiKey = apiKey;
  }

  public abstract JSONObject operation(CommonEntity commonEntity, JSONObject body);

  public abstract void operation(CommonEntity commonEntity, JSONObject body, Callback callback);
}
