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

package cn.universal.persistence.mapper;

import cn.universal.persistence.common.BaseMapper;
import cn.universal.persistence.entity.IoTDeviceLog;
import cn.universal.persistence.entity.bo.IoTDeviceLogBO;
import cn.universal.persistence.entity.vo.IoTDeviceLogVO;
import cn.universal.persistence.interceptor.TableShard;
import cn.universal.persistence.interceptor.TableShardStrategyByIotId;
import cn.universal.persistence.query.LogQuery;
import java.util.List;
import org.apache.ibatis.annotations.Param;

@TableShard(
    tableNamePrefix = "iot_device_log",
    value = "iotId",
    fieldFlag = true,
    shardStrategy = TableShardStrategyByIotId.class)
public interface IoTDeviceLogMapper extends BaseMapper<IoTDeviceLog> {

  /** 分页查询设备日志列表 */
  List<IoTDeviceLogVO> queryLogPageList(IoTDeviceLogBO bo);

  /** 分页查询设备日志列表 */
  List<IoTDeviceLogVO> queryLogPageV2List(LogQuery logQuery);

  /** 根据主键查询 */
  IoTDeviceLogVO queryLogById(LogQuery logQuery);

  /**
   * 获取设备事件的统计信息
   *
   * @param event 事件id
   * @param iotId 设备iotId
   */
  List<String> queryEventTotalByEventAndId(
      @Param("event") String event, @Param("iotId") String iotId);

  /** 查询最新包含坐标的第二条日志 */
  IoTDeviceLog queryCoordinatesLogByIotId(@Param("iotId") String iotId);

  /** 查询日志ID - 此方法不使用分表 */
  @TableShard(
      tableNamePrefix = "iot_device_log",
      value = "",
      fieldFlag = false,
      shardStrategy = TableShardStrategyByIotId.class)
  Long queryLogIdByTime(@Param("tablesIndex") int tablesIndex, @Param("time") Long time);

  @TableShard(
      tableNamePrefix = "iot_device_log",
      value = "",
      fieldFlag = false,
      shardStrategy = TableShardStrategyByIotId.class)
  int deleteLogById(@Param("tablesIndex") int tablesIndex, @Param("id") Long id);

  @TableShard(
      tableNamePrefix = "iot_device_log",
      value = "",
      fieldFlag = false,
      shardStrategy = TableShardStrategyByIotId.class)
  int deleteLogByTask(
      @Param("tablesIndex") int tablesIndex,
      @Param("productKey") String productKey,
      @Param("id") Long id);

  @TableShard(
      tableNamePrefix = "iot_device_log",
      value = "",
      fieldFlag = false,
      shardStrategy = TableShardStrategyByIotId.class)
  Long queryLogIdByProductKeyAndTime(
      @Param("tablesIndex") int tablesIndex,
      @Param("productKey") String productKey,
      @Param("time") Long time);
}
