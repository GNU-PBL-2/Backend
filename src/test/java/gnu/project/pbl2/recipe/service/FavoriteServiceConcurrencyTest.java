package gnu.project.pbl2.recipe.service;

import static org.assertj.core.api.Assertions.assertThat;

import gnu.project.pbl2.auth.entity.Accessor;
import gnu.project.pbl2.auth.enumerated.SocialProvider;
import gnu.project.pbl2.common.entity.Category;
import gnu.project.pbl2.common.entity.Taste;
import gnu.project.pbl2.common.enumerated.UserRole;
import gnu.project.pbl2.common.repository.CategoryRepository;
import gnu.project.pbl2.common.repository.TasteRepository;
import gnu.project.pbl2.recipe.entity.Recipe;
import gnu.project.pbl2.recipe.repository.FavoriteRepository;
import gnu.project.pbl2.recipe.repository.RecipeRepository;
import gnu.project.pbl2.user.entity.User;
import gnu.project.pbl2.user.repository.UserRepository;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FavoriteServiceConcurrencyTest {

    @Autowired FavoriteService favoriteService;
    @Autowired FavoriteRepository favoriteRepository;
    @Autowired UserRepository userRepository;
    @Autowired RecipeRepository recipeRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired TasteRepository tasteRepository;

    Long recipeId;
    Accessor accessor;

    @BeforeEach
    void setUp() {
        // data.sql로 삽입된 기존 데이터 사용 (새로 insert하면 PK 충돌 발생)
        Category category = categoryRepository.findByName("한식").orElseThrow();
        Taste taste = tasteRepository.findByName("매운맛").orElseThrow();
        Recipe recipe = recipeRepository.save(
            Recipe.create("테스트 레시피", category, taste, 30, "테스트 설명", "https://youtube.com")
        );
        recipeId = recipe.getId();

        User user = userRepository.save(
            User.createFromOAuth("test@test.com", "테스트유저", "test-social-" + System.nanoTime(), SocialProvider.KAKAO)
        );
        accessor = Accessor.user("test-social-id", user.getId(), UserRole.USER);
    }

    @AfterEach
    void tearDown() {
        // data.sql 데이터(Category, Taste, Ingredient 등)는 건드리지 않음
        favoriteRepository.deleteAll();
        recipeRepository.deleteById(recipeId);
        userRepository.deleteById(accessor.getUserId());
    }

    @Test
    @DisplayName("동시에 즐겨찾기 추가 요청이 오면 하나만 저장되고 나머지는 실패해야 한다")
    void 동시에_즐겨찾기_추가_시_하나만_저장된다() throws InterruptedException {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    favoriteService.add(recipeId, accessor);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executor.shutdown();

        assertThat(favoriteRepository.findAll()).hasSize(1);
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(threadCount - 1);
    }

    @Test
    @DisplayName("즐겨찾기가 이미 존재하면 추가 시 예외가 발생해야 한다")
    void 즐겨찾기_중복_추가_시_예외가_발생한다() {
        favoriteService.add(recipeId, accessor);

        AtomicInteger failCount = new AtomicInteger();

        try {
            favoriteService.add(recipeId, accessor);
        } catch (Exception e) {
            failCount.incrementAndGet();
        }

        assertThat(favoriteRepository.findAll()).hasSize(1);
        assertThat(failCount.get()).isEqualTo(1);
    }
}
