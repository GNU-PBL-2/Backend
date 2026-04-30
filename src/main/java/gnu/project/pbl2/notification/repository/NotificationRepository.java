package gnu.project.pbl2.notification.repository;

import gnu.project.pbl2.notification.entity.Notification;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE n.member.id = :userId AND n.isRead = false AND n.isDeleted = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadByUserId(@Param("userId") Long userId);

    @Query("SELECT n FROM Notification n WHERE n.member.id = :userId AND n.isDeleted = false ORDER BY n.createdAt DESC")
    List<Notification> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN TRUE ELSE FALSE END FROM Notification n "
        + "WHERE n.member.id = :userId AND n.ingredientName = :ingredientName "
        + "AND n.createdAt >= :startOfDay AND n.createdAt < :endOfDay AND n.isDeleted = false")
    boolean existsTodayNotification(
        @Param("userId") Long userId,
        @Param("ingredientName") String ingredientName,
        @Param("startOfDay") LocalDateTime startOfDay,
        @Param("endOfDay") LocalDateTime endOfDay
    );
}
