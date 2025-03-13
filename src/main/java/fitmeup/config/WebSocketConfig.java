package fitmeup.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration  // 이 클래스가 Spring의 설정 클래스임을 나타냄
@EnableWebSocketMessageBroker  // WebSocket 메시지 브로커를 활성화하는 어노테이션
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  // STOMP 엔드포인트를 등록하는 메서드
  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // "/ws" 경로에서 WebSocket 연결을 시작할 수 있도록 설정
    registry.addEndpoint("/ws")
        // 모든 출처(origin)를 허용
        .setAllowedOriginPatterns("*")
        // SockJS를 사용하여 폴백(fallback) 옵션을 활성화
        .withSockJS();
  }

  // 메시지 브로커 설정을 담당하는 메서드
  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    // "/topic"과 "/queue"로 시작하는 경로에 대해 메시지 브로커를 활성화
    registry.enableSimpleBroker("/topic", "/queue");
    // "/app"로 시작하는 경로에서 메시지를 처리하는 애플리케이션 대상(prefix) 설정
    registry.setApplicationDestinationPrefixes("/app");
  }
}
