package gnu.project.pbl2.notification.controller;

import gnu.project.pbl2.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notification Test", description = "알림 테스트용 API (local 환경 전용)")
@Profile("local")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/test/notifications")
public class NotificationTestController {

    private final NotificationService notificationService;

    @Operation(
        summary = "유통기한 알림 수동 실행",
        description = "스케줄러(매일 오전 9시)를 즉시 수동으로 실행합니다. local 환경에서만 동작합니다."
    )
    @PostMapping("/trigger")
    public ResponseEntity<String> triggerExpiryNotifications() {
        notificationService.checkAndSendExpiryNotifications();
        return ResponseEntity.ok("유통기한 알림 스캔이 완료되었습니다.");
    }
}
