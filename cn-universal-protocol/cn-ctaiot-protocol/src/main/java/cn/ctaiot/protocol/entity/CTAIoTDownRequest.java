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

package cn.ctaiot.protocol.entity;

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
public class CTAIoTDownRequest extends BaseDownRequest {

  private CtwingRequestData ctwingRequestData;
  private IoTProduct ioTProduct;
  private String creatorId;
  private String companyNo;
  private String classifiedId;
  private String classifiedName;

  @Data
  public static class CtwingRequestData {

    /**
     * imei运营商NB号码，长度15
     */
    private String imei;

    /**
     * 长度不超过15
     */
    private String imsi;

    /**
     * 水电表 表号
     */
    private String meterNo;

    /**
     * 设备型号
     */
    private String deviceModel;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 公司名称
     */
    private String companyNo;

    private Map<String, Object> configuration;

    /**
     * 派生元数据，有的设备属性，功能，事件 可能会增加
     */
    private Map<String, Object> deriveMetadata;

    /**
     * 经度
     */
    private String longitude;

    /**
     * 维度
     */
    private String latitude;

    /**
     * 安装位置
     */
    private String location;

    /**
     * 设备指令缓存时长
     */
    private int ttl;

    /**
     * 报文类型（16进制还是字符串）
     */
    private int dataType;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 产品分类
     */
    private String productType;

    /**
     * 二级分类
     */
    private String secondaryType;

    /**
     * 三级分类
     */
    private String thirdType;

    /**
     * 节点类型
     */
    private int nodeType;

    /**
     * 接入方式
     */
    private int accessType;

    /**
     * 网络类型
     */
    private int networkType;

    /**
     * 通讯协议
     */
    private int productProtocol;

    /**
     * 数据加密模式
     */
    private int dataEncryption;

    /**
     * 认证方式
     */
    private int authType;

    /**
     * Endpoint格式
     */
    private int endpointFormat;

    /**
     * 产品型号
     */
    private String tupDeviceModel;

    /**
     * 是否透传
     */
    private int tupIsThrough;

    /**
     * 消息格式
     */
    private int payloadFormat;

    /**
     * 省电模式
     */
    private int powerModel;

    /**
     * eDRX模式时间窗
     */
    private String lwm2mEdrxTime;

    /**
     * 电信产品key
     */
    private String productId;

    /**
     * 电信Master-APIkey
     */
    private String masterKey;

    /**
     * 电信设备类型、厂商ID、厂商名称
     */
    private String searchValue;

    /**
     * 当前页数
     */
    private int pageNow;

    /**
     * 每页记录数
     */
    private int pageSize;

    /**
     * 公共产品ID
     */
    private int publicProductId;

    /**
     * 分类ID
     */
    private String classifiedId;
  }

  public CtwingRequestData getCtwingRequestData() {
    if (getData() != null) {
      this.ctwingRequestData = JSONUtil.toBean(getData(), CtwingRequestData.class);
      return ctwingRequestData;
    }
    return new CtwingRequestData();
  }
}
