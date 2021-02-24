package com.dsy.sunshine.web.exception;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Mr.Yangxiufeng
 * @date 2021-02-24
 * @time 10:38
 */
@Component
@Slf4j
public class GlobalHandlerExceptionResolver extends AbstractHandlerExceptionResolver {

    public GlobalHandlerExceptionResolver() {
        setOrder(Ordered.HIGHEST_PRECEDENCE);
    }

    @Override
    protected ModelAndView doResolveException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler, Exception ex) {
        log.error("统一异常处理");
        if (ex instanceof MethodArgumentNotValidException){
            return handleMethodArgumentNotValidException((MethodArgumentNotValidException)ex, httpServletRequest,httpServletResponse, handler);
        }else if (ex instanceof BindException){
            return handleBindException((BindException)ex, httpServletRequest,httpServletResponse, handler);
        }else if (ex instanceof ConstraintViolationException){
            return handleConstraintViolationException((ConstraintViolationException)ex, httpServletRequest,httpServletResponse, handler);
        }else if (ex instanceof AppException){
            return handleAppException((AppException) ex, httpServletRequest,httpServletResponse, handler);
        }
        return null;
    }

    private ModelAndView getModelAndView(HttpServletResponse response, String path, Map<String, Object> error) {
        ErrorResponse representation = new ErrorResponse(new RequestValidationException(error), path);
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        try {
            response.getWriter().write(JSON.toJSONString(representation));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ModelAndView();
    }

    private ModelAndView handleBindException(BindException ex, HttpServletRequest request,
                                             HttpServletResponse response, @Nullable Object handler) {
        String path = request.getRequestURI();
        Map<String, Object> error = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, fieldError -> {
                    String message = fieldError.getDefaultMessage();
                    return StringUtils.isEmpty(message) ? "无错误提示" : message;
                }));
        log.error("Validation error for [path:{}] [{}]:{}", path,ex.getBindingResult(), error);
        return getModelAndView(response, path, error);
    }

    public ModelAndView handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request,
                                                              HttpServletResponse response, @Nullable Object handler) {
        String path = request.getRequestURI();
        Map<String, Object> error = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, fieldError -> {
                    String message = fieldError.getDefaultMessage();
                    return StringUtils.isEmpty(message) ? "无错误提示" : message;
                }));

        log.error("Validation error for [{}]:{}", ex.getParameter().getParameterType().getName(), error);
        return getModelAndView(response, path, error);
    }
    public ModelAndView handleAppException(AppException ex, HttpServletRequest request,
                                            HttpServletResponse response, @Nullable Object handler) {
        log.error("App error:", ex);
        ErrorResponse representation = new ErrorResponse(ex, request.getRequestURI());
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        try {
            response.getWriter().write(JSON.toJSONString(representation));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ModelAndView();
    }
    public ModelAndView handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request,
                                                                HttpServletResponse response, @Nullable Object handler) {
        String path = request.getRequestURI();
        log.error("ConstraintViolationException Error occurred while access[{}]:", path, ex);
        Map<String , Object> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(action ->{
            errors.put("error",action.getMessageTemplate());
        });
        return getModelAndView(response, path, errors);
    }

}
