package gnu.project.pbl2.notification.controller;

import gnu.project.pbl2.auth.aop.Auth;
import gnu.project.pbl2.auth.entity.Accessor;
import gnu.project.pbl2.notification.controller.docs.NotificationControllerDocs;
import gnu.project.pbl2.notification.dto.response.NotificationResponse;
import gnu.project.pbl2.notification.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController implements NotificationControllerDocs {

    private final NotificationService notificationService;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@Auth Accessor accessor) {
        return notificationService.subscribe(accessor.getUserId());
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(@Auth Accessor accessor) {
        return ResponseEntity.ok(notificationService.getNotifications(accessor.getUserId()));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
        @PathVariable Long notificationId,
        @Auth Accessor accessor
    ) {
        notificationService.markAsRead(notificationId, accessor.getUserId());
        return ResponseEntity.noContent().build();
    }
}
