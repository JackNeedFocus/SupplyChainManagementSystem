package com.jack.admin.config.security;


import com.jack.admin.config.ClassPathTldsLoader;
import com.jack.admin.filters.CaptchaCodeFilter;
import com.jack.admin.pojo.User;
import com.jack.admin.service.IRbacService;
import com.jack.admin.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootConfiguration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private SmcAuthenticationFailedHandler smcAuthenticationFailedHandler;

    @Autowired
    private SmcAuthenticationSuccessHandler smcAuthenticationSuccessHandler;

    @Autowired
    private SmcLogoutSuccessHandler smcLogoutSuccessHandler;

    @Resource
    private CaptchaCodeFilter captchaCodeFilter;


    @Resource
    private IUserService userService;

    @Resource
    private DataSource dataSource;

    @Resource
    private IRbacService rbacService;

    /**
     * ??????????????????
     * @param web
     * @throws Exception
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(
                "/images/**",
                "/css/**",
                "/js/**",
                "/lib/**",
                "/error/**");
    }

    /**
     * ???????????????????????????
     * @param http
     * @throws Exception
     */
    protected void configure(HttpSecurity http) throws Exception{
        http.csrf().disable()
                .addFilterBefore(captchaCodeFilter, UsernamePasswordAuthenticationFilter.class)
                .headers().frameOptions().disable()
                .and()
                    .formLogin()
                    .usernameParameter("userName")
                    .passwordParameter("password")
                    .loginPage("/index")
                    .loginProcessingUrl("/user/login")
                    .successHandler(smcAuthenticationSuccessHandler)
                    .failureHandler(smcAuthenticationFailedHandler)
                .and()
                    .authorizeRequests().antMatchers("/index", "/login", "/image").permitAll()
                    .anyRequest().authenticated()
                .and()
                    .rememberMe()
                    .rememberMeParameter("rememberMe")
                    //????????????????????????cookie???????????????????????????????????????remember-me
                    .rememberMeCookieName("remember-me-cookie")
                    //??????token???????????????????????????????????????????????????????????????????????????
                    .tokenValiditySeconds(7  * 24 * 60 * 60)
                    //?????????
                    .tokenRepository(persistentTokenRepository())
                .and()
                    .logout()
                    .logoutUrl("/signout")
                    .deleteCookies("JSESSIONID")
                    .logoutSuccessHandler(smcLogoutSuccessHandler);

    }


    /**
     * ???????????????????????????token
     * @return
     */
    @Bean
    public PersistentTokenRepository persistentTokenRepository(){
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource); // ?????????dataSource???application.yml?????????????????????dataSource?????????Spring IOC????????????
        return tokenRepository;
    }

    /**
     * Spring ???????????????bean?????????user???????????????????????????user??????????????????????????????
     * @return
     */
    @Bean
    public UserDetailsService userDetailsService(){
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                User userDetails = userService.findUserByUserName(username);

                //?????????????????????????????????
                List<String> roleNames = rbacService.findRolesByUserName(username);
                //?????????????????????????????????
                List<String> authorities = rbacService.findAuthoritiesByRoleName(roleNames);

                roleNames = roleNames.stream().map(role->"ROLE_"+role).collect(Collectors.toList());
                authorities.addAll(roleNames);
                System.out.println("?????????"+authorities);
                userDetails.setAuthorities(AuthorityUtils.commaSeparatedStringToAuthorityList(String.join(",",authorities)));
                return userDetails;
            }
        };
    }

    @Bean
    public PasswordEncoder encoder(){
        return new BCryptPasswordEncoder();
    }

    /**
     * ?????????????????????userDetail?????????
     * @param auth
     * @throws Exception
     */
    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService()).passwordEncoder(encoder());
    }

    /**
     * ?????? ClassPathTldsLoader
     */
    @Bean
    @ConditionalOnMissingBean(ClassPathTldsLoader.class)
    public ClassPathTldsLoader classPathTldsLoader(){
        return new ClassPathTldsLoader();
    }
}
