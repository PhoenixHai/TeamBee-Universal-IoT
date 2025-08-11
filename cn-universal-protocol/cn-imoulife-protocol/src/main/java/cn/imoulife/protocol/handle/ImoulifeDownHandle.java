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

package cn.imoulife.protocol.handle;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.core.base.R;
import cn.universal.core.iot.constant.IotConstant.DeviceStatus;
import cn.universal.core.iot.constant.IotConstant.DownCmd;
import cn.universal.core.iot.constant.IotConstant.ERROR_CODE;
import cn.universal.core.iot.exception.BizException;
import cn.universal.core.iot.message.DownRequest;
import cn.universal.core.utils.DelayedTaskUtil;
import cn.universal.dm.device.service.impl.IoTDeviceService;
import cn.universal.ossm.entity.SysOss;
import cn.universal.ossm.service.ISysOssService;
import cn.universal.persistence.base.IoTDeviceLifeCycle;
import cn.universal.persistence.base.IotDownAdapter;
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.entity.IoTDevice;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.entity.SupportMapAreas;
import cn.universal.persistence.mapper.IoTDeviceMapper;
import cn.universal.persistence.mapper.IoTProductMapper;
import cn.universal.persistence.mapper.SupportMapAreasMapper;
import cn.imoulife.protocol.config.ImoulifeRequest;
import cn.imoulife.protocol.entity.ImoulifeDownRequest;
import cn.imoulife.protocol.entity.RespBody;
import cn.imoulife.protocol.task.LechenOnlineTask;
import com.aliyuncs.utils.IOUtils;
import jakarta.annotation.Resource;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 乐橙下行实际处理类
 *
 * @version 1.0
 * @since 2025/8/24 12:49
 */
@Slf4j
@Component
public class ImoulifeDownHandle extends IotDownAdapter<ImoulifeDownRequest> {

  @Resource private DelayedTaskUtil delayedTaskUtil;

  @Resource private IoTDeviceMapper ioTDeviceMapper;

  @Resource private SupportMapAreasMapper supportMapAreasMapper;

  @Resource private IoTDeviceService iotDeviceService;

  @Autowired private ImoulifeRequest imoulifeRequest;

  @Autowired private LechenOnlineTask lechenOnlineTask;

  @Resource(name = "ioTDeviceActionAfterService")
  private IoTDeviceLifeCycle ioTDeviceLifeCycle;

  @Resource private ISysOssService iSysOssService;

  @Resource private IoTProductMapper ioTProductMapper;

  public R down(ImoulifeDownRequest downRequest) {
    if (downRequest == null || downRequest.getCmd() == null) {
      log.warn("乐橙设备处理下行对象为空,不处理={}", downRequest);
      return R.error("乐橙设备处理下行对象为空");
    }

    R preR =
        preDown(downRequest.getIoTProduct(), downRequest.getImoulifeRequestData(), downRequest);
    // R.SUCCESS.equals(preR.getCode())
    if (Objects.nonNull(preR)) {
      return preR;
    }

    R r = null;
    switch (downRequest.getCmd()) {
      case DEV_ADD:
        r = devAdd(downRequest);
        break;
      case DEV_DEL:
        r = devDel(downRequest);
        break;
      case DEV_UPDATE:
        r = devUpdate(downRequest);
        break;
      case DEV_FUNCTION:
        r = downFunction(downRequest);
        break;
      default:
        log.info("乐橙设备处理下行未匹配到方法");
    }
    return r;
  }

  private R downFunction(ImoulifeDownRequest downRequest) {
    IoTDevice query =
        IoTDevice.builder()
            .productKey(downRequest.getProductKey())
            .deviceId(downRequest.getDeviceId())
            .build();
    IoTDevice ioTDevice = ioTDeviceMapper.selectOne(query);
    if (Objects.isNull(ioTDevice)) {
      // 设备不存在
      return R.error(
          ERROR_CODE.DEV_CONFIG_DEVICE_NO_ID_EXIST.getCode(),
          ERROR_CODE.DEV_CONFIG_DEVICE_NO_ID_EXIST.getName());
    }

    R r = callGlobalFunction(downRequest.getIoTProduct(), ioTDevice, downRequest);
    if (Objects.nonNull(r)) {
      return r;
    }

    String function = (String) downRequest.getFunction().get("function");
    if (StrUtil.isEmpty(function)) {
      log.warn("乐橙设备处理功能下行 function 为空,不处理={}", downRequest);
      return R.error(
          ERROR_CODE.DEV_CONFIG_DEVICE_NULL.getCode(), ERROR_CODE.DEV_CONFIG_DEVICE_NULL.getName());
    }
    DownCmd downCmd = DownCmd.find(function);
    try {
      switch (downCmd) {
        case DEV_MONITOR_CHECK_ONLINE:
          r = online(downRequest);
          break;
        case DEV_MONITOR_TURN:
          r = devTurn(downRequest);
          break;
        case DEV_MONITOR_PLAY:
          r = devPlay(downRequest);
          break;
        case DEV_ELECTRIC_QUANTITY: // 设备锁电量
          r = devElectricQuantity(downRequest);
          break;
        case DEV_DOOR_KEYS: // 获取设备锁密钥列表
          r = devDoorKeys(downRequest);
          break;
        case DEV_OPENDOOR_RECORD: // 获取开门记录
          r = devOpenDoorRecord(downRequest);
          break;
        case DEV_GENERATE_SNAPKEY: // 生成门锁密码
          r = devGenerateSnapkey(downRequest);
          break;
        case DEV_DELETE_DOORKEY: // 废弃密钥 deleteDoorKey
          r = devDeleteDoorKey(downRequest);
          break;
        case DEV_SNAPKEY_LIST: // 获取临时秘钥列表 SnapkeyList
          r = devSnapkeyList(downRequest);
          break;
        case DEV_WAKE_UP: // 唤醒休眠的门锁设备 wakeUpDevice
          r = devWakeUp(downRequest);
          break;
        case DEV_SNAP: // 直播 抓图
          r = devSnap(downRequest);
          break;
        case DEV_LIVE: // 生成直播流 DeviceLive
          r = devLive(downRequest);
          break;
        case DEV_LIVE_STREAMINFO: // 获取直播流地址 LiveStreamInfo
          r = devLiveStreamInfo(downRequest);
          break;
        case DEV_SETTING: // 获取乐橙设备的设置信息
          r = devSetting(downRequest);
          break;
        case DEV_PLAY_BACK: // 本地回放片段信息
          r = devPlayback(downRequest);
          break;
        case DEV_PLAY_BACKCLOUD: // 云回放片段信息
          r = devPlaybackCloud(downRequest);
          break;
        case DEV_VIDEO_LOCAL: // 查询某天本地录像数量
          r = devVideoLocal(downRequest);
          break;
        case DEV_VIDEO_CLOUD: // 查询某天云录像数量
          r = devVideoCloud(downRequest);
          break;
        case DEV_SDCARD_STATUS: // 获取摄像头SD卡状态
          r = deviceSdcardStatus(downRequest);
          break;
        case DEV_SDCARD: // 获取摄像头存储介质容量信息查询某天云录像数量
          r = deviceStorage(downRequest);
          break;
        case RECOVER_SDCARD: // 摄像头SD卡格式化
          r = recoverSDCard(downRequest);
          break;
        case ALARM_MESSAGE: // 查询用户告警列表
          r = getAlarmMessage(downRequest);
          break;
        case SOUND_VOLUME_SIZE: // 设置设备音量分贝
          r = setSoundVolumeSize(downRequest);
          break;
        case RESTART_DEVICE: // 重启乐橙摄像头
          r = restartDevice(downRequest);
          break;
        case REVERSE_STATUS: // 设置乐橙画面翻转
          r = modifyFrameReverseStatus(downRequest);
          break;
        case ENABLE_CONFIG: // 设置设备使能开关
          r = enableConfig(downRequest);
          break;
        case CAPTURE: // 摄像头抓图，返回本地网络路径
          r = capture(downRequest);
          break;
        case DEVICE_CLOUD: // 获取当前设备的云存储服务信息
          r = getDeviceCloud(downRequest);
          break;
        case DEVICE_VERSIONLIST: // 获取设备版本和可升级信息
          r = deviceVersion(downRequest);
          break;
        case UPGRADE_DEVICE: // 设备升级
          r = upgradeDevice(downRequest);
          break;
        case WIFI_AROUND: // 查询周边wifi
          r = wifiAround(downRequest);
          break;
        case CONTROL_DEVICE_WIFI: // 控制设备连接热点
          r = controlDeviceWifi(downRequest);
          break;
        case CAMERA_SNAPSHOT: // 获取截图
          //          r = cameraSnapshot(downRequest);
          break;
        case CAMERA_STATUS: // 获取使能开关状态
          r = getDeviceCameraStatus(downRequest);
          break;
        case SOUND_VOLUME_SIZE_GET: // 获取设备分贝
          r = getSoundVolumeSize(downRequest);
          break;
        case FRAME_REVERSE_STATUS: // 获取画面翻转状态
          r = frameReverseStatus(downRequest);
          break;
        case SET_STORAGE_STRATEGY: // 设置设备免费云存储服务开关
          r = setStorageStrategy(downRequest);
          break;
        case OPEN_CLOUD_RECORD: // 开通设备云存储
          r = openCloudRecord(downRequest);
          break;
        case DOWNLOAD_RECORD: // 下载录像
          break;
        case UN_BIND_DEVICE_CLOUD: // 解绑设备云存储
          r = unBindDeviceCloud(downRequest);
          break;
        case DEVICE_CLOUD_LIST: // 查询设备通道下所有云存储服务
          r = deviceCloudList(downRequest);
          break;
        case SET_ALL_STORAGE_STRATEGY: // 设置当前设备的云存储服务开关
          r = setAllStorageStrategy(downRequest);
          break;
        case QUERY_CLOUD_RECORD_CALL_NUM: // 查询云存储开通接口的剩余调用次数
          r = queryCloudRecordCallNum(downRequest);
          break;
        case UN_USED_CLOUD_LIST: // 获取未启用的云存储服务列表
          r = unUsedCloudList(downRequest);
          break;
        case GET_DOWN_LOAD_LIST: // 查询录像记录
          break;
        case GET_CLOUD_RECORDS: // 倒序查询设备云录像片段
          r = getCloudRecords(downRequest);
          break;
        case GUERY_LOCAL_RECORD_PLAN: // 查询设备本地录像计划
          r = queryLocalRecordPlan(downRequest);
          break;
        case SET_LOCAL_RECORD_PLAN_RULES: // 设置设备本地录像计划
          r = setLocalRecordPlanRules(downRequest);
          break;
        case QUERY_LOCAL_RECORD_STREAM: // 查询设备本地录像视频流
          r = queryLocalRecordStream(downRequest);
          break;
        case SET_LOCAL_RECORD_STREAM: // 设置设备本地录像视频流
          r = setLocalRecordStream(downRequest);
          break;
        case UP_GRADE_PROCESS_DEVICE: // 设置设备本地录像视频流
          r = upgradeProcessDevice(downRequest);
          break;
        case SET_NIGHT_VISION_MODE: // 设置设备夜视模式
          r = setNightVisionMode(downRequest);
          break;
        case LIST_DEVICE_DETAILS_ID: // 设置设备夜视模式
          r = listDeviceDetailsByIds(downRequest);
          break;
        case CREATR_DEVICE_RTMP: // 创建rtmp协议直播地址
          r = createDeviceRtmpLive(downRequest);
          break;
        case CREATR_DEVICE_FLV: // 创建设备flv直播
          r = createDeviceFlvLive(downRequest);
          break;
        case QUERY_DEVICE_FLV: // 查询设备通道flv直播地址
          r = queryDeviceFlvLive(downRequest);
          break;
        case INTERCOMBYIDS: // 查询设备是否支持云台和对讲
          r = isIntercomByIds(downRequest);
          break;
        default:
          log.info("乐橙设备处理功能下行未匹配到方法");
      }
    } catch (Exception exception) {
    }
    return r;
  }

  private R online(ImoulifeDownRequest downRequest) {
    IoTDevice ioTDevice =
        IoTDevice.builder()
            .productKey(downRequest.getProductKey())
            .deviceId(downRequest.getDeviceId())
            .build();
    IoTDevice instance = ioTDeviceMapper.selectOne(ioTDevice);
    if (instance != null) {
      // 检测设备在线状态
      lechenOnlineTask.checkOnlineStatus(instance);
    }
    return R.ok();
  }

  /** 获取乐橙设备的播放信息 */
  private R devPlay(ImoulifeDownRequest downRequest) throws ParseException {
    // 0:直播、1:本地录像、2:云录像
    JSONObject jsonUtil = JSONUtil.parseObj(downRequest.getFunction().get("data"));
    String playType = jsonUtil.getStr("playType");
    String streamId = jsonUtil.getStr("streamId");
    String channelId = jsonUtil.getStr("channelId");
    if (StrUtil.isEmpty(channelId)) {
      channelId = "0";
    }
    // 获取token
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    param.set("channelId", channelId);
    param.set("type", "0");
    RespBody respBody = imoulifeRequest.request("/openapi/getKitToken", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    JSONObject object2 = JSONUtil.parseObj(jsonObject.get("result"));
    JSONObject aaa = JSONUtil.parseObj(object2.get("data"));
    String token = aaa.getStr("kitToken");
    // 拼接url
    String url1 = "imou://open.lechange.com/";
    String url2 = null;
    String beginTime = jsonUtil.getStr("beginTime");
    String endTime = jsonUtil.getStr("endTime");
    if ("1".equals(streamId)) {
      if ("0".equals(playType)) {
        url2 = "/1?streamId=1";
      } else if ("1".equals(playType)) {
        url2 =
            "/2?streamId=1"
                + "&beginTime="
                + beginTime
                + "&endTime="
                + endTime
                + "&recordType=localRecord";
      } else if ("2".equals(playType)) {
        url2 = "/2?streamId=1" + "&beginTime=" + beginTime + "&endTime=" + endTime;
      }
    } else {
      if ("0".equals(playType)) {
        url2 = "/1?streamId=0";
      } else if ("1".equals(playType)) {
        url2 =
            "/2?streamId=0"
                + "&beginTime="
                + beginTime
                + "&endTime="
                + endTime
                + "&recordType=localRecord";
      } else if ("2".equals(playType)) {
        url2 = "/2?streamId=0" + "&beginTime=" + beginTime + "&endTime=" + endTime;
      }
    }
    IoTDevice ioTDevice =
        IoTDevice.builder()
            .productKey(downRequest.getProductKey())
            .deviceId(downRequest.getDeviceId())
            .build();
    IoTDevice instance = ioTDeviceMapper.selectOne(ioTDevice);
    if (!instance.getState()) {
      // 检测设备在线状态
      lechenOnlineTask.checkOnlineStatus(instance);
    }
    String url = url1 + downRequest.getDeviceId() + "/" + channelId + url2;
    JSONObject object = new JSONObject();
    log.info("乐橙播放信息=" + url);
    String type = "lechen";
    object.set("url", url);
    object.set("type", type);
    object.set("token", token);
    return R.ok("ok", object.toString());
  }

  /** 云台控制 */
  private R devTurn(ImoulifeDownRequest downRequest) {
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    JSONObject data = JSONUtil.parseObj(downRequest.getFunction().get("data"));
    String channelId = data.getStr("channelId");
    if (StrUtil.isEmpty(channelId)) {
      channelId = "0";
    }
    Integer duration = 300;
    IoTProduct product = ioTProductMapper.getProductByProductKey(downRequest.getProductKey());
    String configuration = product.getConfiguration();
    if (StrUtil.isNotBlank(configuration)) {
      JSONObject jsonObject = JSONUtil.parseObj(configuration);
      if (jsonObject.getInt("duration") != null) {
        duration = jsonObject.getInt("duration");
      }
    }
    param.set("operation", data.getStr("direction"));
    param.set("channelId", channelId);
    param.set("duration", duration);
    RespBody respBody = imoulifeRequest.request("/openapi/controlMovePTZ", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    String msg = object.getStr("msg");
    JSONObject datas = new JSONObject();
    datas.set("code", code);
    datas.set("msg", msg);
    if ("0".equals(code)) {
      return R.ok("ok", object.toString());
    }
    return R.ok(0, "", datas);
  }

  // --------------------------------------------------设备锁----------------------------------------------------------

  /** 设备锁电量 */
  private R devElectricQuantity(ImoulifeDownRequest downRequest) {
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    RespBody respBody = imoulifeRequest.request("/openapi/getDevicePowerInfo", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    if ("0".equals(code)) {
      return R.ok("ok", object.toString());
    }
    return R.error();
  }

  /** 设备锁密钥列表 */
  private R devDoorKeys(ImoulifeDownRequest downRequest) {
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    RespBody respBody = imoulifeRequest.request("/openapi/getDoorKeys", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    if ("0".equals(code)) {
      return R.ok("ok", object.toString());
    }
    return R.error();
  }

  /** 设备锁开门信息 */
  private R devOpenDoorRecord(ImoulifeDownRequest downRequest) {
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    String recordId =
        JSONUtil.parseObj(downRequest.getFunction().get("data")).get("recordId").toString();
    if ("".equals(recordId) || recordId == null) {
      System.out.println("recordId值为空！已设置为-1");
      recordId = "-1";
    }
    param.set("recordId", recordId);
    String count = JSONUtil.parseObj(downRequest.getFunction().get("data")).get("count").toString();
    if ("".equals(count) || count == null) {
      System.out.println("count值为空！已设置为500");
      count = "500";
    }
    param.set("count", count);
    RespBody respBody = imoulifeRequest.request("/openapi/getOpenDoorRecord", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));

    String records = JSONUtil.parseObj(object.getStr("data")).getStr("records");
    JSONArray objects = new JSONArray(records);

    String code = object.getStr("code");
    if ("0".equals(code)) {
      //    解析时间
      List<Object> collect =
          objects.stream()
              .map(
                  item -> {
                    JSONObject jsonObj = new JSONObject();
                    String localTime = JSONUtil.parseObj(item).getStr("localTime");
                    String dayTime =
                        localTime.substring(0, 4)
                            + "-"
                            + localTime.substring(4, 6)
                            + "-"
                            + localTime.substring(6, 8);
                    String detailTime =
                        localTime.substring(9, 11)
                            + ":"
                            + localTime.substring(11, 13)
                            + ":"
                            + localTime.substring(13, 15);
                    localTime = dayTime + " " + detailTime;
                    jsonObj.set("dayTime", dayTime);
                    jsonObj.set("detailTime", detailTime);
                    jsonObj.set("localTime", localTime);
                    jsonObj.set("name", JSONUtil.parseObj(item).getStr("name"));
                    jsonObj.set("type", JSONUtil.parseObj(item).getStr("type"));
                    jsonObj.set("recordId", JSONUtil.parseObj(item).getStr("recordId"));
                    return jsonObj;
                  })
              .collect(Collectors.toList());

      Map<String, List<Object>> opens =
          collect.stream()
              .collect(
                  Collectors.groupingBy(
                      item -> {
                        return JSONUtil.parseObj(item).getStr("dayTime");
                      }));

      return R.ok("ok", JSONUtil.parseObj(opens).toString());
    }
    return R.error();
  }

  /** 生成门锁密码 devGenerateSnapkey */
  private R devGenerateSnapkey(ImoulifeDownRequest downRequest) {
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    String name = JSONUtil.parseObj(downRequest.getFunction().get("data")).get("name").toString();
    if ("".equals(name) || name == null) {
      System.out.println("name值不能为空！");
      return R.error("name值不能为空！位置：生成门锁密码");
    }
    param.set("name", name);
    String effectiveNum =
        JSONUtil.parseObj(downRequest.getFunction().get("data")).get("effectiveNum").toString();
    if ("".equals(effectiveNum) || effectiveNum == null) {
      System.out.println("effectiveNum值为空！已设置为-1");
      effectiveNum = "-1";
    }
    param.set("effectiveNum", Integer.parseInt(effectiveNum));
    String effectiveDay =
        JSONUtil.parseObj(downRequest.getFunction().get("data")).get("effectiveDay").toString();
    if ("".equals(effectiveDay) || effectiveDay == null) {
      System.out.println("effectiveDay值为空！位置：生成门锁密码 ");
      return R.error("effectiveDay值为空！位置：生成门锁密码");
    }
    param.set("effectiveDay", Integer.parseInt(effectiveDay));

    JSONObject objects = new JSONObject();
    String period =
        JSONUtil.parseObj(downRequest.getFunction().get("data")).get("period").toString();
    String beginTime =
        JSONUtil.parseObj(downRequest.getFunction().get("data")).get("beginTime").toString();
    String endTime =
        JSONUtil.parseObj(downRequest.getFunction().get("data")).get("endTime").toString();
    if ("".equals(beginTime)) {
      beginTime = "00:00:00";
    }
    if ("".equals(endTime)) {
      endTime = "23:59:59";
    }
    if ("".equals(period)) {
      System.out.println("period值为空，请仔细检查");
      return R.error("period值为空，请仔细检查！位置：生成门锁密码");
    }
    objects.set("period", period);
    objects.set("beginTime", beginTime);
    objects.set("endTime", endTime);

    //    Object arr [] = new Object[1];
    //    arr[0] = objects;
    JSONArray array = new JSONArray();
    array.add(objects);
    param.set("effectPeriod", array.toString());
    RespBody respBody = imoulifeRequest.request("/openapi/generateSnapkey", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    if ("0".equals(code)) {
      return R.ok("ok", object.toString());
    }
    return R.error();
  }

  /** 设备锁 废弃密钥 */
  private R devDeleteDoorKey(ImoulifeDownRequest downRequest) {
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    String type = JSONUtil.parseObj(downRequest.getFunction().get("data")).get("type").toString();
    if ("".equals(type) || type == null) {
      System.out.println("type为空，请仔细检查");
      return R.error("type为空，请仔细检查！位置：设备锁  废弃密钥");
    }
    param.set("type", type);
    String keyId = JSONUtil.parseObj(downRequest.getFunction().get("data")).get("keyId").toString();
    if ("".equals(keyId) || keyId == null) {
      System.out.println("keyId值为空！");
      return R.error("KeyId为空，请仔细检查！位置：设备锁  废弃密钥");
    }
    param.set("keyId", keyId);
    RespBody respBody = imoulifeRequest.request("/openapi/deleteDoorKey", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    if ("0".equals(code)) {
      return R.ok("ok", object.toString());
    }
    return R.error();
  }

  /** 设备锁 获取临时秘钥列表 */
  private R devSnapkeyList(ImoulifeDownRequest downRequest) {
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    RespBody respBody = imoulifeRequest.request("/openapi/getSnapkeyList", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));

    String code = object.getStr("code");
    if ("0".equals(code)) {
      JSONArray objects =
          new JSONArray(JSONUtil.parseObj(object.getStr("data")).getStr("snapkeys"));
      JSONObject js = new JSONObject();
      if (objects.size() > 0) {
        //    解析时间
        List<Object> collect =
            objects.stream()
                .map(
                    item -> {
                      JSONObject jsonObj = new JSONObject();
                      String localTime = JSONUtil.parseObj(item).getStr("localTime");
                      String dayTime =
                          localTime.substring(0, 4)
                              + "-"
                              + localTime.substring(4, 6)
                              + "-"
                              + localTime.substring(6, 8);
                      String detailTime =
                          localTime.substring(9, 11)
                              + ":"
                              + localTime.substring(11, 13)
                              + ":"
                              + localTime.substring(13, 15);
                      localTime = dayTime + " " + detailTime;
                      String createLocalTime = JSONUtil.parseObj(item).getStr("createLocalTime");
                      String cdayTime =
                          createLocalTime.substring(0, 4)
                              + "-"
                              + createLocalTime.substring(4, 6)
                              + "-"
                              + createLocalTime.substring(6, 8);
                      String cdetailTime =
                          createLocalTime.substring(9, 11)
                              + ":"
                              + createLocalTime.substring(11, 13)
                              + ":"
                              + createLocalTime.substring(13, 15);
                      createLocalTime = cdayTime + " " + cdetailTime;
                      jsonObj.set("localTime", localTime);
                      jsonObj.set("keyId", JSONUtil.parseObj(item).getStr("keyId"));
                      jsonObj.set("snapKey", JSONUtil.parseObj(item).getStr("snapKey"));
                      jsonObj.set("name", JSONUtil.parseObj(item).getStr("name"));
                      jsonObj.set("status", JSONUtil.parseObj(item).getStr("status"));
                      jsonObj.set("createLocalTime", createLocalTime);
                      ;

                      return jsonObj;
                    })
                .collect(Collectors.toList());
        js.set("snapkeys", collect);
      }

      return R.ok("", js.toString());
    }
    return R.error();
  }

  /** 设备锁 唤醒休眠的门锁设备 */
  private R devWakeUp(ImoulifeDownRequest downRequest) {
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    param.set("url", "/device/wakeup");
    RespBody respBody = imoulifeRequest.request("/openapi/wakeUpDevice", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    if ("0".equals(code)) {
      return R.ok("ok", object.toString());
    }
    return R.error();
  }

  /** 设备锁 直播抓图 */
  private R devSnap(ImoulifeDownRequest downRequest) {
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    param.set("channelId", "0");
    RespBody respBody = imoulifeRequest.request("/openapi/setDeviceSnap", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    if ("0".equals(code)) {
      return R.ok("", object.getStr("data"));
    }
    return R.error();
  }

  /** 设备锁 生成直播流 */
  private R devLive(ImoulifeDownRequest downRequest) {
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    param.set("channelId", "0");
    //    Integer streamId = (Integer)
    // JSONUtil.parseObj(downRequest.getFunction().get("data")).get("streamId");
    param.set("streamId", 1);
    RespBody respBody = imoulifeRequest.request("/openapi/bindDeviceLive", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    if ("0".equals(code)) {
      return R.ok("ok", object.toString());
    } else if ("LV1001".equals(code)) {
      return devLiveStreamInfo(downRequest);
    }
    return R.error();
  }

  /** 设备锁 获取直播流 */
  private R devLiveStreamInfo(ImoulifeDownRequest downRequest) {
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    param.set("channelId", "0");
    RespBody respBody = imoulifeRequest.request("/openapi/getLiveStreamInfo", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    if ("0".equals(code)) {
      return R.ok("ok", object.toString());
    } else if ("LV1002".equals(code)) {
      return devLive(downRequest);
    }
    return R.error();
  }

  /** 设备锁 获取乐橙设备信息 */
  private R devSetting(ImoulifeDownRequest downRequest) {
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    RespBody respBody = imoulifeRequest.request("/openapi/currentDeviceWifi", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    // 获取 wifi信息
    JSONObject wifi = JSONUtil.parseObj(jsonObject.get("result"));

    String code = wifi.getStr("code");

    if ("0".equals(code)) {
      return R.ok("", wifi.toString());
    }
    return R.error();
  }

  // --------------------------------------------------设备锁
  // end----------------------------------------------------------

  /** 查询某天本地录像数量 */
  private R devVideoLocal(ImoulifeDownRequest downRequest) {
    JSONObject jsonUtil = JSONUtil.parseObj(downRequest.getFunction().get("data"));
    String startTime = jsonUtil.getStr("beginTime");
    String channelId = jsonUtil.getStr("channelId");
    if (StrUtil.isEmpty(channelId)) {
      channelId = "0";
    }
    String timeqian = startTime.substring(0, 10);
    String timehou = "23:59:59";
    String timehou2 = "00:00:00";
    String endTime = timeqian + " " + timehou;
    String beginTime = timeqian + " " + timehou2;
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    param.set("channelId", channelId);
    param.set("beginTime", beginTime);
    param.set("endTime", endTime);
    RespBody respBody = imoulifeRequest.request("/openapi/queryLocalRecordNum", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    String msg = object.getStr("msg");
    JSONObject datas = new JSONObject();
    datas.set("code", code);
    datas.set("msg", msg);
    JSONObject ob = JSONUtil.parseObj(object.get("data"));
    if ("0".equals(code)) {
      JSONObject object1 = new JSONObject();
      object1.set("recordNum", ob.getStr("recordNum"));
      datas.set("data", object1.toString());
      return R.ok().ok(0, "", datas);
    }
    return R.ok(0, "", datas);
  }

  /** 查询某天云录像数量 */
  private R devVideoCloud(ImoulifeDownRequest downRequest) {
    JSONObject jsonUtil = JSONUtil.parseObj(downRequest.getFunction().get("data"));
    String startTime = jsonUtil.getStr("beginTime");
    String channelId = jsonUtil.getStr("channelId");
    if (StrUtil.isEmpty(channelId)) {
      channelId = "0";
    }
    String timeqian = startTime.substring(0, 10);
    String timehou = "23:59:59";
    String timehou2 = "00:00:00";
    String endTime = timeqian + " " + timehou;
    String beginTime = timeqian + " " + timehou2;
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    param.set("channelId", channelId);
    param.set("beginTime", beginTime);
    param.set("endTime", endTime);
    RespBody respBody = imoulifeRequest.request("/openapi/queryCloudRecordNum", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    String msg = object.getStr("msg");
    JSONObject datas = new JSONObject();
    datas.set("code", code);
    datas.set("msg", msg);
    JSONObject ob = JSONUtil.parseObj(object.get("data"));
    if ("0".equals(code)) {
      JSONObject object1 = new JSONObject();
      object1.set("recordNum", ob.getStr("recordNum"));
      datas.set("data", object1.toString());
      return R.ok().ok(0, "", datas);
    }
    return R.ok(0, "", datas);
  }

  /** 本地回放片段信息 */
  private R devPlayback(ImoulifeDownRequest downRequest) throws ParseException {
    JSONObject jsonUtil = JSONUtil.parseObj(downRequest.getFunction().get("data"));
    String beginTime = jsonUtil.getStr("beginTime");
    String channelId = jsonUtil.getStr("channelId");
    String queryRange = jsonUtil.getStr("queryRange");
    if (StrUtil.isEmpty(channelId)) {
      channelId = "0";
    }
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    Long dt1 = df.parse(beginTime).getTime();
    String timeqian = beginTime.substring(0, 10);
    String timehou = "23:59:59";
    String endTime = timeqian + " " + timehou;
    List<JSONObject> list = new ArrayList<>();
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    param.set("channelId", channelId);
    param.set("beginTime", beginTime);
    param.set("endTime", endTime);
    if (StrUtil.isNotBlank(queryRange)) {
      param.set("count", Integer.parseInt(queryRange));
    } else {
      param.set("queryRange", "1-30");
    }
    RespBody respBody2 = imoulifeRequest.request("/openapi/queryLocalRecords", param);
    JSONObject jsonObject2 = JSONUtil.parseObj(respBody2);
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject2);
    JSONObject object2 = JSONUtil.parseObj(jsonObject2.get("result"));
    String code = object2.getStr("code");
    String msg = object2.getStr("msg");
    JSONObject data = new JSONObject();
    data.set("code", code);
    data.set("msg", msg);
    if ("0".equals(code)) {
      JSONObject aaa = JSONUtil.parseObj(object2.get("data"));
      JSONArray records = aaa.getJSONArray("records");
      if (records == null) {
        return R.ok(0, "", data);
      }
      for (int k = 0; k < records.size(); k++) {
        JSONObject obj = JSONUtil.parseObj(records.get(k));
        String beginTimes = obj.getStr("beginTime");
        Long dt2 = df.parse(beginTimes).getTime();
        if (dt2 < dt1) {
          continue;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("recordId", obj.get("recordId"));
        jsonObject.set("fileLength", obj.get("fileLength"));
        jsonObject.set("channelID", obj.get("channelID"));
        jsonObject.set("beginTime", beginTimes);
        jsonObject.set("endTime", obj.get("endTime"));
        jsonObject.set("type", obj.get("type"));
        list.add(jsonObject);
      }
      data.set("data", list);
    }
    return R.ok(0, "", data);
  }

  /** 云回放片段信息 */
  private R devPlaybackCloud(ImoulifeDownRequest downRequest) throws ParseException {
    JSONObject jsonUtil = JSONUtil.parseObj(downRequest.getFunction().get("data"));
    String beginTime = jsonUtil.getStr("beginTime");
    String channelId = jsonUtil.getStr("channelId");
    String queryRange = jsonUtil.getStr("queryRange");
    if (StrUtil.isEmpty(channelId)) {
      channelId = "0";
    }
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    Long dt1 = df.parse(beginTime).getTime();
    String timeqian = beginTime.substring(0, 10);
    String timehou = "23:59:59";
    String endTime = timeqian + " " + timehou;
    List<Map> list = new ArrayList<>();
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    param.set("channelId", channelId);
    param.set("beginTime", beginTime);
    param.set("endTime", endTime);
    if (StrUtil.isNotBlank(queryRange)) {
      param.set("queryRange", "1-" + queryRange);
    } else {
      param.set("queryRange", "1-30");
    }
    RespBody respBody = imoulifeRequest.request("/openapi/queryCloudRecords", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    JSONObject aaa = JSONUtil.parseObj(object.get("data"));
    JSONArray records = aaa.getJSONArray("records");
    String code = object.getStr("code");
    String msg = object.getStr("msg");
    JSONObject data = new JSONObject();
    data.set("code", code);
    data.set("msg", msg);
    if ("0".equals(code)) {
      if (records.size() == 0) {
        return R.ok(0, "", data);
      }
      for (int k = 0; k < records.size(); k++) {
        JSONObject obj = JSONUtil.parseObj(records.get(k));
        String beginTimes = obj.getStr("beginTime");
        Long dt2 = df.parse(beginTimes).getTime();
        if (dt2 < dt1) {
          continue;
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("recordId", obj.get("recordId"));
        map.put("deviceId", obj.get("deviceId"));
        map.put("channelId", obj.get("channelId"));
        map.put("beginTime", obj.get("beginTime"));
        map.put("endTime", obj.get("endTime"));
        map.put("size", obj.get("size"));
        map.put("thumbUrl", obj.get("thumbUrl"));
        map.put("encryptMode", obj.get("encryptMode"));
        map.put("recordRegionId", obj.get("recordRegionId"));
        list.add(map);
      }
      data.set("data", list);
    }
    return R.ok(0, "", data);
  }

  /** 获取摄像头SD卡状态 */
  private R deviceSdcardStatus(ImoulifeDownRequest downRequest) {
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    RespBody respBody = imoulifeRequest.request("/openapi/deviceSdcardStatus", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    String msg = object.getStr("msg");
    JSONObject datas = new JSONObject();
    datas.set("code", code);
    datas.set("msg", msg);
    if ("0".equals(code)) {
      JSONObject ob = JSONUtil.parseObj(object.get("data"));
      JSONObject object1 = new JSONObject();
      object1.set("status", ob.getStr("status"));
      datas.set("data", object1);
      return R.ok(0, "", datas);
    }
    return R.ok(0, "", datas);
  }

  /** 获取设备升级状态和进度 */
  private R upgradeProcessDevice(ImoulifeDownRequest downRequest) {
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    RespBody respBody = imoulifeRequest.request("/openapi/upgradeProcessDevice", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    String msg = object.getStr("msg");
    JSONObject datas = new JSONObject();
    datas.set("code", code);
    datas.set("msg", msg);
    if ("0".equals(code)) {
      JSONObject ob = JSONUtil.parseObj(object.get("data"));
      JSONObject object1 = new JSONObject();
      object1.set("status", ob.getStr("status"));
      datas.set("data", object1);
      return R.ok(0, "", datas);
    }
    return R.ok(0, "", datas);
  }

  /** 获取设备升级状态和进度 */
  private R setNightVisionMode(ImoulifeDownRequest downRequest) {
    JSONObject jsonUtil = JSONUtil.parseObj(downRequest.getFunction().get("data"));
    String mode = jsonUtil.getStr("mode");
    String channelId = jsonUtil.getStr("channelId");
    if (StrUtil.isEmpty(channelId)) {
      channelId = "0";
    }
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    param.set("channelId", channelId);
    param.set("mode", mode);
    RespBody respBody = imoulifeRequest.request("/openapi/setNightVisionMode", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    String msg = object.getStr("msg");
    JSONObject datas = new JSONObject();
    datas.set("code", code);
    datas.set("msg", msg);
    if ("0".equals(code)) {
      JSONObject ob = JSONUtil.parseObj(object.get("data"));
      JSONObject object1 = new JSONObject();
      object1.set("status", ob.getStr("status"));
      datas.set("data", object1);
      return R.ok(0, "", datas);
    }
    return R.ok(0, "", datas);
  }

  /** 设备详情查询(通道信息) */
  private R listDeviceDetailsByIds(ImoulifeDownRequest downRequest) {
    JSONObject param = new JSONObject();
    JSONArray array = new JSONArray();
    JSONObject obj = new JSONObject();
    obj.set("deviceId", downRequest.getDeviceId());
    array.add(obj);
    param.set("deviceList", array);
    RespBody respBody = imoulifeRequest.request("/openapi/listDeviceDetailsByIds", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    String msg = object.getStr("msg");
    JSONObject datas = new JSONObject();
    datas.set("code", code);
    datas.set("msg", msg);
    if ("0".equals(code)) {
      JSONObject data = JSONUtil.parseObj(object.getStr("data"));
      JSONArray deviceList = data.getJSONArray("deviceList");
      JSONObject device = JSONUtil.parseObj(deviceList.get(0));
      JSONArray channelList = device.getJSONArray("channelList");
      int num = channelList.size();
      JSONArray result = new JSONArray();
      for (int i = 0; i < num; i++) {
        JSONObject object1 = new JSONObject();
        JSONObject channel = JSONUtil.parseObj(channelList.get(i));
        object1.set("channelPicUrl", channel.getStr("channelPicUrl"));
        object1.set("csStatus", channel.getStr("csStatus"));
        object1.set("shareFunctions", channel.getStr("shareFunctions"));
        object1.set("channelId", channel.getStr("channelId"));
        object1.set("channelName", channel.getStr("channelName"));
        object1.set("channelAbility", channel.getStr("channelAbility"));
        object1.set("channelStatus", channel.getStr("channelStatus"));
        object1.set("lastOffLineTime", channel.getStr("lastOffLineTime"));
        result.add(object1);
      }
      datas.set("data", result);
      return R.ok(0, "", datas);
    }
    return R.ok(0, "", datas);
  }

  /** 查询设备是否支持对讲 */
  private R isIntercomByIds(ImoulifeDownRequest downRequest) {
    JSONObject param = new JSONObject();
    JSONArray array = new JSONArray();
    JSONObject obj = new JSONObject();
    obj.set("deviceId", downRequest.getDeviceId());
    array.add(obj);
    param.set("deviceList", array);
    RespBody respBody = imoulifeRequest.request("/openapi/listDeviceDetailsByIds", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    String msg = object.getStr("msg");
    JSONObject datas = new JSONObject();
    datas.set("code", code);
    datas.set("msg", msg);
    if ("0".equals(code)) {
      JSONObject data = JSONUtil.parseObj(object.getStr("data"));
      JSONArray deviceList = data.getJSONArray("deviceList");
      JSONObject device = JSONUtil.parseObj(deviceList.get(0));
      String deviceAbility = device.getStr("deviceAbility");
      Boolean AudioTalk = deviceAbility.contains("AudioTalk");
      Boolean PT = deviceAbility.contains("PT");
      Boolean PTZ = deviceAbility.contains("PTZ");
      Boolean AudioTalkV1 = deviceAbility.contains("AudioTalkV1");
      JSONObject result = new JSONObject();
      result.set("AudioTalk", AudioTalk);
      result.set("AudioTalkV1", AudioTalkV1);
      result.set("PT", PT);
      result.set("PTZ", PTZ);
      datas.set("data", result);
      return R.ok(0, "", datas);
    }
    return R.ok(0, "", datas);
  }

  /** 获取摄像头存储介质容量信息 */
  private R deviceStorage(ImoulifeDownRequest downRequest) {
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    RespBody respBody = imoulifeRequest.request("/openapi/deviceStorage", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    String msg = object.getStr("msg");
    JSONObject datas = new JSONObject();
    datas.set("code", code);
    datas.set("msg", msg);
    if ("0".equals(code)) {
      JSONObject ob = JSONUtil.parseObj(object.get("data"));
      JSONObject object1 = new JSONObject();
      object1.set("totalBytes", ob.getLong("totalBytes"));
      object1.set("usedBytes", ob.getLong("usedBytes"));
      datas.set("data", object1);
      return R.ok(0, "", datas);
    }
    return R.ok(0, "", datas);
  }

  /** 摄像头SD卡格式化 */
  private R recoverSDCard(ImoulifeDownRequest downRequest) {
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    RespBody respBody = imoulifeRequest.request("/openapi/recoverSDCard", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    String msg = object.getStr("msg");
    JSONObject datas = new JSONObject();
    datas.set("code", code);
    datas.set("msg", msg);
    if ("0".equals(code)) {
      JSONObject ob = JSONUtil.parseObj(object.get("data"));
      JSONObject object1 = new JSONObject();
      object1.set("result", ob.getStr("result"));
      datas.set("data", object1);
      return R.ok(0, "", datas);
    }
    return R.ok(0, "", datas);
  }

  /** 摄像头使能控制(设备指示灯、设备提示音、红外灯) */
  private R enableConfig(ImoulifeDownRequest downRequest) {
    String deviceId = downRequest.getDeviceId();
    IoTDevice ioTDevice = new IoTDevice();
    ioTDevice.setDeviceId(deviceId);
    List<IoTDevice> ioTDeviceList = ioTDeviceMapper.select(ioTDevice);

    JSONObject jsonUtil = JSONUtil.parseObj(downRequest.getFunction().get("data"));
    String enableType = jsonUtil.getStr("enableType");
    String enable = jsonUtil.getStr("enable");
    String channelId = jsonUtil.getStr("channelId");
    if (StrUtil.isEmpty(channelId)) {
      channelId = "0";
    }
    Boolean e = Boolean.valueOf(enable);
    if ("infraredLight".equals(enableType)) {
      for (IoTDevice d : ioTDeviceList) {
        if ("LC_K71FT_Camera_001".equals(d.getProductKey())) {
          JSONObject param = new JSONObject();
          param.set("deviceId", downRequest.getDeviceId());
          param.set("channelId", channelId);
          if (Boolean.FALSE.equals(e)) {
            param.set("mode", "Off");
          } else if (Boolean.TRUE.equals(e)) {
            param.set("mode", "Infrared");
          }
          RespBody respBody = imoulifeRequest.request("/openapi/setNightVisionMode", param);
          JSONObject jsonObject = JSONUtil.parseObj(respBody);
          log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
          JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
          String code = object.getStr("code");
          String msg = object.getStr("msg");
          JSONObject datas = new JSONObject();
          datas.set("code", code);
          datas.set("msg", msg);
          if ("0".equals(code)) {
            JSONObject object1 = new JSONObject();
            datas.set("data", object1);
            return R.ok(0, "", datas);
          }
          return R.ok(0, "", datas);
        }
      }
    }
    JSONObject param = new JSONObject();
    param.set("deviceId", deviceId);
    param.set("channelId", channelId);
    param.set("enableType", enableType);
    param.set("enable", Boolean.valueOf(enable));
    RespBody respBody = imoulifeRequest.request("/openapi/setDeviceCameraStatus", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    String msg = object.getStr("msg");
    JSONObject datas = new JSONObject();
    datas.set("code", code);
    datas.set("msg", msg);
    if ("0".equals(code)) {
      JSONObject ob = JSONUtil.parseObj(object.get("data"));
      JSONObject object1 = new JSONObject();
      object1.set("result", ob.getStr("result"));
      datas.set("data", object1);
      return R.ok(0, "", datas);
    }
    return R.ok(0, "", datas);
  }

  /** 摄像头获取当前设备的云存储服务信息 */
  private R getDeviceCloud(ImoulifeDownRequest downRequest) {
    JSONObject param = new JSONObject();
    JSONObject jsonUtil = JSONUtil.parseObj(downRequest.getFunction().get("data"));
    String channelId = jsonUtil.getStr("channelId");
    if (StrUtil.isEmpty(channelId)) {
      channelId = "0";
    }
    param.set("deviceId", downRequest.getDeviceId());
    param.set("channelId", channelId);
    log.info("获取当前设备的云存储服务信息：" + downRequest.getDeviceId());
    RespBody respBody = imoulifeRequest.request("/openapi/getDeviceCloud", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    String msg = object.getStr("msg");
    JSONObject datas = new JSONObject();
    datas.set("code", code);
    datas.set("msg", msg);
    log.info("获取当前设备的云存储服务信息回复：" + msg);
    if ("0".equals(code)) {
      JSONObject ob = JSONUtil.parseObj(object.get("data"));
      datas.set("data", ob);
      return R.ok(0, "", datas);
    }
    return R.ok(0, "", datas);
  }

  /** 摄像头获取设备版本和可升级信息 */
  private R deviceVersion(ImoulifeDownRequest downRequest) {
    JSONObject param = new JSONObject();
    param.set("deviceIds", downRequest.getDeviceId());
    RespBody respBody = imoulifeRequest.request("/openapi/deviceVersionList", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    String msg = object.getStr("msg");
    JSONObject datas = new JSONObject();
    datas.set("code", code);
    datas.set("msg", msg);
    if ("0".equals(code)) {
      JSONObject ob = JSONUtil.parseObj(object.get("data"));
      datas.set("data", ob);
      return R.ok(0, "", datas);
    }
    return R.ok(0, "", datas);
  }

  /** 设备升级 */
  private R upgradeDevice(ImoulifeDownRequest downRequest) {
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    RespBody respBody = imoulifeRequest.request("/openapi/upgradeDevice", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    String msg = object.getStr("msg");
    JSONObject datas = new JSONObject();
    datas.set("code", code);
    datas.set("msg", msg);
    if ("0".equals(code)) {
      JSONObject ob = JSONUtil.parseObj(object.get("data"));
      JSONObject object1 = new JSONObject();
      object1.set("result", ob.getStr("result"));
      datas.set("data", object1);
      return R.ok(0, "", datas);
    }
    return R.ok(0, "", datas);
  }

  /** 摄像头周边WIFI信息 */
  private R wifiAround(ImoulifeDownRequest downRequest) {
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    RespBody respBody = imoulifeRequest.request("/openapi/wifiAround", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    String msg = object.getStr("msg");
    JSONObject datas = new JSONObject();
    datas.set("code", code);
    datas.set("msg", msg);
    if ("0".equals(code)) {
      JSONObject ob = JSONUtil.parseObj(object.get("data"));
      datas.set("data", ob);
      return R.ok(0, "", datas);
    }
    return R.ok(0, "", datas);
  }

  /** 摄像头控制设备连接热点 */
  private R controlDeviceWifi(ImoulifeDownRequest downRequest) {
    JSONObject jsonUtil = JSONUtil.parseObj(downRequest.getFunction().get("data"));
    String ssid = jsonUtil.getStr("ssid");
    String bssid = jsonUtil.getStr("bssid");
    String password = jsonUtil.getStr("password");
    Boolean linkEnable = Boolean.valueOf(jsonUtil.getStr("linkEnable"));
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    param.set("ssid", ssid);
    param.set("bssid", bssid);
    param.set("password", password);
    param.set("linkEnable", linkEnable);
    RespBody respBody = imoulifeRequest.request("/openapi/controlDeviceWifi", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    String msg = object.getStr("msg");
    JSONObject datas = new JSONObject();
    datas.set("code", code);
    datas.set("msg", msg);
    if ("0".equals(code)) {
      JSONObject ob = JSONUtil.parseObj(object.get("data"));
      JSONObject object1 = new JSONObject();
      object1.set("result", ob.getStr("result"));
      datas.set("data", object1);
      return R.ok(0, "", datas);
    }
    return R.ok(0, "", datas);
  }

  /** 获取使能开关状态 */
  private R getDeviceCameraStatus(ImoulifeDownRequest downRequest) {
    String deviceId = downRequest.getDeviceId();
    IoTDevice ioTDevice = new IoTDevice();
    ioTDevice.setDeviceId(deviceId);
    List<IoTDevice> ioTDeviceList = ioTDeviceMapper.select(ioTDevice);
    JSONObject jsonUtil = JSONUtil.parseObj(downRequest.getFunction().get("data"));
    String enableType = jsonUtil.getStr("enableType");
    String channelId = jsonUtil.getStr("channelId");
    if (StrUtil.isEmpty(channelId)) {
      channelId = "0";
    }
    if ("infraredLight".equals(enableType)) {
      for (IoTDevice d : ioTDeviceList) {
        if ("LC_K71FT_Camera_001".equals(d.getProductKey())) {
          JSONObject param = new JSONObject();
          param.set("deviceId", downRequest.getDeviceId());
          param.set("channelId", channelId);
          RespBody respBody = imoulifeRequest.request("/openapi/getNightVisionMode", param);
          JSONObject jsonObject = JSONUtil.parseObj(respBody);
          log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
          JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
          String code = object.getStr("code");
          String msg = object.getStr("msg");
          JSONObject datas = new JSONObject();
          datas.set("code", code);
          datas.set("msg", msg);
          if ("0".equals(code)) {
            JSONObject ob = JSONUtil.parseObj(object.get("data"));
            JSONObject object1 = new JSONObject();
            object1.set("status", ob.getStr("mode"));
            datas.set("data", object1);
            return R.ok(0, "", datas);
          }
          return R.ok(0, "", datas);
        }
      }
    }

    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    param.set("channelId", channelId);
    param.set("enableType", enableType);
    RespBody respBody = imoulifeRequest.request("/openapi/getDeviceCameraStatus", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    String msg = object.getStr("msg");
    JSONObject datas = new JSONObject();
    datas.set("code", code);
    datas.set("msg", msg);
    if ("0".equals(code)) {
      JSONObject ob = JSONUtil.parseObj(object.get("data"));
      datas.set("data", ob);
      return R.ok(0, "", datas);
    }
    return R.ok(0, "", datas);
  }

  /** 获取设备分贝 */
  private R getSoundVolumeSize(ImoulifeDownRequest downRequest) {
    JSONObject jsonUtil = JSONUtil.parseObj(downRequest.getFunction().get("data"));
    String type = jsonUtil.getStr("type");
    String channelId = jsonUtil.getStr("channelId");
    if (StrUtil.isEmpty(channelId)) {
      channelId = "0";
    }
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    param.set("channelId", channelId);
    param.set("channelSn", "");
    param.set("type", type);
    RespBody respBody = imoulifeRequest.request("/openapi/getSoundVolumeSize", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    String msg = object.getStr("msg");
    JSONObject datas = new JSONObject();
    datas.set("code", code);
    datas.set("msg", msg);
    if ("0".equals(code)) {
      JSONObject ob = JSONUtil.parseObj(object.get("data"));
      datas.set("data", ob);
      return R.ok(0, "", datas);
    }
    return R.ok(0, "", datas);
  }

  /** 获取画面翻转状态 */
  private R frameReverseStatus(ImoulifeDownRequest downRequest) {
    JSONObject param = new JSONObject();
    JSONObject jsonUtil = JSONUtil.parseObj(downRequest.getFunction().get("data"));
    String channelId = jsonUtil.getStr("channelId");
    if (StrUtil.isEmpty(channelId)) {
      channelId = "0";
    }
    param.set("deviceId", downRequest.getDeviceId());
    param.set("channelId", channelId);
    RespBody respBody = imoulifeRequest.request("/openapi/frameReverseStatus", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    String msg = object.getStr("msg");
    JSONObject datas = new JSONObject();
    datas.set("code", code);
    datas.set("msg", msg);
    if ("0".equals(code)) {
      JSONObject ob = JSONUtil.parseObj(object.get("data"));
      datas.set("data", ob);
      return R.ok(0, "", datas);
    }
    return R.ok(0, "", datas);
  }

  /** 摄像头抓图，返回本地网络路径 */
  private R capture(ImoulifeDownRequest downRequest) {
    String url1 = "http://oss-cn-universal-public.oss-cn-hangzhou.aliyuncs.com/snapshot/";
    JSONObject param = new JSONObject();
    JSONObject jsonUtil = JSONUtil.parseObj(downRequest.getFunction().get("data"));
    String channelId = jsonUtil.getStr("channelId");
    if (StrUtil.isEmpty(channelId)) {
      channelId = "0";
    }
    param.set("deviceId", downRequest.getDeviceId());
    param.set("channelId", channelId);
    RespBody respBody = imoulifeRequest.request("/openapi/setDeviceSnapEnhanced", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    String msg = object.getStr("msg");
    JSONObject datas = new JSONObject();
    datas.set("code", code);
    datas.set("msg", msg);
    if ("0".equals(code)) {
      JSONObject ob = JSONUtil.parseObj(object.get("data"));
      String urlResult = ob.getStr("url");
      JSONObject object1 = new JSONObject();
      object1.set("result", ob.getStr("result"));
      // 截取链接中的文件名
      String fileName =
          urlResult.substring(urlResult.lastIndexOf("/") + 1, urlResult.lastIndexOf("?"));
      String urlLast = url1 + fileName;
      delayedTaskUtil.putTask(
          () -> {
            // 图片转换存本地
            try {
              // 把地址转换成URL对象
              URL url = new URL(urlResult);
              // 创建http链接
              HttpURLConnection conn = (HttpURLConnection) url.openConnection();
              // 设置超时间为3秒
              conn.setConnectTimeout(10 * 1000);
              // 防止屏蔽程序抓取而返回403错误
              conn.setRequestProperty(
                  "User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
              // 得到输入流
              InputStream inputStream = conn.getInputStream();
              int i = 0;
              while (inputStream == null || i <= 10) {
                Thread.sleep(1000);
                inputStream = conn.getInputStream();
                i++;
              }
              if (i > 10) {
                log.info("未下载到乐橙截图");
              }
              // 请求OSS方法
              SysOss resUrl = iSysOssService.uploadStream(inputStream, fileName);
              IOUtils.closeQuietly(inputStream);
            } catch (Exception e) {
              throw new BizException("图片存储异常!!!", e);
            }
          },
          3,
          TimeUnit.SECONDS);
      object1.set("url", urlLast);
      object1.set("lcurl", urlResult);
      datas.set("data", object1);

      return R.ok(0, "", datas);
    }
    return R.ok(0, "", datas);
  }

  /** 用户告警列表 */
  private R getAlarmMessage(ImoulifeDownRequest downRequest) throws ParseException {
    JSONObject jsonUtil = JSONUtil.parseObj(downRequest.getFunction().get("data"));
    String beginTime = jsonUtil.getStr("beginTime");
    String count = jsonUtil.getStr("count");
    String channelId = jsonUtil.getStr("channelId");
    if (StrUtil.isEmpty(channelId)) {
      channelId = "0";
    }
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    String timeqian = beginTime.substring(0, 10);
    String timehou = "23:59:59";
    String endTime = timeqian + " " + timehou;
    List<JSONObject> list = new ArrayList<>();
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    param.set("channelId", channelId);
    param.set("beginTime", beginTime);
    param.set("endTime", endTime);
    param.set("count", count);
    RespBody respBody = imoulifeRequest.request("/openapi/getAlarmMessage", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    JSONObject aaa = JSONUtil.parseObj(object.get("data"));
    JSONArray alarms = aaa.getJSONArray("alarms");
    String code = object.getStr("code");
    String msg = object.getStr("msg");
    JSONObject data = new JSONObject();
    data.set("code", code);
    data.set("msg", msg);
    if ("0".equals(code)) {
      if (alarms.size() == 0) {
        return R.ok(0, "", data);
      }
      for (int k = 0; k < alarms.size(); k++) {
        JSONObject obj = JSONUtil.parseObj(alarms.get(k));
        JSONObject object1 = new JSONObject();
        object1.set("alarmId", obj.get("alarmId"));
        object1.set("name", obj.get("name"));
        object1.set("time", obj.get("time"));
        object1.set("localDate", obj.get("localDate"));
        object1.set("type", obj.get("type"));
        object1.set("thumbUrl", obj.get("thumbUrl"));
        object1.set("deviceId", obj.get("deviceId"));
        object1.set("picurlArray", obj.getJSONArray("picurlArray"));
        list.add(object1);
      }
      Collections.reverse(list);
      data.set("data", list);
    }
    return R.ok(0, "", data);
  }

  /** 设置设备音量分贝 */
  private R setSoundVolumeSize(ImoulifeDownRequest downRequest) {
    JSONObject jsonUtil = JSONUtil.parseObj(downRequest.getFunction().get("data"));
    String type = jsonUtil.getStr("type");
    String value = jsonUtil.getStr("value");
    String channelId = jsonUtil.getStr("channelId");
    if (StrUtil.isEmpty(channelId)) {
      channelId = "0";
    }
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    param.set("channelId", channelId);
    param.set("channelSn", "");
    param.set("value", value);
    param.set("type", type);
    RespBody respBody = imoulifeRequest.request("/openapi/setSoundVolumeSize", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    String msg = object.getStr("msg");
    JSONObject datas = new JSONObject();
    datas.set("code", code);
    datas.set("msg", msg);
    if ("0".equals(code)) {
      JSONObject ob = JSONUtil.parseObj(object.get("data"));
      JSONObject object1 = new JSONObject();
      object1.set("result", ob.getStr("result"));
      datas.set("data", object1);
      return R.ok(0, "", datas);
    }
    return R.ok(0, "", datas);
  }

  /** 重启乐橙摄像头 */
  private R restartDevice(ImoulifeDownRequest downRequest) {
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    RespBody respBody = imoulifeRequest.request("/openapi/restartDevice", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    String msg = object.getStr("msg");
    JSONObject datas = new JSONObject();
    datas.set("code", code);
    datas.set("msg", msg);
    if ("0".equals(code)) {
      JSONObject ob = JSONUtil.parseObj(object.get("data"));
      JSONObject object1 = new JSONObject();
      object1.set("result", ob.getStr("result"));
      datas.set("data", object1);
      return R.ok(0, "", datas);
    }
    return R.ok(0, "", datas);
  }

  /** 设置乐橙摄像头画面翻转 */
  private R modifyFrameReverseStatus(ImoulifeDownRequest downRequest) {
    JSONObject jsonUtil = JSONUtil.parseObj(downRequest.getFunction().get("data"));
    String direction = jsonUtil.getStr("direction");
    String channelId = jsonUtil.getStr("channelId");
    if (StrUtil.isEmpty(channelId)) {
      channelId = "0";
    }
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    param.set("channelId", channelId);
    param.set("direction", direction);
    RespBody respBody = imoulifeRequest.request("/openapi/modifyFrameReverseStatus", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    String msg = object.getStr("msg");
    JSONObject datas = new JSONObject();
    datas.set("code", code);
    datas.set("msg", msg);
    if ("0".equals(code)) {
      JSONObject ob = JSONUtil.parseObj(object.get("data"));
      JSONObject object1 = new JSONObject();
      object1.set("result", ob.getStr("result"));
      datas.set("data", object1);
      return R.ok(0, "", datas);
    }
    return R.ok(0, "", datas);
  }

  /** 设置设备免费云存储服务开关 */
  private R setStorageStrategy(ImoulifeDownRequest downRequest) {
    JSONObject jsonUtil = JSONUtil.parseObj(downRequest.getFunction().get("data"));
    String status = jsonUtil.getStr("status");
    String channelId = jsonUtil.getStr("channelId");
    if (StrUtil.isEmpty(channelId)) {
      channelId = "0";
    }
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    param.set("status", status);
    param.set("channelId", channelId);
    RespBody respBody = imoulifeRequest.request("/openapi/setStorageStrategy", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    String msg = object.getStr("msg");
    JSONObject datas = new JSONObject();
    datas.set("code", code);
    datas.set("msg", msg);
    if ("0".equals(code)) {
      JSONObject ob = JSONUtil.parseObj(object.get("data"));
      JSONObject object1 = new JSONObject();
      object1.set("result", ob.getStr("result"));
      datas.set("data", object1);
      return R.ok(0, "", datas);
    }
    return R.ok(0, "", datas);
  }

  /** 开通设备云存储 */
  private R openCloudRecord(ImoulifeDownRequest downRequest) {
    JSONObject jsonUtil = JSONUtil.parseObj(downRequest.getFunction().get("data"));
    String strategyId = jsonUtil.getStr("strategyId");
    String deviceCloudId = jsonUtil.getStr("deviceCloudId");
    String channelId = jsonUtil.getStr("channelId");
    if (StrUtil.isEmpty(channelId)) {
      channelId = "0";
    }
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    param.set("strategyId", strategyId);
    param.set("deviceCloudId", deviceCloudId);
    param.set("channelId", channelId);
    log.info("乐橙摄像头云存储分配：" + downRequest.getDeviceId() + ",分配套餐:" + strategyId);
    RespBody respBody = imoulifeRequest.request("/openapi/openCloudRecord", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    String msg = object.getStr("msg");
    JSONObject datas = new JSONObject();
    datas.set("code", code);
    datas.set("msg", msg);
    log.info("乐橙摄像头云存储分配回复：" + msg);
    if ("0".equals(code)) {
      JSONObject ob = JSONUtil.parseObj(object.get("data"));
      JSONObject object1 = new JSONObject();
      object1.set("result", ob.getStr("result"));
      datas.set("data", object1);
      return R.ok(0, "", datas);
    }
    return R.ok(0, "", datas);
  }

  /** 解绑设备云存储 */
  private R unBindDeviceCloud(ImoulifeDownRequest downRequest) {
    JSONObject jsonUtil = JSONUtil.parseObj(downRequest.getFunction().get("data"));
    String channelId = jsonUtil.getStr("channelId");
    if (StrUtil.isEmpty(channelId)) {
      channelId = "0";
    }
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    param.set("channelId", channelId);
    log.info("乐橙摄像头云存储解绑：" + downRequest.getDeviceId());
    RespBody respBody = imoulifeRequest.request("/openapi/unBindDeviceCloud", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    String msg = object.getStr("msg");
    JSONObject datas = new JSONObject();
    datas.set("code", code);
    datas.set("msg", msg);
    log.info("乐橙摄像头云存储解绑回复：" + msg);
    if ("0".equals(code)) {
      JSONObject ob = JSONUtil.parseObj(object.get("data"));
      JSONObject object1 = new JSONObject();
      object1.set("result", ob.getStr("result"));
      datas.set("data", object1);
      log.info("乐橙摄像头云存储解绑回复结果：" + datas);
      return R.ok(0, "", datas);
    }
    return R.ok(0, "", datas);
  }

  /** 查询设备通道下所有云存储服务 */
  private R deviceCloudList(ImoulifeDownRequest downRequest) {
    JSONObject jsonUtil = JSONUtil.parseObj(downRequest.getFunction().get("data"));
    String channelId = jsonUtil.getStr("channelId");
    if (StrUtil.isEmpty(channelId)) {
      channelId = "0";
    }
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    param.set("channelId", channelId);
    log.info("查询设备通道下所有云存储服务：" + downRequest.getDeviceId());
    RespBody respBody = imoulifeRequest.request("/openapi/deviceCloudList", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    String msg = object.getStr("msg");
    JSONObject datas = new JSONObject();
    datas.set("code", code);
    datas.set("msg", msg);
    log.info("查询设备通道下所有云存储服务回复：" + msg);
    if ("0".equals(code)) {
      JSONObject ob = JSONUtil.parseObj(object.get("data"));
      JSONObject object1 = new JSONObject();
      object1.set("result", ob.getStr("result"));
      datas.set("data", object1);
      log.info("查询设备通道下所有云存储服务回复结果：" + datas);
      return R.ok(0, "", datas);
    }
    return R.ok(0, "", datas);
  }

  /** 设置当前设备的云存储服务开关 */
  private R setAllStorageStrategy(ImoulifeDownRequest downRequest) {
    JSONObject jsonUtil = JSONUtil.parseObj(downRequest.getFunction().get("data"));
    String status = jsonUtil.getStr("status");
    String channelId = jsonUtil.getStr("channelId");
    if (StrUtil.isEmpty(channelId)) {
      channelId = "0";
    }
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    param.set("channelId", channelId);
    param.set("status", status);
    log.info("设置当前设备的云存储服务开关：" + downRequest.getDeviceId());
    RespBody respBody = imoulifeRequest.request("/openapi/setAllStorageStrategy", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    String msg = object.getStr("msg");
    JSONObject datas = new JSONObject();
    datas.set("code", code);
    datas.set("msg", msg);
    log.info("设置当前设备的云存储服务开关回复：" + msg);
    if ("0".equals(code)) {
      JSONObject ob = JSONUtil.parseObj(object.get("data"));
      JSONObject object1 = new JSONObject();
      object1.set("result", ob.getStr("result"));
      datas.set("data", object1);
      log.info("设置当前设备的云存储服务开关回复结果：" + datas);
      return R.ok(0, "", datas);
    }
    return R.ok(0, "", datas);
  }

  /** 查询云存储开通接口的剩余调用次数 */
  private R queryCloudRecordCallNum(ImoulifeDownRequest downRequest) {
    JSONObject jsonUtil = JSONUtil.parseObj(downRequest.getFunction().get("data"));
    String strategyId = jsonUtil.getStr("strategyId");
    JSONObject param = new JSONObject();
    param.set("strategyId", strategyId);
    log.info("查询云存储开通接口的剩余调用次数：" + downRequest.getDeviceId());
    RespBody respBody = imoulifeRequest.request("/openapi/queryCloudRecordCallNum", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    String msg = object.getStr("msg");
    JSONObject datas = new JSONObject();
    datas.set("code", code);
    datas.set("msg", msg);
    log.info("查询云存储开通接口的剩余调用次数回复：" + msg);
    if ("0".equals(code)) {
      JSONObject ob = JSONUtil.parseObj(object.get("data"));
      JSONObject object1 = new JSONObject();
      object1.set("result", ob.getStr("result"));
      datas.set("data", object1);
      log.info("查询云存储开通接口的剩余调用次数结果：" + datas);
      return R.ok(0, "", datas);
    }
    return R.ok(0, "", datas);
  }

  /** 获取未启用的云存储服务列表 */
  private R unUsedCloudList(ImoulifeDownRequest downRequest) {
    JSONObject jsonUtil = JSONUtil.parseObj(downRequest.getFunction().get("data"));
    String strategyId = jsonUtil.getStr("strategyId");
    JSONObject param = new JSONObject();
    param.set("nextDeviceCloudId", "-1");
    param.set("limit", "10");
    log.info("获取未启用的云存储服务列表：" + downRequest.getDeviceId());
    RespBody respBody = imoulifeRequest.request("/openapi/unUsedCloudList", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    String msg = object.getStr("msg");
    JSONObject datas = new JSONObject();
    datas.set("code", code);
    datas.set("msg", msg);
    log.info("获取未启用的云存储服务列表回复：" + msg);
    if ("0".equals(code)) {
      JSONObject ob = JSONUtil.parseObj(object.get("data"));
      JSONObject object1 = new JSONObject();
      object1.set("result", ob.getStr("result"));
      datas.set("data", object1);
      log.info("获取未启用的云存储服务列表结果：" + datas);
      return R.ok(0, "", datas);
    }
    return R.ok(0, "", datas);
  }

  /** 倒序查询设备云录像片段 */
  private R getCloudRecords(ImoulifeDownRequest downRequest) {
    JSONObject jsonUtil = JSONUtil.parseObj(downRequest.getFunction().get("data"));
    String beginTime = jsonUtil.getStr("beginTime");
    String endTime = jsonUtil.getStr("endTime");
    String count = jsonUtil.getStr("count");
    String channelId = jsonUtil.getStr("channelId");
    if (StrUtil.isEmpty(channelId)) {
      channelId = "0";
    }
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    param.set("channelId", channelId);
    param.set("beginTime", beginTime);
    param.set("endTime", endTime);
    param.set("count", count);
    RespBody respBody = imoulifeRequest.request("/openapi/getCloudRecords", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    String msg = object.getStr("msg");
    JSONObject datas = new JSONObject();
    datas.set("code", code);
    datas.set("msg", msg);
    if ("0".equals(code)) {
      JSONObject ob = JSONUtil.parseObj(object.get("data"));
      JSONArray object1 = new JSONArray();
      object1.add(ob.getStr("records"));
      datas.set("data", object1);
      return R.ok(0, "", datas);
    }
    return R.ok(0, "", datas);
  }

  /** 查询设备本地录像计划 */
  private R queryLocalRecordPlan(ImoulifeDownRequest downRequest) {
    JSONObject param = new JSONObject();
    JSONObject jsonUtil = JSONUtil.parseObj(downRequest.getFunction().get("data"));
    String channelId = jsonUtil.getStr("channelId");
    if (StrUtil.isEmpty(channelId)) {
      channelId = "0";
    }
    param.set("deviceId", downRequest.getDeviceId());
    param.set("channelId", channelId);
    RespBody respBody = imoulifeRequest.request("/openapi/queryLocalRecordPlan", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    String msg = object.getStr("msg");
    JSONObject datas = new JSONObject();
    datas.set("code", code);
    datas.set("msg", msg);
    if ("0".equals(code)) {
      JSONObject ob = JSONUtil.parseObj(object.get("data"));
      JSONArray object1 = new JSONArray();
      object1.add(ob.getStr("rules"));
      datas.set("data", object1);
      return R.ok(0, "", datas);
    }
    return R.ok(0, "", datas);
  }

  /** 设置设备本地录像计划 */
  private R setLocalRecordPlanRules(ImoulifeDownRequest downRequest) {
    JSONObject jsonUtil = JSONUtil.parseObj(downRequest.getFunction().get("data"));
    String beginTime = jsonUtil.getStr("beginTime");
    String endTime = jsonUtil.getStr("endTime");
    String period = jsonUtil.getStr("period");
    String channelId = jsonUtil.getStr("channelId");
    if (StrUtil.isEmpty(channelId)) {
      channelId = "0";
    }
    JSONObject param = new JSONObject();
    Map<String, String> rules = new HashMap<>();
    JSONArray jsonArray = new JSONArray();
    rules.put("beginTime", beginTime);
    rules.put("endTime", endTime);
    rules.put("period", period);
    jsonArray.add(rules);
    param.set("deviceId", downRequest.getDeviceId());
    param.set("channelId", channelId);
    param.set("rules", jsonArray);
    RespBody respBody = imoulifeRequest.request("/openapi/setLocalRecordPlanRules", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    String msg = object.getStr("msg");
    JSONObject datas = new JSONObject();
    datas.set("code", code);
    datas.set("msg", msg);
    if ("0".equals(code)) {
      JSONObject ob = JSONUtil.parseObj(object.get("data"));
      JSONObject object1 = new JSONObject();
      object1.set("result", ob.getStr("result"));
      datas.set("data", object1);
      return R.ok(0, "", datas);
    }
    return R.ok(0, "", datas);
  }

  /** 查询设备本地录像视频流 */
  private R queryLocalRecordStream(ImoulifeDownRequest downRequest) {
    JSONObject param = new JSONObject();
    JSONObject jsonUtil = JSONUtil.parseObj(downRequest.getFunction().get("data"));
    String channelId = jsonUtil.getStr("channelId");
    if (StrUtil.isEmpty(channelId)) {
      channelId = "0";
    }
    param.set("deviceId", downRequest.getDeviceId());
    param.set("channelId", channelId);
    RespBody respBody = imoulifeRequest.request("/openapi/queryLocalRecordStream", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    String msg = object.getStr("msg");
    JSONObject datas = new JSONObject();
    datas.set("code", code);
    datas.set("msg", msg);
    if ("0".equals(code)) {
      JSONObject ob = JSONUtil.parseObj(object.get("data"));
      JSONObject object1 = new JSONObject();
      object1.set("result", ob.getStr("result"));
      datas.set("data", object1);
      return R.ok(0, "", datas);
    }
    return R.ok(0, "", datas);
  }

  /** 设置设备本地录像视频流 */
  private R setLocalRecordStream(ImoulifeDownRequest downRequest) {
    JSONObject jsonUtil = JSONUtil.parseObj(downRequest.getFunction().get("data"));
    String streamType = jsonUtil.getStr("streamType");
    String channelId = jsonUtil.getStr("channelId");
    if (StrUtil.isEmpty(channelId)) {
      channelId = "0";
    }
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    param.set("channelId", channelId);
    param.set("streamType", streamType);
    RespBody respBody = imoulifeRequest.request("/openapi/setLocalRecordStream", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String code = object.getStr("code");
    String msg = object.getStr("msg");
    JSONObject datas = new JSONObject();
    datas.set("code", code);
    datas.set("msg", msg);
    if ("0".equals(code)) {
      JSONObject ob = JSONUtil.parseObj(object.get("data"));
      JSONObject object1 = new JSONObject();
      object1.set("result", ob.getStr("result"));
      datas.set("data", object1);
      return R.ok(0, "", datas);
    }
    return R.ok(0, "", datas);
  }

  /** 修改摄像头的名称 */
  private R devUpdate(ImoulifeDownRequest downRequest) {
    IoTDevice ioTDevice =
        IoTDevice.builder()
            .productKey(downRequest.getProductKey())
            .deviceId(downRequest.getDeviceId())
            .build();
    int size = ioTDeviceMapper.selectCount(ioTDevice);
    if (size == 0) {
      // 设备不存在
      return R.error(
          ERROR_CODE.DEV_UPDATE_DEVICE_NO_ID_EXIST.getCode(),
          ERROR_CODE.DEV_UPDATE_DEVICE_NO_ID_EXIST.getName());
    }
    // 添加设备后同步修改设备名称
    JSONObject paramName = new JSONObject();
    paramName.set("deviceId", downRequest.getDeviceId());
    paramName.set("name", downRequest.getImoulifeRequestData().getDeviceName());
    RespBody respBodyName = imoulifeRequest.request("/openapi/modifyDeviceName", paramName);
    JSONObject jsonObjectName = JSONUtil.parseObj(JSONUtil.toJsonStr(respBodyName));
    log.info("请求乐橙结果,入参={},返回={}", paramName, jsonObjectName);
    // 访问数据库获取设备信息
    IoTDevice dev = ioTDeviceMapper.selectOne(ioTDevice);
    // 修改设备名称
    dev.setDeviceName(downRequest.getImoulifeRequestData().getDeviceName());
    // 更新设备名称
    Map<String, Object> updateResult = updateDevInstance(dev, downRequest);
    return R.ok(updateResult);
  }

  private Map<String, Object> updateDevInstance(
      IoTDevice ioTDevice, ImoulifeDownRequest downRequest) {
    if (StrUtil.isNotBlank(downRequest.getImoulifeRequestData().getLatitude())
        && StrUtil.isNotBlank(downRequest.getImoulifeRequestData().getLongitude())) {
      ioTDevice.setCoordinate(
          StrUtil.join(
              ",",
              downRequest.getImoulifeRequestData().getLongitude(),
              downRequest.getImoulifeRequestData().getLatitude()));

      SupportMapAreas supportMapAreas =
          supportMapAreasMapper.selectMapAreas(
              downRequest.getImoulifeRequestData().getLongitude(),
              downRequest.getImoulifeRequestData().getLatitude());
      if (supportMapAreas == null) {
        log.info(
            "查询区域id为空,lot={},lat={}",
            downRequest.getImoulifeRequestData().getLongitude(),
            downRequest.getImoulifeRequestData().getLatitude());
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

  /** 删除设备 删除设备接口文档：https://open.ys7.com/doc/zh/book/index/device_option.html#device_option-api2 */
  private R devDel(ImoulifeDownRequest downRequest) {
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
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    RespBody respBody = imoulifeRequest.request("/openapi/unBindDevice", param);
    JSONObject jsonObject = JSONUtil.parseObj(JSONUtil.toJsonStr(respBody));
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String resultCode = object.getStr("code");
    if ("0".equals(resultCode)) {
      // 操作本地数据库
      deleteDevInstance(instance, downRequest);
      return R.ok();
    } else if ("DV1028".equals(resultCode)) {
      return R.error("用户未绑定设备或设备通道号不存在");
    }
    log.info("乐橙设备删除失败,入参={},返回={}", param, jsonObject);
    return R.error("解绑失败");
  }

  /** 创建flv协议直播地址 */
  private R createDeviceFlvLive(ImoulifeDownRequest downRequest) {
    JSONObject jsonUtil = JSONUtil.parseObj(downRequest.getFunction().get("data"));
    String channelId = jsonUtil.getStr("channelId");
    if (StrUtil.isEmpty(channelId)) {
      channelId = "0";
    }
    String beginTime = jsonUtil.getStr("beginTime");
    String endTime = jsonUtil.getStr("endTime");
    String type = jsonUtil.getStr("type");
    String recordType = jsonUtil.getStr("recordType");
    // 获取token
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    param.set("channelId", channelId);
    param.set("type", type);
    param.set("beginTime", beginTime);
    param.set("endTime", endTime);
    param.set("recordType", recordType);
    RespBody respBody = imoulifeRequest.request("/openapi/createDeviceFlvLive", param);
    JSONObject jsonObjects = JSONUtil.parseObj(respBody);
    JSONObject object = JSONUtil.parseObj(jsonObjects.get("result"));
    return R.ok("ok", object.toString());
  }

  /** 查询设备通道flv直播地址 */
  private R queryDeviceFlvLive(ImoulifeDownRequest downRequest) {
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    JSONObject data = JSONUtil.parseObj(downRequest.getFunction().get("data"));
    String channelId = data.getStr("channelId");
    if (StrUtil.isEmpty(channelId)) {
      channelId = "0";
    }
    param.set("channelId", channelId);
    RespBody respBody = imoulifeRequest.request("/openapi/queryDeviceFlvLive", param);
    JSONObject jsonObjects = JSONUtil.parseObj(respBody);
    JSONObject object = JSONUtil.parseObj(jsonObjects.get("result"));
    return R.ok(0, "", object);
  }

  /** 创建rtmp协议直播地址 */
  private R createDeviceRtmpLive(ImoulifeDownRequest downRequest) {
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    JSONObject data = JSONUtil.parseObj(downRequest.getFunction().get("data"));
    String channelId = data.getStr("channelId");
    if (StrUtil.isEmpty(channelId)) {
      channelId = "0";
    }
    param.set("channelId", channelId);
    param.set("isTalk", Boolean.TRUE);
    RespBody respBody = imoulifeRequest.request("/openapi/createDeviceRtmpLive", param);
    JSONObject jsonObject = JSONUtil.parseObj(respBody);
    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    return R.ok(0, "", object);
  }

  private void deleteDevInstance(IoTDevice ioTDevice, DownRequest downRequest) {
    Map<String, Object> param = new HashMap<>();
    param.put("iotId", ioTDevice.getIotId());
    IoTDeviceDTO ioTDeviceDTO = ioTDeviceMapper.selectIoTDeviceBO(param);
    ioTDeviceLifeCycle.delete(ioTDeviceDTO, downRequest);
    iotDeviceService.delDevInstance(ioTDevice.getIotId());
  }

  /** 添加设备 添加设备接口文档 */
  private R devAdd(ImoulifeDownRequest downRequest) {
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
    String code = downRequest.getImoulifeRequestData().getAppKey();
    JSONObject param = new JSONObject();
    param.set("deviceId", downRequest.getDeviceId());
    param.set("code", code);
    RespBody respBody = imoulifeRequest.request("/openapi/bindDevice", param);
    JSONObject jsonObject = JSONUtil.parseObj(JSONUtil.toJsonStr(respBody));
    log.info("请求乐橙结果,入参={},返回={}", param, jsonObject);
    // 添加设备后同步修改设备名称
    JSONObject paramName = new JSONObject();
    paramName.set("deviceId", downRequest.getDeviceId());
    paramName.set("name", downRequest.getImoulifeRequestData().getDeviceName());
    RespBody respBodyName = imoulifeRequest.request("/openapi/modifyDeviceName", paramName);
    JSONObject jsonObjectName = JSONUtil.parseObj(JSONUtil.toJsonStr(respBodyName));
    log.info("请求乐橙结果,入参={},返回={}", paramName, jsonObjectName);

    JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
    String resultCode = object.getStr("code");
    String msg = object.getStr("msg");
    log.info("respBody" + respBody);
    if ("0".equals(resultCode)) {
      //      JSONObject params = new JSONObject();
      //      param.set("deviceId", downRequest.getDeviceId());
      //      param.set("status", "on");
      //      param.set("channelId", "0");
      //      imoulifeRequest.request("/openapi/setStorageStrategy", params);
      // 操作本地数据库
      Map<String, Object> saveResult = saveDevInstance(downRequest);
      // 检测设备在线状态
      lechenOnlineTask.delayCheckOnlineStatus(ioTDeviceMapper.selectOne(ioTDevice));
      if ("LC_LC_Lock_001".equals(downRequest.getProductKey())) {
        devLive(downRequest);
      }
      return R.ok(saveResult);
    }
    log.info("乐橙设备添加失败,入参={},返回={}", param, jsonObject);
    return R.error(jsonObject.toString());
  }

  @Override
  public void Rule() {}

  private Map<String, Object> saveDevInstance(ImoulifeDownRequest downRequest) {
    IoTDevice ioTDevice =
        IoTDevice.builder()
            .deviceId(downRequest.getDeviceId())
            .createTime(System.currentTimeMillis() / 1000)
            .deviceName(downRequest.getImoulifeRequestData().getDeviceName())
            .state(DeviceStatus.offline.getCode())
            .iotId(IdUtil.simpleUUID())
            .creatorId(downRequest.getAppUnionId())
            .productName(downRequest.getIoTProduct().getName())
            .application(downRequest.getApplicationId())
            .detail(downRequest.getDetail())
            .gwProductKey(downRequest.getGwProductKey())
            .extDeviceId(downRequest.getGwDeviceId())
            .productKey(downRequest.getProductKey())
            .build();
    Map<String, Object> config = new HashMap<>();
    // 丢弃值
    config.put("discardValue", 0);
    finalDown(
        config,
        downRequest.getIoTProduct(),
        downRequest.getCmd(),
        downRequest.getImoulifeRequestData());
    ioTDevice.setConfiguration(JSONUtil.toJsonStr(config));

    if (StrUtil.isNotBlank(downRequest.getImoulifeRequestData().getLatitude())
        && StrUtil.isNotBlank(downRequest.getImoulifeRequestData().getLongitude())) {
      ioTDevice.setCoordinate(
          StrUtil.join(
              ",",
              downRequest.getImoulifeRequestData().getLongitude(),
              downRequest.getImoulifeRequestData().getLatitude()));

      SupportMapAreas supportMapAreas =
          supportMapAreasMapper.selectMapAreas(
              downRequest.getImoulifeRequestData().getLongitude(),
              downRequest.getImoulifeRequestData().getLatitude());
      if (supportMapAreas == null) {
        log.info(
            "查询区域id为空,lot={},lat={}",
            downRequest.getImoulifeRequestData().getLongitude(),
            downRequest.getImoulifeRequestData().getLatitude());
      } else {
        ioTDevice.setAreasId(supportMapAreas.getId());
      }
    }

    ioTDeviceMapper.insertUseGeneratedKeys(ioTDevice);
    ioTDeviceLifeCycle.create(downRequest.getProductKey(), downRequest.getDeviceId(), downRequest);
    // 组件返回字段
    Map<String, Object> result = new HashMap<>();
    result.put("iotId", ioTDevice.getIotId());
    result.put("areasId", ioTDevice.getAreasId() == null ? "" : ioTDevice.getAreasId());
    if (StrUtil.isNotBlank(downRequest.getIoTProduct().getMetadata())) {
      result.put("metadata", JSONUtil.parseObj(downRequest.getIoTProduct().getMetadata()));
    }
    result.put("productKey", downRequest.getProductKey());
    result.put("deviceNode", downRequest.getIoTProduct().getDeviceNode());

    // 查询是否为多通道设备
    if ("634116e3cd68426aa70b0431".equals(downRequest.getProductKey())) {
      JSONObject param = new JSONObject();
      JSONArray array = new JSONArray();
      JSONObject obj = new JSONObject();
      obj.set("deviceId", downRequest.getDeviceId());
      array.add(obj);
      param.set("deviceList", array);
      RespBody respBody = imoulifeRequest.request("/openapi/listDeviceDetailsByIds", param);
      JSONObject jsonObject = JSONUtil.parseObj(respBody);
      JSONObject object = JSONUtil.parseObj(jsonObject.get("result"));
      JSONObject data = JSONUtil.parseObj(object.getStr("data"));
      JSONArray deviceList = data.getJSONArray("deviceList");
      JSONObject device = JSONUtil.parseObj(deviceList.get(0));
      JSONArray channelList = device.getJSONArray("channelList");
      int channelNum = channelList.size();
      result.put("channelNum", channelNum);
    }

    return result;
  }

  private class EzResultParse {

    private Map<String, String> errorResult;
    private Map<String, String> successResult;

    public Map<String, String> getErrorResult() {
      return errorResult;
    }

    public void setErrorResult(Map<String, String> errorResult) {
      this.errorResult = errorResult;
    }

    public Map<String, String> getSuccessResult() {
      return successResult;
    }

    public void setSuccessResult(Map<String, String> successResult) {
      this.successResult = successResult;
    }
  }
}
