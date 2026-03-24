package gnu.project.pbl2.user.entity;

import gnu.project.pbl2.auth.entity.OauthInfo;
import gnu.project.pbl2.auth.entity.OauthUser;
import gnu.project.pbl2.auth.enumerated.SocialProvider;
import gnu.project.pbl2.common.entity.Allergy;
import gnu.project.pbl2.common.entity.BaseEntity;
import gnu.project.pbl2.common.entity.Category;
import gnu.project.pbl2.common.entity.Taste;
import gnu.project.pbl2.common.enumerated.UserRole;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User extends BaseEntity implements OauthUser {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column(columnDefinition = "BINARY(16)", unique = true, updatable = false, nullable = false)
    private UUID publicId;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserTaste> userTastes = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserCategory> userCategories = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAllergy> userAllergies = new ArrayList<>();

    @Column
    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @Embedded
    private OauthInfo oauthInfo;



    public void delete(){
        super.delete();
    }
    public static User createFromOAuth(
        final String email,
        final String name,
        final String socialId,
        final SocialProvider provider) {
        User user = new User();
        user.publicId = UUID.randomUUID();
        user.userRole = UserRole.USER;
        user.oauthInfo = OauthInfo.of(email, name, socialId, provider);
        return user;
    }
    public void updateOnboarding(
        List<Allergy> allergies,
        List<Taste> tastes,
        List<Category> categories
    ) {
        this.userAllergies.clear();
        this.userTastes.clear();
        this.userCategories.clear();

        allergies.stream()
            .map(a -> UserAllergy.of(this, a))
            .forEach(this.userAllergies::add);

        tastes.stream()
            .map(t -> UserTaste.of(this, t))
            .forEach(this.userTastes::add);

        categories.stream()
            .map(c -> UserCategory.of(this, c))
            .forEach(this.userCategories::add);
    }
    public List<String> getAllergyNames() {
        return userAllergies.stream()
            .map(ua -> ua.getAllergy().getName())
            .toList();
    }

    public List<String> getTasteNames() {
        return userTastes.stream()
            .map(ut -> ut.getTaste().getName())
            .toList();
    }

    public List<String> getCategoryNames() {
        return userCategories.stream()
            .map(uc -> uc.getCategory().getName())
            .toList();
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
