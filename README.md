# Spring Security实现多种用户认证

### 先说明一下需求：

　　系统里面有两种用户，对应数据库里面的两张表。需要提供两个用户登录接口，对两种用户进行登录认证。

　　网上找了很多资料，折腾了好几天，走了不少弯路，终于把问题搞定了。

　　

　　系统是基于若依框架（RuoYi-Vue-fast-3.8.1）进行开发，其中Spring Security的版本是5.5.4。项目地址：

　　

### 简单说明一下Spring Security用户名+密码方式登录认证的流程：

1. Controller的登录接口，接收用户名和密码。

2. 根据用户名和密码构造一个UsernamePasswordAuthenticationToken，传递给authenticationManager，开启认证流程
   
   ```java
               authentication = authenticationManager
                       .authenticate(new UsernamePasswordAuthenticationToken(username, password));
   ```

3. authenticationManager实际ProviderManager，他会对所有的Provider进行遍历，每个Provider的support方法判断是否匹配当前token类型。如果匹配，就调用authenticate方法对token进行认证。
   
   ```java
       public boolean supports(Class<?> authentication) {
           return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
       }
   ```

4. UsernamePasswordAuthenticationToken对应的是DaoAuthenticationProvider，会调用UserDetailsService的loadUserByUsername方法，去获取用户信息。
   
   ```java
   UserDetails loadedUser = this.getUserDetailsService().loadUserByUsername(username);
   ```
   
    我们可以自定义一个类AppUserDetailService来实现UserDetailsService接口，并且注入到我们自定义provider中，这样就可以加入一个认证流程了。

5. 继续走认证逻辑，包括判断密码正确，用户是否过期，等等；如果认证失败，会抛出对应的异常。如果认证成功，返回
   
    authentication到controller。

6. 可以根据authentication生成token，返回给前端。

　　

### 自定义token-AppLoginAuthenticationToken

```java
public class AppLoginAuthenticationToken extends UsernamePasswordAuthenticationToken {
    private static final long serialVersionUID = 1761684474334568306L;

    public AppLoginAuthenticationToken(Object principal, Object credentials) {
        super(principal, credentials);
    }
}
```

### 自定义provider-AppLoginProvider

```java
public class AppLoginProvider extends DaoAuthenticationProvider {

    @Override
    public boolean supports(Class<?> authentication) {
        // 设置当前provider需要支持验证的token类型
        return AppLoginAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
```

### 自定义用户认证-AppUserDetailService

```java
@Slf4j
@Component
public class AppUserDetailService implements UserDetailsService {
    @Resource
    private AppUserService appUserService;

    @Override
    public UserDetails loadUserByUsername(String username)
    {
        SysUser user = appUserService.selectUserByUserName(username);
        if (StringUtils.isNull(user))
        {
            log.info("登录用户：{} 不存在.", username);
            throw new ServiceException("登录用户：" + username + " 不存在");
        }
        else if (UserStatus.DELETED.getCode().equals(user.getDelFlag()))
        {
            log.info("登录用户：{} 已被删除.", username);
            throw new ServiceException("对不起，您的账号：" + username + " 已被删除");
        }
        else if (UserStatus.DISABLE.getCode().equals(user.getStatus()))
        {
            log.info("登录用户：{} 已被停用.", username);
            throw new ServiceException("对不起，您的账号：" + username + " 已停用");
        }

        return createLoginUser(user);
    }

    public UserDetails createLoginUser(SysUser user)
    {
        // return new LoginUser(user.getUserId(), user.getDeptId(), user, permissionService.getMenuPermission(user));
        return new LoginUser(user.getUserId(), user.getDeptId(), user, null);
    }
}
```

### SecurityConfig配置修改

```java
    /**
     * 自定义用户认证逻辑
     */
    @Autowired
    @Qualifier("userDetailsServiceImpl")
    private UserDetailsService userDetailsService;

    /**
     * app用户认证逻辑
     */
    @Resource
    @Qualifier("appUserDetailService")
    private UserDetailsService appUserDetailService;

// ..............


    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // CSRF禁用，因为不使用session
                .csrf().disable()
                // 认证失败处理类
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                // 基于token，所以不需要session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                // 过滤请求
                .authorizeRequests()
                // 对于登录login 注册register 验证码captchaImage 允许匿名访问
                .antMatchers("/login", "/register", "/captchaImage").anonymous()

                // app登录页面 允许匿名访问
                .antMatchers("/appLogin").anonymous()

//..................................

}

//.......................

    /**
     * 身份认证接口
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception
    {
        // 添加app登录身份认证的provider
        auth.authenticationProvider(getAppLoginProvider());
        // 默认的身份认证
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder());
    }

    @Bean
    public AppLoginProvider getAppLoginProvider() {
        AppLoginProvider provider = new AppLoginProvider();
        provider.setPasswordEncoder(bCryptPasswordEncoder());
        // 注入自定义认证的UserDetailService
        provider.setUserDetailsService(appUserDetailService);
        return provider;
    }
```

### 提供登录接口-AppLoginController

```java
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
```

### service-发起身份认证

```java
        // 用户验证
        Authentication authentication = null;
        try
        {
            // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
            authentication = authenticationManager
                    .authenticate(new AppLoginAuthenticationToken(username, password));
        }
        catch (Exception e)
        {
            if (e instanceof BadCredentialsException)
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
                throw new UserPasswordNotMatchException();
            }
            else
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, e.getMessage()));
                throw new ServiceException(e.getMessage());
            }
        }
```

### 项目安装说明，参照若依说明文档

/login接口：

用户/密码： admin/admin123

/appLogin接口：

用户/密码：appUser/admin123
