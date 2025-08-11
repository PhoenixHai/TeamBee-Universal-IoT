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

package cn.universal.ossm.oss.factory;

import cn.hutool.core.lang.Assert;
import cn.universal.core.base.SpringUtils;
import cn.universal.ossm.oss.enumd.CloudServiceEnumd;
import cn.universal.ossm.oss.service.ICloudStorageService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** 文件上传Factory @Author Lion Li */
public class OssFactory {

  //  private static ISysConfigService sysConfigService;
  //
  //  static {
  //    OssFactory.sysConfigService = SpringUtils.getBean(ISysConfigService.class);
  //  }

  private static final Map<String, ICloudStorageService> SERVICES = new ConcurrentHashMap<>();

  public static ICloudStorageService instance() {
    //    String type = sysConfigService.selectConfigByKey(CloudConstant.CLOUD_STORAGE_CONFIG_KEY);
    return instance("aliyun");
  }

  public static ICloudStorageService instance(String type) {
    ICloudStorageService service = SERVICES.get(type);
    if (service == null) {
      service = (ICloudStorageService) SpringUtils.getBean(CloudServiceEnumd.getServiceClass(type));
    }
    return service;
  }

  public static void register(String type, ICloudStorageService iCloudStorageService) {
    Assert.notNull(type, "type can't be null");
    SERVICES.put(type, iCloudStorageService);
  }
}
