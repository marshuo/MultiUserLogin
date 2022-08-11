package com.ruoyi.project.app;

import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

/**
 * @author mars
 * @date 2022/08/09
 */
public class AppLoginProvider extends DaoAuthenticationProvider {
    
    @Override
    public boolean supports(Class<?> authentication) {
        // 设置当前provider需要支持验证的token类型
        return AppLoginAuthenticationToken.class.isAssignableFrom(authentication);
    }
}