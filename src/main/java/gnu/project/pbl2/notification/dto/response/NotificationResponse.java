package gnu.project.pbl2.notification.dto.response;

import gnu.project.pbl2.notification.entity.Notification;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record NotificationResponse(
    Long notificationId,
    String ingredientName,
    LocalDate expiryDate,
    long daysUntilExpiry,
    boolean isRead,
    LocalDateTime createdAt
) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
            notification.getId(),
            notification.getIngredientName(),
            notification.getExpiryDate(),
            ChronoUnit.DAYS.between(LocalDate.now(), notification.getExpiryDate()),
            notification.isRead(),
            notification.getCreatedAt()
        );
    }
}
