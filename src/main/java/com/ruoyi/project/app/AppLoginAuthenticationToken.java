package com.ruoyi.project.app;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

/**
 * @author mars
 * @date 2022/08/09
 */

public class AppLoginAuthenticationToken extends UsernamePasswordAuthenticationToken {
    private static final long serialVersionUID = 1761684474334568306L;
    
    public AppLoginAuthenticationToken(Object principal, Object credentials) {
        super(principal, credentials);
    }
}

