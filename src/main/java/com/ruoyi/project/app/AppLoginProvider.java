package com.ruoyi.project.app;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;

/**
 * @author mars
 * @date 2022/08/09
 */
@Slf4j
public class AppLoginProvider implements AuthenticationProvider {
    
    private PasswordEncoder passwordEncoder;
    
    private volatile String userNotFoundEncodedPassword;
    
    private UserDetailsService userDetailsService;
    
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!supports(authentication.getClass())) {
            return null;
        }
        log.info("AppLoginAuthenticationToken authentication request: %s", authentication);
        AppLoginAuthenticationToken token = (AppLoginAuthenticationToken) authentication;
        
        // 从数据库查询 数据
        UserDetails userDetails = userDetailsService.loadUserByUsername((String) token.getPrincipal());
        
        System.out.println(token.getPrincipal());
        if (userDetails == null) {
            throw new InternalAuthenticationServiceException("无法获取用户信息");
        }
        System.out.println(userDetails.getAuthorities());
    
        String presentedPassword = token.getCredentials().toString();
        if (!this.passwordEncoder.matches(presentedPassword, userDetails.getPassword())) {
            throw new BadCredentialsException("密码不匹配");
        }
        
        AppLoginAuthenticationToken result =
                new AppLoginAuthenticationToken(userDetails, token.getCredentials(), userDetails.getAuthorities());
        /**
         Details 中包含了 ip地址、 sessionId 等等属性
         其实还可以存储一些我们想要存储的数据，之后我们再利用。、
         */
        result.setDetails(token.getDetails());
        
        return result;
    }
    
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        Assert.notNull(passwordEncoder, "passwordEncoder cannot be null");
        this.passwordEncoder = passwordEncoder;
        this.userNotFoundEncodedPassword = null;
    }
    
    public PasswordEncoder getPasswordEncoder() {
        return this.passwordEncoder;
    }
    
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }
    
    public UserDetailsService getUserDetailsService() {
        return this.userDetailsService;
    }
    
    @Override
    public boolean supports(Class<?> authentication) {
        // 设置当前provider需要支持验证的token类型
        return AppLoginAuthenticationToken.class.isAssignableFrom(authentication);
    }
}