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

package cn.universal.persistence.entity.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 规则模型查询 @Author Aleo
 *
 * @since 2023/1/13 15:06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema
public class RuleModelBO {

  @Schema(description = "id")
  private Long id;

  @Schema(description = "规则名称")
  private String ruleName;

  @Schema(description = "状态 run.运行中 stop.已停止")
  private String status;

  private String productKey;
  private String creatorId;
  private String iotId;
  private List<String> groupIds;
}
