package gnu.project.pbl2.user.entity;

import gnu.project.pbl2.auth.entity.OauthInfo;
import gnu.project.pbl2.auth.entity.OauthUser;
import gnu.project.pbl2.auth.enumerated.SocialProvider;
import gnu.project.pbl2.common.entity.BaseEntity;
import gnu.project.pbl2.common.enumerated.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class User extends BaseEntity implements OauthUser {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(columnDefinition = "BINARY(16)", unique = true, nullable = false, updatable = false)
    private java.util.UUID publicId;


    @Column
    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @Embedded
    private OauthInfo oauthInfo;

    public static User createFromOAuth(
        final String email,
        final String name,
        final String socialId,
        final SocialProvider provider) {
        OauthInfo oauthInfo = OauthInfo.of(email,name,socialId,provider);
        return new User(
            null,
            null,
            UserRole.USER,
            oauthInfo
        );
    }

    @Override
    public UserRole getUserRole() {
        return this.userRole != null ? this.userRole : UserRole.USER;
    }
    @Override
    public OauthInfo getOauthInfo() {
        return this.oauthInfo;
    }
}
