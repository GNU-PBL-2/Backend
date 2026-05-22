package gnu.project.pbl2.notification.service;

import static org.assertj.core.api.Assertions.assertThat;

import gnu.project.pbl2.auth.enumerated.SocialProvider;
import gnu.project.pbl2.fridge.entity.Fridge;
import gnu.project.pbl2.fridge.entity.Ingredient;
import gnu.project.pbl2.fridge.repository.FridgeRepository;
import gnu.project.pbl2.fridge.repository.IngredientRepository;
import gnu.project.pbl2.notification.entity.Notification;
import gnu.project.pbl2.notification.repository.NotificationRepository;
import gnu.project.pbl2.user.entity.User;
import gnu.project.pbl2.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NotificationSchedulerTest {

    @Autowired NotificationService notificationService;
    @Autowired NotificationRepository notificationRepository;
    @Autowired FridgeRepository fridgeRepository;
    @Autowired IngredientRepository ingredientRepository;
    @Autowired UserRepository userRepository;

    User user;
    Ingredient ingredient;

    @BeforeEach
    void setUp() {
        user = userRepository.save(
            User.createFromOAuth("scheduler-test@test.com", "스케줄러테스트유저", "sched-" + System.nanoTime(), SocialProvider.KAKAO)
        );
        // data.sql에 이미 있는 재료 재사용 (PK 충돌 방지)
        ingredient = ingredientRepository.findAll().get(0);
    }

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAll();
        fridgeRepository.findAllByMember_Id(user.getId()).forEach(f -> fridgeRepository.delete(f));
        userRepository.deleteById(user.getId());
    }

    @Test
    @DisplayName("유통기한 3일 이내 재료가 있으면 알림이 생성된다")
    void 유통기한_임박_재료_알림_생성() {
        LocalDate expiryDate = LocalDate.now().plusDays(2);
        fridgeRepository.save(Fridge.create(user, ingredient, BigDecimal.ONE, "개", expiryDate));

        notificationService.checkAndSendExpiryNotifications();

        List<Notification> notifications = notificationRepository.findAllByUserId(user.getId());
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getIngredientName()).isEqualTo(ingredient.getName());
        assertThat(notifications.get(0).getExpiryDate()).isEqualTo(expiryDate);
    }

    @Test
    @DisplayName("유통기한이 4일 이상 남은 재료는 알림이 생성되지 않는다")
    void 유통기한_여유_재료_알림_미생성() {
        fridgeRepository.save(Fridge.create(user, ingredient, BigDecimal.ONE, "개", LocalDate.now().plusDays(4)));

        notificationService.checkAndSendExpiryNotifications();

        assertThat(notificationRepository.findAllByUserId(user.getId())).isEmpty();
    }

    @Test
    @DisplayName("스케줄러가 여러 번 실행되어도 같은 알림은 중복 생성되지 않는다")
    void 스케줄러_반복_실행_시_중복_알림_없음() {
        LocalDate expiryDate = LocalDate.now().plusDays(1);
        fridgeRepository.save(Fridge.create(user, ingredient, BigDecimal.ONE, "개", expiryDate));

        notificationService.checkAndSendExpiryNotifications();
        notificationService.checkAndSendExpiryNotifications();
        notificationService.checkAndSendExpiryNotifications();

        List<Notification> notifications = notificationRepository.findAllByUserId(user.getId());
        assertThat(notifications).hasSize(1);
    }

    @Test
    @DisplayName("유통기한이 없는 재료는 알림이 생성되지 않는다")
    void 유통기한_없는_재료_알림_미생성() {
        fridgeRepository.save(Fridge.create(user, ingredient, BigDecimal.ONE, "개", null));

        notificationService.checkAndSendExpiryNotifications();

        assertThat(notificationRepository.findAllByUserId(user.getId())).isEmpty();
    }
}
