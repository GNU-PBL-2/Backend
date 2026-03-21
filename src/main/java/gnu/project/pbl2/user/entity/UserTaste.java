package gnu.project.pbl2.user.entity;

import gnu.project.pbl2.common.entity.Taste;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Table(name = "user_tastes")
public class UserTaste {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @JoinColumn(name = "taste_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Taste taste;
    public static UserTaste of(final User user, final Taste taste) {
        final UserTaste userTaste = new UserTaste();
        userTaste.user = user;
        userTaste.taste = taste;
        return userTaste;
    }
}
