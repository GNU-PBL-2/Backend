package gnu.project.pbl2.notification.entity;

import gnu.project.pbl2.common.entity.BaseEntity;
import gnu.project.pbl2.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private User member;

    @Column(name = "ingredient_name", nullable = false, length = 100)
    private String ingredientName;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    private Notification(User member, String ingredientName, LocalDate expiryDate) {
        this.member = member;
        this.ingredientName = ingredientName;
        this.expiryDate = expiryDate;
        this.isRead = false;
    }

    public static Notification create(User member, String ingredientName, LocalDate expiryDate) {
        return new Notification(member, ingredientName, expiryDate);
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
