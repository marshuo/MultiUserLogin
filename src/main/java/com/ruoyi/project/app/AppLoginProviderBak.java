package com.ruoyi.project.app;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/**
 * @author mars
 * @date 2022/08/09
 */
@Slf4j
@Component
public class AppLoginProviderBak implements AuthenticationProvider {
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        log.info("自定义验证");
        String username = (String) authentication.getPrincipal();
        String password = (String) authentication.getCredentials();
        
        if ( "tom".equals(username) && "123".equals(password)) {
            
            AppLoginAuthenticationToken loginAuthenticationToken = new AppLoginAuthenticationToken(username, password);
            loginAuthenticationToken.setDetails(authentication.getDetails());
            return loginAuthenticationToken;
        }
        
        return null;
    }
    
    @Override
    public boolean supports(Class<?> aClass) {
        return AppLoginAuthenticationToken.class.isAssignableFrom(aClass);
    }
}

