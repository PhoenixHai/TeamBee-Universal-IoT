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

import cn.hutool.json.JSONUtil;
import cn.universal.persistence.base.BaseDownRequest;
import cn.universal.persistence.entity.IoTProduct;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 电信下行参数
 *
 * @version 1.0 @Author Aleo
 * @since 2025/8/19 11:19
 */
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
public class ImoulifeDownRequest extends BaseDownRequest {

  private ImoulifeRequestData imoulifeRequestData;
  private IoTProduct ioTProduct;

  @Data
  public class ImoulifeRequestData {

    /** imei运营商NB号码，长度15 */
    private String imei;

    /** 验证码 */
    private String appKey;

    /** 长度不超过15 */
    private String imsi;

    /** 水电表 表号 */
    private String meterNo;

    /** 设备型号 */
    private String deviceModel;

    /** 设备名称 */
    private String deviceName;

    /** 公司名称 */
    private String companyNo;

    private Map<String, Object> configuration;

    /** 派生元数据，有的设备属性，功能，事件 可能会增加 */
    private Map<String, Object> deriveMetadata;

    /** 经度 */
    private String longitude;

    /** 维度 */
    private String latitude;

    /** 安装位置 */
    private String location;

    /** 设备指令缓存时长 */
    private int ttl;

    /** 报文类型（16进制还是字符串） */
    private int dataType;
  }

  public ImoulifeRequestData getImoulifeRequestData() {
    if (getData() != null) {
      this.imoulifeRequestData = JSONUtil.toBean(getData(), ImoulifeRequestData.class);
      return imoulifeRequestData;
    }
    return new ImoulifeRequestData();
  }
}
