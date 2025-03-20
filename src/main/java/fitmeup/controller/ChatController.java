package fitmeup.controller;

import fitmeup.dto.ChatMessage;
import fitmeup.dto.LoginUserDetails;
import fitmeup.dto.UploadResultDTO;
import fitmeup.dto.UserDTO;
import fitmeup.service.ChatService;
import fitmeup.service.FileService;
import fitmeup.service.TrainerApplicationService;
import fitmeup.service.UserService;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * 로그인한 사용자와 선택한 상대방의 userId를 기준으로 대화 내역을 조회하고,
 * 실시간 메시지 전송(STOMP) 및 파일 업로드 기능을 제공하는 채팅 컨트롤러입니다.
 */
@Controller
@RequiredArgsConstructor
public class ChatController {

  private final ChatService chatService;
  private final SimpMessagingTemplate messageTemplate;
  private final FileService fileService;
  private final UserService userService;


  /**
   * applicationId를 받아 로그인한 사용자의 롤에 따라 currentUser와 targetUser를 결정한 후,
   * 두 사용자 간의 대화 내역을 조회하여 채팅 프래그먼트를 반환합니다.
   */
  @GetMapping("/chat")
  public String getConversation(@RequestParam("applicationId") Long applicationId, Model model) {
    // 로그인한 사용자의 정보를 SecurityContext에서 가져옴
    LoginUserDetails loginUser = (LoginUserDetails) SecurityContextHolder.getContext()
        .getAuthentication().getPrincipal();
    Long loginUserId = loginUser.getUserId();
    UserDTO currentUser;
    UserDTO targetUser;

    // 롤에 따라 currentUser와 targetUser 결정
    if (loginUser.getRoles().equals("Trainer")) {
      // 트레이너일 경우: 현재 사용자는 로그인한 트레이너, 대상 회원은 신청서의 user
      currentUser = userService.getUserById(loginUserId);
      targetUser = chatService.getUserDTOByApplicationId(applicationId);
    } else {
      // 유저일 경우: 현재 사용자는 로그인한 유저, 대상 회원은 신청서의 trainer의 user 정보
      currentUser = userService.getUserById(loginUserId);
      targetUser = chatService.getTrainerUserDTOByApplicationId(applicationId);
    }

    // targetUser가 null이면 오류 발생
    if (targetUser == null) {
      throw new RuntimeException("targetUser is null for applicationId: " + applicationId);
    }

    // 대화 내역 조회: 대상 회원의 userId와 현재 사용자의 userId를 기준으로 조회
    List<ChatMessage> conversation = chatService.getConversation(targetUser.getUserId(), currentUser.getUserId());
    model.addAttribute("conversation", conversation);
    model.addAttribute("currentUser", currentUser);
    model.addAttribute("targetUser", targetUser);
    return "fragment/chat :: chatFragment";
  }

  /**
   * STOMP를 통해 전송된 채팅 메시지를 처리합니다.
   * 로그인한 사용자의 id를 발신자로 설정한 후 메시지를 DB에 저장하고,
   * 저장된 메시지를 수신자와 발신자 모두의 큐로 실시간 전송합니다.
   *
   * @param chatMessage 전송된 채팅 메시지 데이터 (senderId, receiverId, content 등 포함)
   * @param headerAccessor STOMP 헤더 접근 객체
   */
  @MessageMapping("/chat.sendMessage")
  public void sendMessage(ChatMessage chatMessage, StompHeaderAccessor headerAccessor) {
    // 수정된 부분: headerAccessor.getUser()가 UsernamePasswordAuthenticationToken를 반환하므로,
    // 먼저 해당 객체에서 principal을 꺼내 LoginUserDetails로 캐스팅합니다.
    Object auth = headerAccessor.getUser();
    Long currentUserId;
    if (auth instanceof UsernamePasswordAuthenticationToken) {
      Object principal = ((UsernamePasswordAuthenticationToken) auth).getPrincipal();
      if (principal instanceof LoginUserDetails) {
        currentUserId = ((LoginUserDetails) principal).getUserId();
      } else {
        throw new RuntimeException("Principal is not an instance of LoginUserDetails: " + principal);
      }
    } else if (auth instanceof LoginUserDetails) {
      currentUserId = ((LoginUserDetails) auth).getUserId();
    } else {
      throw new RuntimeException("Authentication type not recognized: " + auth);
    }

    chatMessage.setSenderId(currentUserId);
    ChatMessage savedChat = chatService.saveMessage(chatMessage);
    messageTemplate.convertAndSend("/queue/chat/" + savedChat.getReceiverId(), savedChat);
    messageTemplate.convertAndSend("/queue/chat/" + savedChat.getSenderId(), savedChat);

    // ChatController.java (sendMessage 메서드 내부, unreadCount 갱신 로직 추가 예시)
    int unreadCount = chatService.getUnreadCountFromTrainer(savedChat.getReceiverId());
    messageTemplate.convertAndSend("/queue/notifications/" + savedChat.getReceiverId(), unreadCount);
  }

  @GetMapping("/chat/unreadCount")
  @ResponseBody
  public int getUnreadCount(@AuthenticationPrincipal LoginUserDetails loginUser) {
    if (loginUser == null) {
      return 0;
    }
    return chatService.getUnreadCountFromTrainer(loginUser.getUserId());
  }





  /**
   * 파일 업로드 요청을 처리합니다.
   * 업로드된 파일을 저장한 후, 결과(파일 URL, 타입, 파일명 등)를 JSON 형태로 반환합니다.
   *
   * @param uploadFile 클라이언트에서 전송한 MultipartFile
   * @return UploadResultDTO 파일 업로드 결과 정보
   * @throws IOException 파일 저장 시 발생할 수 있는 예외
   */
  @ResponseBody
  @PostMapping("/chat/uploadFile")
  public UploadResultDTO uploadFile(@RequestParam("uploadFile") MultipartFile uploadFile) throws IOException {
    return fileService.saveToLocal(uploadFile);
  }
}
