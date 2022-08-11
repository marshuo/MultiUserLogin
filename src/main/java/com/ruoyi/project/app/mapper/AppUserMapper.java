package com.ruoyi.project.app.mapper;

import com.ruoyi.project.system.domain.SysUser;

/**
 * @author mars
 * @date 2022/08/11
 */
public interface AppUserMapper {
    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户对象信息
     */
    SysUser selectUserByUserName(String userName);
    
}
