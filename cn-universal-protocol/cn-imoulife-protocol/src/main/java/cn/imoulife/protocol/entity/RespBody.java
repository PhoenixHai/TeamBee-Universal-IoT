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

import java.util.Objects;
import lombok.Data;

/**
 * @Author 🐤 zhongxins
 *
 * @email ✉ asimooc@foxmail.com
 * @since ⏰ 2018/11/21
 */
@Data
public class RespBody {

  private String id;
  private RespResult result;

  public boolean isOk() {
    return Objects.equals(RespCode.OK.code, this.getResult().getCode());
  }

  public String errCode() {
    return this.getResult().getCode();
  }

  public String errMsg() {
    return this.getResult().getMsg();
  }

  public Object getData() {
    return this.getResult().getData();
  }
}
