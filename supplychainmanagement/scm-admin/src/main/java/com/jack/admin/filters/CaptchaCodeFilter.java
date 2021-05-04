package com.jack.admin.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jack.admin.model.CaptchaImageModel;
import com.jack.admin.model.RespBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Objects;

@Component
public class CaptchaCodeFilter extends OncePerRequestFilter {

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        // 只有在登录的时候才过滤
        if(StringUtils.equals("/user/login", httpServletRequest.getRequestURI()) && StringUtils.equalsIgnoreCase(httpServletRequest.getMethod(),"post")){
            // 调用校验方法
            try {
                this.validate(new ServletWebRequest(httpServletRequest));
            } catch (AuthenticationException e) {
                httpServletResponse.setContentType("application/json;charset=UTF-8");
                httpServletResponse.getWriter().write(objectMapper.writeValueAsString(
                        RespBean.error("验证码错误")
                ));
                return;
            }
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    private void validate(ServletWebRequest servletWebRequest) throws ServletRequestBindingException {
        HttpSession session = servletWebRequest.getRequest().getSession();

        String codeInRequest = ServletRequestUtils.getStringParameter(servletWebRequest.getRequest(),"captchaCode");

        if(StringUtils.isEmpty(codeInRequest)){
            throw new SessionAuthenticationException("验证码不能为空");
        }

        CaptchaImageModel codeInSession = (CaptchaImageModel) session.getAttribute("captcha_key");

        if(Objects.isNull(codeInSession)){
            throw new SessionAuthenticationException("验证码不存在");
        }
        if(codeInSession.isExpired()){
            throw new SessionAuthenticationException("验证码已过期");
        }
        if(!StringUtils.equals(codeInSession.getCode(), codeInRequest)) {
            throw new SessionAuthenticationException("验证码不匹配");
        }

    }
}