package com.jflove.gateway.config;

import com.jflove.gateway.tool.JJwtTool;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * @author tanjun
 * @date 2022/12/16 9:16
 * @describe
 */
@EnableWebSocketMessageBroker
@Configuration
@Log4j2
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Autowired
    private IgnoreUrlsConfig ignoreUrlsConfig;
    @Autowired
    private JJwtTool jJwtTool;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry) {
        // 注册一个STOMP的endpoint,并指定使用SockJS协议
        stompEndpointRegistry.addEndpoint("/gateway/stomp")
                .setAllowedOriginPatterns(ignoreUrlsConfig.getOriginsStrings())
                .addInterceptors(new HandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
                        String token = ((ServletServerHttpRequest) request).getServletRequest().getParameter(HttpConstantConfig.AUTHORIZATION);
                        log.info("websocket authenticated token:{}", token);
                        if(!StringUtils.hasLength(token)){//没有token,无法验证身份
                            return false;
                        }
                        try {
                            Jws<Claims> jws = jJwtTool.parseJwt(token);
                        }catch (SecurityException e){
                            return false;//token 验证异常
                        }
                        return true;//身份验证成功,允许连接
                    }

                    @Override
                    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

                    }
                })
                .withSockJS();
        log.info("成功启动stomp websocket服务");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 客户端订阅消息的请求前缀，topic广播给所有用户,user 与用户点对点,space 与这个空间相关
        registry.enableSimpleBroker("/topic", "/user", "/space");
        // 客户端发送消息的请求前缀
        registry.setApplicationDestinationPrefixes("/app");
        // 服务端通知客户端的前缀，可以不设置，默认为user
        registry.setUserDestinationPrefix("/user");
    }
}
