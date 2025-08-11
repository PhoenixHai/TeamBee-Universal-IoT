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

package cn.universal.web.controller.common;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.universal.admin.system.service.IIotUserService;
import cn.universal.admin.system.service.impl.UserApplicationService;
import cn.universal.core.iot.exception.APIErrorCode;
import cn.universal.core.iot.exception.BizException;
import cn.universal.dm.device.service.impl.IoTDeviceService;
import cn.universal.dm.device.service.impl.IoTProductDeviceService;
import cn.universal.persistence.entity.IoTUserApplication;
import cn.universal.persistence.query.IoTAPIQuery;
import cn.universal.web.context.TtlAuthContextHolder;
import jakarta.annotation.Resource;
import java.util.List;

/**
 * @version 1.0 @Author Aleo
 * @since 2025/11/15
 */
public class BaseApiController {

  @Resource protected IoTDeviceService iotDeviceService;

  @Resource private IoTProductDeviceService iotProductDeviceService;

  @Resource protected UserApplicationService userApplicationService;

  @Resource private IIotUserService iIotUserService;

  /***
   * 获取用户唯一标志
   * @return
   */
  protected String iotUnionId() {
    String principal = TtlAuthContextHolder.getInstance().getContext();
    IoTUserApplication iotUserApplication = userApplicationService.getIotUserByAppid(principal);
    // 如果为空，则使用的是密码
    if (iotUserApplication == null) {
      return principal;
    }
    return iotUserApplication.getUnionId();
  }

  /***
   * 是否客户端登录
   * @return
   */
  protected boolean isOauth2ClientCredentials() {
    String principal = TtlAuthContextHolder.getInstance().getContext();
    IoTUserApplication iotUserApplication = userApplicationService.getIotUserByAppid(principal);
    // 如果不为空，则使用的是客户端
    if (iotUserApplication != null) {
      return true;
    }
    return false;
  }

  /**
   * 客户端方式鉴权归属到某个应用
   *
   * <p>密码方式归属到正好
   *
   * @return
   */
  protected String iotApplicationId() {
    String principal = TtlAuthContextHolder.getInstance().getContext();
    IoTUserApplication iotUserApplication = userApplicationService.getIotUserByAppid(principal);
    if (iotUserApplication == null) {
      return null;
    }
    return iotUserApplication.getAppUniqueId();
  }

  /**
   * 判断是否自己的应用
   *
   * @param appid
   * @return
   */
  protected boolean checkAPPSelf(String appid) {
    if (StrUtil.isBlank(appid)) {
      throw new BizException("appid不能为空");
    }
    String unionId = iotUnionId();
    List<IoTUserApplication> userApps = userApplicationService.getUserAppByUnionId(unionId);
    if (CollectionUtil.isEmpty(userApps)) {
      throw new BizException("你当前没有应用");
    }
    for (IoTUserApplication userApplication : userApps) {
      if (appid.equals(userApplication.getAppId())) {
        return true;
      }
    }
    throw new BizException(
        "appid=[" + appid + "]应用没有操作权限！", APIErrorCode.APPLICATION_NOT_FOR_YOU.getCode());
  }

  /**
   * 判断是否自己的设备
   *
   * @param iotId 设备唯一编码
   * @return
   */
  protected boolean checkDevSelf(String iotId) {
    boolean flag =
        iotDeviceService.selectDevCount(
            IoTAPIQuery.builder()
                .iotId(iotId)
                .applicationId(iotApplicationId())
                .iotUnionId(iotUnionId())
                .build());
    if (!flag) {
      throw new BizException("此设备没有操作权限！", APIErrorCode.DEV_NOT_FOR_YOU.getCode());
    }
    return true;
  }

  /**
   * 校验产品或者设备是否属于自己
   *
   * @param iotAPIQuery
   */
  protected void checkDevOrProductSelf(IoTAPIQuery iotAPIQuery) {
    iotAPIQuery.setIotUnionId(iotUnionId());
    int selfProduct =
        iotProductDeviceService.selectProductCount(iotAPIQuery.getProductKey(), iotUnionId());
    boolean devCount = iotDeviceService.selectDevCount(iotAPIQuery);
    if (selfProduct <= 0 && !devCount) {
      throw new BizException("您没有权限操作此设备或产品！", APIErrorCode.DEV_NOT_FOR_YOU.getCode());
    }
  }

  protected boolean checkDevSelf(IoTAPIQuery iotAPIQuery) {
    iotAPIQuery.setIotUnionId(iotUnionId());
    boolean flag = iotDeviceService.selectDevCount(iotAPIQuery);
    if (!flag) {
      throw new BizException("此设备没有操作权限！", APIErrorCode.DEV_NOT_FOR_YOU.getCode());
    }
    return true;
  }

  protected void checkProductSelf(String productKey) {
    int flag = iotProductDeviceService.selectProductCount(productKey, iotUnionId());
    if (flag <= 0) {
      throw new BizException("您没有权限操作此产品！", APIErrorCode.PRODUCT_NOT_FOR_YOU.getCode());
    }
  }
}
