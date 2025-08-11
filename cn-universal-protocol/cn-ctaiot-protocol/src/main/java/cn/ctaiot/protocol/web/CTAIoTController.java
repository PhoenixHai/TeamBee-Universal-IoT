package cn.ctaiot.protocol.web;

import cn.universal.core.base.R;
import cn.universal.core.service.IUP;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ct/aiot")
@Slf4j
public class CTAIoTController {

  @Resource(name = "ctaIoTUPService")
  private IUP ctaIoTUPService;

  @PostMapping(value = "/msg")
  public R iotCdUp(@RequestBody String msg) {
    log.info("[CT-AIoT上行][AEP消息] 收到AEP的消息: {}", msg);
    // 接收消息
    ctaIoTUPService.asyncUP(msg);
    return R.ok();
  }
}
