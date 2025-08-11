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

package cn.universal.web.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.universal.admin.iot.service.IIoTDeviceService;
import cn.universal.admin.web.BaseController;
import cn.universal.core.base.R;
import cn.universal.persistence.entity.IoTDevice;
import cn.universal.persistence.entity.IoTDeviceFenceRel;
import cn.universal.persistence.entity.IoTDeviceGeoFence;
import cn.universal.persistence.entity.bo.IoTDeviceBO;
import cn.universal.persistence.entity.vo.IoTDeviceGeoFenceVO;
import cn.universal.persistence.entity.vo.IoTDeviceVO;
import cn.universal.persistence.mapper.IoTDeviceFenceRelMapper;
import cn.universal.persistence.mapper.IoTDeviceGeoFenceMapper;
import cn.universal.persistence.mapper.IoTDeviceMapper;
import cn.universal.persistence.query.IoTDeviceQuery;
import cn.universal.persistence.query.PageBean;
import cn.universal.rule.fence.service.FenceService;
import cn.universal.web.context.IoTInnerAuthContext;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * IoT设备位置和地理围栏管理控制器
 *
 * <p>提供IoT设备的位置服务和地理围栏功能，包括： - 地理围栏的创建、修改、删除和查询 - 围栏与设备的关联管理 - 围栏触发规则配置 - 设备位置监控
 *
 * <p>支持多种围栏类型（圆形、多边形等）和时间规则配置， 用于设备的位置监控和区域管理。
 *
 * @version 1.0 @Author Aleo
 * @since 2025/1/1
 */
@RestController
@RequestMapping("iot/location")
@Slf4j
public class FenceController extends BaseController {

  /** IoT内部认证上下文 */
  @Resource private IoTInnerAuthContext ioTInnerAuthContext;

  /** IoT设备地理围栏数据访问层 */
  @Resource private IoTDeviceGeoFenceMapper ioTDeviceGeoFenceMapper;

  /** IoT设备数据访问层 */
  @Resource private IoTDeviceMapper ioTDeviceMapper;

  /** IoT设备围栏关联数据访问层 */
  @Resource private IoTDeviceFenceRelMapper ioTDeviceFenceRelMapper;

  /** IoT设备服务 */
  @Resource private IIoTDeviceService devInstanceService;

  /** 围栏服务 */
  @Resource private FenceService fenceService;

  /**
   * 查询围栏列表
   *
   * <p>根据查询条件分页获取地理围栏列表，支持按名称、类型、 触发方式等条件筛选
   *
   * @param downRequest 加密的请求数据
   * @param request HTTP请求对象
   * @return 围栏列表分页结果
   */
  @PostMapping(value = "/selectFence")
  public R selectFence(@RequestBody String downRequest, HttpServletRequest request) {
    JSONObject jsonObject = ioTInnerAuthContext.checkAndDecryptMsg(downRequest, request);
    IoTDeviceGeoFence ioTDeviceGeoFence = new IoTDeviceGeoFence();
    int page = 0;
    int size = 0;
    if (jsonObject.containsKey("page") && jsonObject.containsKey("size")) {
      page = jsonObject.getInt("page");
      size = jsonObject.getInt("size");
    }
    ioTDeviceGeoFence.setName(jsonObject.getStr("name"));
    ioTDeviceGeoFence.setTouchWay(jsonObject.getStr("touchWay"));
    ioTDeviceGeoFence.setType(jsonObject.getStr("type"));
    ioTDeviceGeoFence.setWeekTime(jsonObject.getStr("weekTime"));
    ioTDeviceGeoFence.setCreatorUser(jsonObject.getStr("creatorUser"));
    ioTDeviceGeoFence.setCreatorId(jsonObject.getStr("creatorUser"));
    if (jsonObject.containsKey("queryUserList")) {
      ioTDeviceGeoFence.setQueryUserList(
          jsonObject.getJSONArray("queryUserList").stream()
              .map(Object::toString)
              .collect(Collectors.toList()));
    }
    Page<IoTDeviceGeoFence> p = PageHelper.startPage(page, size);
    List<IoTDeviceGeoFenceVO> devInstanceVOList =
        ioTDeviceGeoFenceMapper.selectList(ioTDeviceGeoFence);
    PageBean pageBean =
        new PageBean(devInstanceVOList, new PageInfo(devInstanceVOList).getTotal(), size, page);
    return R.ok(pageBean);
  }

  /**
   * 创建地理围栏
   *
   * <p>创建新的地理围栏，支持圆形和多边形围栏类型， 可配置触发条件、时间规则等
   *
   * @param downRequest 加密的请求数据
   * @param request HTTP请求对象
   * @return 创建的围栏ID
   */
  @PostMapping("/setFence")
  public R setFence(@RequestBody String downRequest, HttpServletRequest request) {
    JSONObject jsonObject = ioTInnerAuthContext.checkAndDecryptMsg(downRequest, request);
    IoTDeviceGeoFence ioTDeviceGeoFence = new IoTDeviceGeoFence();
    BigDecimal redius = null;
    if (StrUtil.isNotEmpty(jsonObject.getStr("radius"))) {
      redius = new BigDecimal(jsonObject.getStr("radius"));
    }
    ioTDeviceGeoFence.setName(jsonObject.getStr("name"));
    ioTDeviceGeoFence.setStatus(Integer.parseInt(jsonObject.getStr("status")));
    ioTDeviceGeoFence.setTouchWay(jsonObject.getStr("touchWay"));
    ioTDeviceGeoFence.setFence(jsonObject.getStr("fence"));
    ioTDeviceGeoFence.setType(jsonObject.getStr("type"));
    ioTDeviceGeoFence.setPoint(jsonObject.getStr("point"));
    ioTDeviceGeoFence.setRadius(redius);
    ioTDeviceGeoFence.setWeekTime(jsonObject.getStr("weekTime"));
    ioTDeviceGeoFence.setBeginTime(jsonObject.getStr("beginTime"));
    ioTDeviceGeoFence.setEndTime(jsonObject.getStr("endTime"));
    ioTDeviceGeoFence.setCreatorUser(jsonObject.getStr("creatorUser"));
    ioTDeviceGeoFence.setCreatorId(jsonObject.getStr("creatorUser"));
    ioTDeviceGeoFence.setNoTriggerTime(jsonObject.getStr("noTriggerTime"));
    ioTDeviceGeoFence.setDelayTime(jsonObject.getInt("delayTime"));
    ioTDeviceGeoFence.setCreateDate(new Date());
    ioTDeviceGeoFence.setUpdateDate(new Date());
    ioTDeviceGeoFenceMapper.insert(ioTDeviceGeoFence);
    return R.ok(ioTDeviceGeoFence.getId());
  }

  /**
   * 修改地理围栏
   *
   * <p>更新现有地理围栏的配置信息，包括围栏形状、触发条件等
   *
   * @param downRequest 加密的请求数据
   * @param request HTTP请求对象
   * @return 更新结果
   */
  @PostMapping("/updateFence")
  public R updateFence(@RequestBody String downRequest, HttpServletRequest request) {
    JSONObject jsonObject = ioTInnerAuthContext.checkAndDecryptMsg(downRequest, request);
    IoTDeviceGeoFence ioTDeviceGeoFence = new IoTDeviceGeoFence();
    BigDecimal redius = null;
    Integer status = null;
    if (StrUtil.isNotEmpty(jsonObject.getStr("radius"))) {
      redius = new BigDecimal(jsonObject.getStr("radius"));
    }
    if (StrUtil.isNotEmpty(jsonObject.getStr("status"))) {
      status = Integer.parseInt(jsonObject.getStr("status"));
    }
    ioTDeviceGeoFence.setId(jsonObject.getLong("id"));
    ioTDeviceGeoFence.setName(jsonObject.getStr("name"));
    ioTDeviceGeoFence.setStatus(status);
    ioTDeviceGeoFence.setTouchWay(jsonObject.getStr("touchWay"));
    ioTDeviceGeoFence.setFence(jsonObject.getStr("fence"));
    ioTDeviceGeoFence.setType(jsonObject.getStr("type"));
    ioTDeviceGeoFence.setPoint(jsonObject.getStr("point"));
    ioTDeviceGeoFence.setRadius(redius);
    ioTDeviceGeoFence.setWeekTime(jsonObject.getStr("weekTime"));
    ioTDeviceGeoFence.setBeginTime(jsonObject.getStr("beginTime"));
    ioTDeviceGeoFence.setEndTime(jsonObject.getStr("endTime"));
    ioTDeviceGeoFence.setCreatorUser(jsonObject.getStr("creatorUser"));
    ioTDeviceGeoFence.setCreatorId(jsonObject.getStr("creatorUser"));
    ioTDeviceGeoFence.setNoTriggerTime(jsonObject.getStr("noTriggerTime"));
    ioTDeviceGeoFence.setDelayTime(jsonObject.getInt("delayTime"));
    ioTDeviceGeoFence.setUpdateDate(new Date());
    Integer result = ioTDeviceGeoFenceMapper.updateFence(ioTDeviceGeoFence);
    return R.ok(result);
  }

  /**
   * 删除地理围栏
   *
   * <p>删除指定的地理围栏及其与设备的关联关系
   *
   * @param id 围栏ID
   * @return 删除结果
   */
  @RequestMapping(value = "/{id}")
  public R delFence(@PathVariable("id") String id) {
    Integer result = ioTDeviceGeoFenceMapper.deleteByIds(id);
    IoTDeviceFenceRel ioTDeviceFenceRel = new IoTDeviceFenceRel();
    ioTDeviceFenceRel.setFenceId(Long.parseLong(id));
    ioTDeviceFenceRelMapper.delete(ioTDeviceFenceRel);
    return R.ok(result);
  }

  /**
   * 根据围栏ID查询围栏详情
   *
   * <p>获取指定围栏的完整配置信息
   *
   * @param downRequest 加密的请求数据
   * @param request HTTP请求对象
   * @return 围栏详细信息
   */
  @PostMapping("/selectFenceById")
  public R selectFenceById(@RequestBody String downRequest, HttpServletRequest request) {
    JSONObject jsonObject = ioTInnerAuthContext.checkAndDecryptMsg(downRequest, request);
    Long id = jsonObject.getLong("id");
    IoTDeviceGeoFence ioTDeviceGeoFence = ioTDeviceGeoFenceMapper.selectByPrimaryKey(id);
    return R.ok(ioTDeviceGeoFence);
  }

  /** 复制围栏 */
  @PostMapping("/fence/copy")
  public R copyFence(@RequestBody String downRequest, HttpServletRequest request) {

    JSONObject jsonObject = ioTInnerAuthContext.checkAndDecryptMsg(downRequest, request);
    Long id = jsonObject.getLong("id");
    IoTDeviceGeoFence ioTDeviceGeoFence = ioTDeviceGeoFenceMapper.selectByPrimaryKey(id);
    ioTDeviceGeoFence.setId(null);
    ioTDeviceGeoFence.setCreateDate(new Date());
    ioTDeviceGeoFence.setUpdateDate(new Date());
    ioTDeviceGeoFence.setName("复制-" + ioTDeviceGeoFence.getName());
    ioTDeviceGeoFenceMapper.insert(ioTDeviceGeoFence);
    return R.ok(ioTDeviceGeoFence);
  }

  /** 查询绑定围栏设备列表 */
  @PostMapping("/selectFenceDevice")
  public R selectFenceDevice(@RequestBody String downRequest, HttpServletRequest request) {
    JSONObject jsonObject = ioTInnerAuthContext.checkAndDecryptMsg(downRequest, request);
    IoTDeviceBO ioTDeviceBO = new IoTDeviceBO();
    int page = 0;
    int size = 0;
    if (jsonObject.containsKey("page") && jsonObject.containsKey("size")) {
      page = jsonObject.getInt("page");
      size = jsonObject.getInt("size");
    }
    ioTDeviceBO.setFenceId(jsonObject.getStr("id"));
    if (StrUtil.isNotEmpty(jsonObject.getStr("deviceName"))) {
      ioTDeviceBO.setDeviceName(jsonObject.getStr("deviceName"));
    }
    if (StrUtil.isNotEmpty(jsonObject.getStr("online"))) {
      ioTDeviceBO.setOnline(jsonObject.getStr("online"));
    }
    Page<IoTDeviceVO> ioTDeviceVOList =
        devInstanceService.selectFenceDevice(ioTDeviceBO, page, size);
    PageBean pageBean =
        new PageBean(ioTDeviceVOList, new PageInfo(ioTDeviceVOList).getTotal(), size, page);
    return R.ok(pageBean);
  }

  /** 根据设备iotId与创建者查询围栏 */
  @PostMapping("/selectFenceByIotId")
  public R selectFenceByIotId(@RequestBody String downRequest, HttpServletRequest request) {
    JSONObject jsonObject = ioTInnerAuthContext.checkAndDecryptMsg(downRequest, request);
    String iotId = jsonObject.getStr("iotId");
    List<IoTDeviceGeoFenceVO> ioTDeviceGeoFenceVOS =
        ioTDeviceFenceRelMapper.selectFenceByIotId(iotId);
    PageBean pageBean =
        new PageBean(ioTDeviceGeoFenceVOS, new PageInfo(ioTDeviceGeoFenceVOS).getTotal(), 999, 1);
    return R.ok(pageBean);
  }

  /** 绑定设备 */
  @PostMapping("/addFenceDevice")
  public R addFenceDevice(@RequestBody String downRequest, HttpServletRequest request) {
    JSONObject jsonObject = ioTInnerAuthContext.checkAndDecryptMsg(downRequest, request);
    IoTDeviceFenceRel ioTDeviceFenceRel = new IoTDeviceFenceRel();
    Long fenceId = jsonObject.getLong("fenceId");
    ioTDeviceFenceRel.setFenceId(fenceId);
    ioTDeviceFenceRel.setCreateDate(new Date());
    String[] ids = jsonObject.getStr("deviceIds").split(",");
    for (String id : ids) {
      IoTDevice ioTDevice =
          ioTDeviceMapper.getOneByDeviceId(IoTDeviceQuery.builder().deviceId(id).build());
      ioTDeviceFenceRel.setIotId(ioTDevice.getIotId());
      ioTDeviceFenceRel.setDeviceId(id);
      ioTDeviceFenceRel.setCreatorId(ioTDevice.getCreatorId());
      ioTDeviceFenceRelMapper.insert(ioTDeviceFenceRel);
    }
    return R.ok();
  }

  /** 解绑设备 */
  @PostMapping("/delFenceDevice")
  public R delFenceDevice(@RequestBody String downRequest, HttpServletRequest request) {
    JSONObject jsonObject = ioTInnerAuthContext.checkAndDecryptMsg(downRequest, request);
    Long fenceId = jsonObject.getLong("fenceId");
    String[] ids = jsonObject.getStr("deviceIds").split(",");
    for (String id : ids) {
      ioTDeviceFenceRelMapper.deleteDeviceIdAndFenceId(id, fenceId);
    }
    return R.ok();
  }
}
