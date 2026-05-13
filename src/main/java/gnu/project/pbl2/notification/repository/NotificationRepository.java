package gnu.project.pbl2.notification.repository;

import gnu.project.pbl2.notification.entity.Notification;
import java.time.LocalDate;
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
        + "AND n.expiryDate = :expiryDate AND n.isDeleted = false")
    boolean existsByUserAndIngredientAndExpiryDate(
        @Param("userId") Long userId,
        @Param("ingredientName") String ingredientName,
        @Param("expiryDate") LocalDate expiryDate
    );
}
