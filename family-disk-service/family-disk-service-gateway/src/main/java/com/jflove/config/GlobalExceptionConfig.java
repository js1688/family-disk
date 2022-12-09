package com.jflove.config;

import com.jflove.vo.ResponseHeadVO;
import lombok.extern.log4j.Log4j2;
import org.apache.dubbo.rpc.RpcException;
import org.springframework.core.annotation.Order;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * @author tanjun
 * @date 2022/12/8 16:30
 * @describe controller 层抛出的异常拦截
 */
@RestControllerAdvice
@Order(100)
@Log4j2
public class GlobalExceptionConfig {

    @ExceptionHandler(value = Throwable.class)
    @ResponseBody
    public Object  handle(Throwable e) {
        if(e instanceof RpcException){
            return new ResponseHeadVO<String>(false,"服务不稳定请稍后再试");
        }else if (e instanceof RuntimeException || e instanceof SecurityException) { //普通的运行异常以及认证异常
            return new ResponseHeadVO<String>(false,e.getMessage());
        }else if(e instanceof BindException){ //参数校验异常
            BindException be = ((BindException) e);
            List<String> errors = be.getAllErrors().stream().map(ObjectError::getDefaultMessage).toList();
            return new ResponseHeadVO<String>(false, String.join(",",errors));
        }else { //不认识的异常
            return new ResponseHeadVO<String>(false,e.getMessage());
        }
    }
}
