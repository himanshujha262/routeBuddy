package com.routbuddy.locations.security;

import com.routbuddy.auth.security.CustomUserDetailsService;
import com.routbuddy.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = getHeader(accessor, "Authorization");
            if (authHeader == null || authHeader.isBlank()) {
                authHeader = getHeader(accessor, "Auth-Token");
            }

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    if (jwtTokenProvider.validateToken(token)) {
                        UUID userId = jwtTokenProvider.getUserIdFromToken(token);
                        UserDetails userDetails = userDetailsService.loadUserById(userId);

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                        accessor.setUser(authentication);
                        log.debug("WebSocket authenticated for user: {}", userId);
                    } else {
                        log.warn("Invalid JWT token provided in WebSocket CONNECT headers");
                    }
                } catch (Exception e) {
                    log.error("Failed to authenticate WebSocket connection: {}", e.getMessage());
                }
            }
        }

        return message;
    }

    private String getHeader(StompHeaderAccessor accessor, String headerName) {
        List<String> values = accessor.getNativeHeader(headerName);
        if (values != null && !values.isEmpty()) {
            return values.get(0);
        }
        return null;
    }
}
