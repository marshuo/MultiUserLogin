package com.ruoyi.project.app;

import com.ruoyi.project.app.mapper.AppUserMapper;
import com.ruoyi.project.system.domain.SysUser;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author mars
 * @date 2022/08/11
 */
@Service
public class AppUserService {

    @Resource
    private AppUserMapper appUserMapper;
    
    public SysUser selectUserByUserName(String username) {
        return appUserMapper.selectUserByUserName(username);
    }
}
