package com.ruoyi.project.app;

import com.ruoyi.common.constant.Constants;
import com.ruoyi.framework.security.LoginBody;
import com.ruoyi.framework.web.domain.AjaxResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author mars
 * @date 2022/08/10
 */
@Slf4j
@RestController
public class AppLoginController {
    @Resource AppLoginService loginService;
    
    @PostMapping("/appLogin")
    public AjaxResult appLogin(@RequestBody LoginBody loginBody) {
        log.info("appLogin");
        AjaxResult ajax = AjaxResult.success();
        // 生成令牌
        String token = loginService.login(loginBody.getUsername(), loginBody.getPassword(), loginBody.getCode(),
                loginBody.getUuid());
        ajax.put(Constants.TOKEN, token);
        return ajax;
    }
    
}
