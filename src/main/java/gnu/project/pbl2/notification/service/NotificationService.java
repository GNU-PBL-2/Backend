package gnu.project.pbl2.notification.service;

import gnu.project.pbl2.common.error.ErrorCode;
import gnu.project.pbl2.common.exception.AuthException;
import gnu.project.pbl2.common.exception.NotFoundException;
import gnu.project.pbl2.fridge.repository.FridgeRepository;
import gnu.project.pbl2.notification.dto.response.NotificationResponse;
import gnu.project.pbl2.notification.entity.Notification;
import gnu.project.pbl2.notification.repository.NotificationRepository;
import gnu.project.pbl2.notification.repository.SseEmitterRepository;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private static final long SSE_TIMEOUT = 60L * 60 * 1000; // 1시간
    private static final int EXPIRY_THRESHOLD_DAYS = 3;

    private final NotificationRepository notificationRepository;
    private final SseEmitterRepository sseEmitterRepository;
    private final FridgeRepository fridgeRepository;

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        sseEmitterRepository.save(userId, emitter);

        emitter.onCompletion(() -> sseEmitterRepository.delete(userId));
        emitter.onTimeout(() -> sseEmitterRepository.delete(userId));
        emitter.onError(e -> sseEmitterRepository.delete(userId));

        sendToEmitter(emitter, userId, "connect", "연결이 완료되었습니다.");

        notificationRepository.findUnreadByUserId(userId)
            .forEach(n -> sendToEmitter(emitter, userId, "notification", NotificationResponse.from(n)));

        return emitter;
    }

    public List<NotificationResponse> getNotifications(Long userId) {
        return notificationRepository.findAllByUserId(userId)
            .stream()
            .map(NotificationResponse::from)
            .toList();
    }

    @Transactional
    @Scheduled(cron = "0 0 9 * * *")
    public void checkAndSendExpiryNotifications() {
        LocalDate threshold = LocalDate.now().plusDays(EXPIRY_THRESHOLD_DAYS);
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        fridgeRepository.findAllExpiringFridgeItems(threshold).forEach(fridge -> {
            Long userId = fridge.getMember().getId();
            String ingredientName = fridge.getIngredient().getName();

            if (notificationRepository.existsTodayNotification(userId, ingredientName, startOfDay, endOfDay)) {
                return;
            }

            Notification notification = Notification.create(
                fridge.getMember(), ingredientName, fridge.getExpiryDate()
            );
            notificationRepository.save(notification);

            sseEmitterRepository.findById(userId).ifPresent(emitter ->
                sendToEmitter(emitter, userId, "notification", NotificationResponse.from(notification)));
        });
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new NotFoundException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getMember().getId().equals(userId)) {
            throw new AuthException(ErrorCode.AUTH_FORBIDDEN);
        }

        notification.markAsRead();
    }

    private void sendToEmitter(SseEmitter emitter, Long userId, String eventName, Object data) {
        try {
            emitter.send(SseEmitter.event().name(eventName).data(data));
        } catch (IOException e) {
            log.warn("SSE 전송 실패 - userId: {}", userId);
            sseEmitterRepository.delete(userId);
            emitter.completeWithError(e);
        }
    }
}
