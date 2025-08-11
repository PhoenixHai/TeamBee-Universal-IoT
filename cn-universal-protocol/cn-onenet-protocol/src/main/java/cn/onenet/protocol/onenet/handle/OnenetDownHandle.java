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

package cn.onenet.protocol.onenet.handle;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.universal.core.base.R;
import cn.universal.core.config.PlatformProperties;
import cn.universal.core.iot.constant.IotConstant.DeviceStatus;
import cn.universal.core.iot.constant.IotConstant.ERROR_CODE;
import cn.universal.core.iot.message.DownRequest;
import cn.universal.dm.device.service.impl.IoTDeviceService;
import cn.universal.persistence.base.IoTDeviceLifeCycle;
import cn.universal.persistence.base.IotDownAdapter;
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.entity.IoTDevice;
import cn.universal.persistence.entity.SupportMapAreas;
import cn.universal.persistence.mapper.IoTDeviceMapper;
import cn.universal.persistence.mapper.IoTProductMapper;
import cn.universal.persistence.mapper.SupportMapAreasMapper;
import cn.onenet.protocol.onenet.entity.OnenetDownRequest;
import com.alibaba.fastjson2.JSON;
import com.github.cm.heclouds.onenet.studio.api.IotClient;
import com.github.cm.heclouds.onenet.studio.api.IotProfile;
import com.github.cm.heclouds.onenet.studio.api.entity.application.project.AddDeviceResponse;
import com.github.cm.heclouds.onenet.studio.api.entity.common.BatchCreateDevicesResponse;
import com.github.cm.heclouds.onenet.studio.api.entity.common.CreateDeviceResponse;
import com.github.cm.heclouds.onenet.studio.api.entity.common.DeleteDeviceResponse;
import com.github.cm.heclouds.onenet.studio.api.entity.common.Device;
import com.github.cm.heclouds.onenet.studio.api.entity.common.UpdateDeviceResponse;
import com.github.cm.heclouds.onenet.studio.api.exception.IotClientException;
import com.github.cm.heclouds.onenet.studio.api.exception.IotServerException;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 电信下行实际处理类
 *
 * @version 1.0 @Author Aleo
 * @since 2025/8/20 08:41
 */
@Slf4j
@Component
public class OnenetDownHandle extends IotDownAdapter<OnenetDownRequest> {

  @Resource private PlatformProperties ctwingProperties;
  @Resource private IoTProductMapper ioTProductMapper;
  @Resource private IoTDeviceMapper ioTDeviceMapper;
  @Resource private SupportMapAreasMapper supportMapAreasMapper;
  @Resource private Token token;
  @Resource private IoTDeviceService iotDeviceService;

  @Resource(name = "ioTDeviceActionAfterService")
  private IoTDeviceLifeCycle ioTDeviceLifeCycle;

  @Value("${ctaiot.protocol.userId}")
  private String userId;

  @Value("${ctaiot.protocol.accessKey}")
  private String accessKey;

  @Value("${ctaiot.protocol.projectId}")
  private String projectId;

  public R down(OnenetDownRequest downRequest) {
    if (downRequest == null || downRequest.getCmd() == null) {
      log.warn("移动物联下行对象为空,不处理={}", downRequest);
      return R.error("移动物联下行对象为空");
    }

    R preR = preDown(downRequest.getIoTProduct(), downRequest.getOnenetRequestData(), downRequest);
    // R.SUCCESS.equals(preR.getCode())
    if (Objects.nonNull(preR)) {
      return preR;
    }

    R r = null;
    switch (downRequest.getCmd()) {
      case DEV_ADD:
        r = devAdd(downRequest);
        break;
      case DEV_ADDS:
        r = devAdds(downRequest);
        break;
      case DEV_DEL:
        r = devDel(downRequest);
        break;
      case DEV_UPDATE:
        r = devUpdate(downRequest);
        break;
      case DEV_FUNCTION:
        r = devConfig(downRequest);
        break;
      default:
        log.info("移动下行未匹配到方法");
    }
    return r;
  }

  /** 详见文档 https://apiportalweb.ctwing.cn/index.html#/apiDetail/10255/218/1001 */
  private R devConfig(OnenetDownRequest downRequest) {
    IoTDevice ioTDevice =
        IoTDevice.builder()
            .productKey(downRequest.getProductKey())
            .deviceId(downRequest.getDeviceId())
            .build();
    IoTDevice instance = ioTDeviceMapper.selectOne(ioTDevice);
    if (instance == null) {
      // 设备不存在
      return R.error(
          ERROR_CODE.DEV_DEL_DEVICE_NO_ID_EXIST.getCode(),
          ERROR_CODE.DEV_DEL_DEVICE_NO_ID_EXIST.getName());
    }

    R r = callGlobalFunction(downRequest.getIoTProduct(), instance, downRequest);
    if (Objects.nonNull(r)) {
      return r;
    }

    return R.ok();
  }

  /** 设备添加 */
  private R devAdd(OnenetDownRequest downRequest) {
    IoTDevice ioTDevice =
        IoTDevice.builder()
            .productKey(downRequest.getProductKey())
            .deviceId(downRequest.getDeviceId())
            .build();
    int size = ioTDeviceMapper.selectCount(ioTDevice);
    if (size > 0) {
      // 设备已经存在
      return R.error(
          ERROR_CODE.DEV_ADD_DEVICE_ID_EXIST.getCode(),
          ERROR_CODE.DEV_ADD_DEVICE_ID_EXIST.getName());
    }
    IotProfile profile = new IotProfile();
    profile.userId(userId).accessKey(accessKey);
    IotClient client = IotClient.create(profile);
    // 自定义添加设备类
    AddDeviceRequest deviceRequest = new AddDeviceRequest();
    // 自定义添加设备到项目
    AddDeviceProjectRequest addDeviceProjectRequest = new AddDeviceProjectRequest();
    deviceRequest.setProductId(
        downRequest.getOnenetRequestData().getConfiguration().get("productId").toString());
    deviceRequest.setDeviceName(downRequest.getOnenetRequestData().getImei());
    deviceRequest.setImei(downRequest.getOnenetRequestData().getImei());
    deviceRequest.setImsi(downRequest.getOnenetRequestData().getImsi());
    addDeviceProjectRequest.setProductId(
        downRequest.getOnenetRequestData().getConfiguration().get("productId").toString());
    addDeviceProjectRequest.setProjectId(projectId);
    List<String> stringList = new ArrayList<>();
    stringList.add(downRequest.getOnenetRequestData().getImei());
    addDeviceProjectRequest.setDevices(stringList);
    try {
      // 安全鉴权计算
      String Authorization = token.getAuthorization();
      deviceRequest.setAuthorization(Authorization);
      addDeviceProjectRequest.setAuthorization(Authorization);
    } catch (Exception e) {
      e.printStackTrace();
    }
    // 发送移动
    try {
      CreateDeviceResponse response = client.sendRequest(deviceRequest);
      System.out.println(JSON.toJSONString(response));
      Map<String, Object> saveResult = saveDevInstance(downRequest);
      // 添加设备后，再将设备添加到项目中
      try {
        AddDeviceResponse response2 = client.sendRequest(addDeviceProjectRequest);
        System.out.println(JSON.toJSONString(response2));
        return R.ok(saveResult);
      } catch (IotClientException e) {
        e.printStackTrace();
      } catch (IotServerException e) {
        System.err.println(e.getCode());
        e.printStackTrace();
        return R.error(e.getCode());
      }
    } catch (IotClientException e) {
      e.printStackTrace();
    } catch (IotServerException e) {
      System.err.println(e.getCode());
      e.printStackTrace();
      return R.error(e.getCode());
    }
    return R.error(ERROR_CODE.DEV_ADD_ERROR.getCode(), ERROR_CODE.DEV_ADD_ERROR.getName());
  }

  // 本地添加设备
  private Map<String, Object> saveDevInstance(OnenetDownRequest downRequest) {
    String iotId = IdUtil.simpleUUID();
    IoTDevice ioTDevice =
        IoTDevice.builder()
            .deviceId(downRequest.getDeviceId())
            .createTime(System.currentTimeMillis() / 1000)
            .deviceName(downRequest.getOnenetRequestData().getDeviceName())
            .iotId(iotId)
            .state(DeviceStatus.offline.getCode())
            .creatorId(downRequest.getAppUnionId())
            .gwProductKey(downRequest.getGwProductKey())
            .application(downRequest.getApplicationId())
            .productName(downRequest.getIoTProduct().getName())
            .productKey(downRequest.getProductKey())
            .build();

    if (StrUtil.isNotBlank(downRequest.getOnenetRequestData().getLatitude())
        && StrUtil.isNotBlank(downRequest.getOnenetRequestData().getLongitude())) {

      ioTDevice.setCoordinate(
          StrUtil.join(
              ",",
              downRequest.getOnenetRequestData().getLongitude(),
              downRequest.getOnenetRequestData().getLatitude()));

      SupportMapAreas supportMapAreas =
          supportMapAreasMapper.selectMapAreas(
              downRequest.getOnenetRequestData().getLongitude(),
              downRequest.getOnenetRequestData().getLatitude());
      if (supportMapAreas == null) {
        log.info(
            "查询区域id为空,lot={},lat={}",
            downRequest.getOnenetRequestData().getLongitude(),
            downRequest.getOnenetRequestData().getLatitude());
      } else {
        ioTDevice.setAreasId(supportMapAreas.getId());
      }
    }
    Map<String, Object> config = new HashMap<>();
    config.put("imei", downRequest.getOnenetRequestData().getImei());
    config.put("meterNo", downRequest.getOnenetRequestData().getMeterNo());
    finalDown(
        config,
        downRequest.getIoTProduct(),
        downRequest.getCmd(),
        downRequest.getOnenetRequestData());
    ioTDevice.setConfiguration(JSONUtil.toJsonStr(config));
    ioTDeviceMapper.insertUseGeneratedKeys(ioTDevice);

    // 推送设备创建消息
    ioTDeviceLifeCycle.create(downRequest.getProductKey(), downRequest.getDeviceId(), downRequest);

    // 组件返回字段
    Map<String, Object> result = new HashMap<>();
    result.put("iotId", iotId);
    result.put("areasId", ioTDevice.getAreasId() == null ? "" : ioTDevice.getAreasId());
    if (StrUtil.isNotBlank(downRequest.getIoTProduct().getMetadata())) {
      result.put("metadata", JSONUtil.parseObj(downRequest.getIoTProduct().getMetadata()));
    }
    result.put("productKey", downRequest.getProductKey());
    result.put("deviceNode", downRequest.getIoTProduct().getDeviceNode());
    return result;
  }

  @Override
  public void Rule() {}

  /** 删除电信平台设备 */
  private R devDel(OnenetDownRequest downRequest) {
    IoTDevice ioTDevice =
        IoTDevice.builder()
            .productKey(downRequest.getProductKey())
            .deviceId(downRequest.getDeviceId())
            .build();
    IotProfile profile = new IotProfile();
    profile.userId(userId).accessKey(accessKey);
    IotClient client = IotClient.create(profile);

    DelDeviceRequest delDeviceRequest = new DelDeviceRequest();
    // 批量
    List<DelDeviceRequest> delDeviceRequestList = new ArrayList<>();
    String[] split = ioTDevice.getDeviceId().split(",");
    List<IoTDevice> ioTDeviceList = new ArrayList<>();
    for (int i = 0; i < split.length; i++) {
      ioTDevice.setDeviceId(split[i]);
      IoTDevice ioTDeviceOne = ioTDeviceMapper.selectOne(ioTDevice);
      ioTDeviceList.add(ioTDeviceOne);
      // 自定义删除类
      delDeviceRequest.setProductId(
          downRequest.getOnenetRequestData().getConfiguration().get("productId").toString());
      delDeviceRequest.setDeviceName(split[i]);
      try {
        // 安全鉴权计算
        String Authorization = token.getAuthorization();
        delDeviceRequest.setAuthorization(Authorization);
      } catch (Exception e) {
        e.printStackTrace();
      }
      delDeviceRequestList.add(delDeviceRequest);
      int size = ioTDeviceMapper.selectCount(ioTDevice);
      if (size == 0) {
        // 设备不存在
        return R.error(
            ERROR_CODE.DEV_DEL_DEVICE_NO_ID_EXIST.getCode(),
            ERROR_CODE.DEV_DEL_DEVICE_NO_ID_EXIST.getName());
      }
      // 发送移动
      try {
        DeleteDeviceResponse response = client.sendRequest(delDeviceRequest);
        System.out.println(JSON.toJSONString(response));
        deleteDevInstance(ioTDeviceList, downRequest);
        return R.ok("删除成功");
      } catch (IotClientException e) {
        e.printStackTrace();
      } catch (IotServerException e) {
        System.err.println(e.getCode());
        e.printStackTrace();
        return R.error(e.getCode());
      }
    }
    return R.error(ERROR_CODE.DEV_DEL_ERROR.getCode(), ERROR_CODE.DEV_DEL_ERROR.getName());
  }

  /** 删除本地数据库设备 */
  private void deleteDevInstance(List<IoTDevice> ioTDeviceList, DownRequest downRequest) {
    for (IoTDevice dev : ioTDeviceList) {
      Map<String, Object> objectMap = new HashMap<>();
      objectMap.put("iotId", dev.getIotId());
      IoTDeviceDTO ioTDeviceDTO = iotDeviceService.selectDevInstanceBO(objectMap);
      if (ioTDeviceDTO != null) {
        // 设备生命周期-删除
        ioTDeviceLifeCycle.delete(ioTDeviceDTO, downRequest);
        iotDeviceService.delDevInstance(dev.getIotId());
      } else {
        log.info("设备生命周期-删除失败 【设备不存在】 iotId = {}", dev.getIotId());
      }
    }
  }

  /** 修改电信平台设备 */
  private R devUpdate(OnenetDownRequest downRequest) {
    IoTDevice ioTDevice =
        IoTDevice.builder()
            .productKey(downRequest.getProductKey())
            .deviceId(downRequest.getDeviceId())
            .build();
    int size = ioTDeviceMapper.selectCount(ioTDevice);
    if (size == 0) {
      // 设备已经存在
      return R.error(
          ERROR_CODE.DEV_UPDATE_DEVICE_NO_ID_EXIST.getCode(),
          ERROR_CODE.DEV_UPDATE_DEVICE_NO_ID_EXIST.getName());
    }
    IoTDevice ioTDeviceOne = ioTDeviceMapper.selectOne(ioTDevice);
    ioTDeviceOne.setDeviceName(downRequest.getOnenetRequestData().getDeviceName());
    IotProfile profile = new IotProfile();
    profile.userId(userId).accessKey(accessKey);
    IotClient client = IotClient.create(profile);
    // 自定义添加设备类
    UpdateDeviceRequest updateDeviceRequest = new UpdateDeviceRequest();
    updateDeviceRequest.setProductId(
        downRequest.getOnenetRequestData().getConfiguration().get("productId").toString());
    updateDeviceRequest.setDeviceName(downRequest.getDeviceId());
    updateDeviceRequest.setImsi(downRequest.getOnenetRequestData().getImsi());
    try {
      // 安全鉴权计算
      String Authorization = token.getAuthorization();
      updateDeviceRequest.setAuthorization(Authorization);
    } catch (Exception e) {
      e.printStackTrace();
    }
    // 发送移动
    try {
      UpdateDeviceResponse response = client.sendRequest(updateDeviceRequest);
      System.out.println(JSON.toJSONString(response));
      Map<String, Object> updateResult = updateDevInstance(ioTDeviceOne, downRequest);
      return R.ok(updateResult);
    } catch (IotClientException e) {
      log.error("oneNet 设备更新异常=", e);
    } catch (IotServerException e) {
      log.error("oneNet 设备更新异常=", e);
      return R.error(e.getCode());
    }
    return R.error(ERROR_CODE.DEV_UPDATE_ERROR.getCode(), ERROR_CODE.DEV_UPDATE_ERROR.getName());
  }

  /** 修改本地数据库设备 详见文档 https://apiportalweb.ctwing.cn/index.html#/apiDetail/10255/218/1001 Aleo */
  private Map<String, Object> updateDevInstance(
      IoTDevice ioTDevice, OnenetDownRequest downRequest) {
    if (StrUtil.isNotBlank(downRequest.getOnenetRequestData().getLatitude())
        && StrUtil.isNotBlank(downRequest.getOnenetRequestData().getLongitude())) {
      ioTDevice.setCoordinate(
          StrUtil.join(
              ",",
              downRequest.getOnenetRequestData().getLongitude(),
              downRequest.getOnenetRequestData().getLatitude()));

      SupportMapAreas supportMapAreas =
          supportMapAreasMapper.selectMapAreas(
              downRequest.getOnenetRequestData().getLongitude(),
              downRequest.getOnenetRequestData().getLatitude());
      if (supportMapAreas == null) {
        log.info(
            "查询区域id为空,lot={},lat={}",
            downRequest.getOnenetRequestData().getLongitude(),
            downRequest.getOnenetRequestData().getLatitude());
      } else {
        ioTDevice.setAreasId(supportMapAreas.getId());
      }
    }

    // 组件返回字段
    Map<String, Object> result = new HashMap<>();
    result.put("deviceId", ioTDevice.getDeviceId());
    result.put("areasId", ioTDevice.getAreasId() == null ? "" : ioTDevice.getAreasId());
    ioTDevice.setDetail(downRequest.getDetail());
    ioTDeviceMapper.updateByPrimaryKey(ioTDevice);
    // 设备生命周期-修改
    ioTDeviceLifeCycle.update(ioTDevice.getIotId());
    return result;
  }

  /** 设备批量添加 */
  private R devAdds(OnenetDownRequest downRequest) {
    /*IoTDevice devInstance = IoTDevice.builder().productKey(downRequest.getProductKey())
        .deviceId(downRequest
            .getDeviceId()).build();
    int size = ioTDeviceMapper.selectCount(devInstance);
    if (size > 0) {
      //设备已经存在
      return R.error(ERROR_CODE.DEV_ADD_DEVICE_ID_EXIST.getCode(),
          ERROR_CODE.DEV_ADD_DEVICE_ID_EXIST.getName());
    }*/
    IotProfile profile = new IotProfile();
    profile.userId(userId).accessKey(accessKey);
    IotClient client = IotClient.create(profile);
    // 自定义添加设备类
    AddBatchDeviceRequest addBatchDeviceRequest = new AddBatchDeviceRequest();
    // 自定义添加设备到项目
    AddDeviceProjectRequest addDeviceProjectRequest = new AddDeviceProjectRequest();
    addBatchDeviceRequest.setProductId(
        downRequest.getOnenetRequestData().getConfiguration().get("productId").toString());
    addDeviceProjectRequest.setProductId(
        downRequest.getOnenetRequestData().getConfiguration().get("productId").toString());
    addDeviceProjectRequest.setProjectId(projectId);
    List<Device> deviceList = new ArrayList<>();
    List<String> stringList = new ArrayList<>();
    downRequest
        .getDatas()
        .forEach(
            onenetRequestData -> {
              Devices device = new Devices();
              device.setName(onenetRequestData.getImei());
              device.setImei(onenetRequestData.getImei());
              device.setImsi(onenetRequestData.getImsi());
              deviceList.add(device);
              addBatchDeviceRequest.setDevices(deviceList);
              stringList.add(onenetRequestData.getImei());
              addDeviceProjectRequest.setDevices(stringList);
            });

    try {
      // 安全鉴权计算
      String Authorization = token.getAuthorization();
      addBatchDeviceRequest.setAuthorization(Authorization);
      addDeviceProjectRequest.setAuthorization(Authorization);
    } catch (Exception e) {
      e.printStackTrace();
    }
    // 发送移动
    try {
      BatchCreateDevicesResponse response = client.sendRequest(addBatchDeviceRequest);
      log.info("请求移动={} ，返回={}", addBatchDeviceRequest, response);
      // 添加设备后，再将设备添加到项目中

      AddDeviceResponse response2 = client.sendRequest(addDeviceProjectRequest);
      log.info("请求移动={} ，返回={}", addDeviceProjectRequest, response2);
      List<Map> saveResult = saveDevInstances(downRequest);
      return R.ok(saveResult);

    } catch (IotClientException e) {
      log.error("oneNet 异常=", e);
    } catch (IotServerException e) {
      log.error("oneNet 异常=", e);
      return R.error(e.getCode());
    }
    return R.error(ERROR_CODE.DEV_ADD_ERROR.getCode(), ERROR_CODE.DEV_ADD_ERROR.getName());
  }

  // 本地批量添加设备
  private List<Map> saveDevInstances(OnenetDownRequest downRequest) {
    // 组件返回字段

    List<Map> mapList = new ArrayList<>();
    downRequest
        .getDatas()
        .forEach(
            onenetRequestData -> {
              Map<String, Object> result = new HashMap<>();
              String iotId = IdUtil.simpleUUID();
              IoTDevice ioTDevice =
                  IoTDevice.builder()
                      .deviceId(onenetRequestData.getImei())
                      .createTime(System.currentTimeMillis() / 1000)
                      .deviceName(onenetRequestData.getDeviceName())
                      .iotId(iotId)
                      .state(DeviceStatus.offline.getCode())
                      .creatorId(downRequest.getAppUnionId())
                      .gwProductKey(downRequest.getGwProductKey())
                      .productName(downRequest.getIoTProduct().getName())
                      .productKey(downRequest.getProductKey())
                      .build();
              if (StrUtil.isNotBlank(downRequest.getOnenetRequestData().getLatitude())
                  && StrUtil.isNotBlank(downRequest.getOnenetRequestData().getLongitude())) {

                ioTDevice.setCoordinate(
                    StrUtil.join(
                        ",",
                        downRequest.getOnenetRequestData().getLatitude(),
                        downRequest.getOnenetRequestData().getLongitude()));

                SupportMapAreas supportMapAreas =
                    supportMapAreasMapper.selectMapAreas(
                        downRequest.getOnenetRequestData().getLongitude(),
                        downRequest.getOnenetRequestData().getLatitude());
                if (supportMapAreas == null) {
                  log.info(
                      "查询区域id为空,lot={},lat={}",
                      downRequest.getOnenetRequestData().getLongitude(),
                      downRequest.getOnenetRequestData().getLatitude());
                } else {
                  ioTDevice.setAreasId(supportMapAreas.getId());
                }
              }
              Map<String, Object> config = new HashMap<>();
              config.put("imei", onenetRequestData.getImei());
              config.put("meterNo", downRequest.getOnenetRequestData().getMeterNo());
              ioTDevice.setConfiguration(JSONUtil.toJsonStr(config));
              ioTDeviceMapper.insertUseGeneratedKeys(ioTDevice);
              // 推送设备创建消息
              ioTDeviceLifeCycle.create(
                  downRequest.getProductKey(), onenetRequestData.getImei(), downRequest);
              result.put("iotId", iotId);
              result.put("areasId", ioTDevice.getAreasId() == null ? "" : ioTDevice.getAreasId());
              if (StrUtil.isNotBlank(downRequest.getIoTProduct().getMetadata())) {
                result.put(
                    "metadata", JSONUtil.parseObj(downRequest.getIoTProduct().getMetadata()));
              }
              result.put("productKey", downRequest.getProductKey());
              result.put("deviceNode", downRequest.getIoTProduct().getDeviceNode());
              mapList.add(result);
            });
    return mapList;
  }
}
