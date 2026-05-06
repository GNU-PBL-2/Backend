package gnu.project.pbl2.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class SseEmitterRepositoryConcurrencyTest {

    // SseEmitter.onCompletion() 콜백은 HTTP 핸들러 없이 동작하지 않으므로
    // complete() 자체를 오버라이드해 호출 여부를 추적한다
    static class TrackableEmitter extends SseEmitter {
        final AtomicBoolean completed = new AtomicBoolean(false);

        TrackableEmitter() {
            super(1000L);
        }

        @Override
        public void complete() {
            completed.set(true);
            super.complete();
        }
    }

    private SseEmitterRepository repository;

    @BeforeEach
    void setUp() {
        repository = new SseEmitterRepository();
    }

    @Test
    @DisplayName("같은 유저로 재연결 시 이전 이미터가 complete() 처리되어야 한다")
    void 재연결_시_이전_이미터가_완료된다() {
        Long userId = 1L;
        TrackableEmitter first = new TrackableEmitter();
        repository.save(userId, first);

        SseEmitter second = new SseEmitter(1000L);
        repository.save(userId, second);

        assertThat(first.completed.get()).isTrue();
        assertThat(repository.findById(userId)).hasValueSatisfying(e -> assertThat(e).isSameAs(second));
    }

    @Test
    @DisplayName("동시에 저장 시 이전 이미터들이 모두 complete() 처리되고 하나만 남아야 한다")
    void 동시에_저장_시_이전_이미터가_모두_완료된다() throws InterruptedException {
        int threadCount = 10;
        Long userId = 1L;
        CopyOnWriteArrayList<TrackableEmitter> emitters = new CopyOnWriteArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    TrackableEmitter emitter = new TrackableEmitter();
                    emitters.add(emitter);
                    repository.save(userId, emitter);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executor.shutdown();

        long completedCount = emitters.stream()
            .filter(e -> e.completed.get())
            .count();

        // 10개 저장 시 최초 저장을 제외한 9개(threadCount - 1)의 이전 이미터가 완료되어야 함
        assertThat(repository.findById(userId)).isPresent();
        assertThat(completedCount).isEqualTo(threadCount - 1);
    }

    @Test
    @DisplayName("서로 다른 유저의 이미터는 독립적으로 관리되어야 한다")
    void 다른_유저_이미터는_독립적으로_관리된다() {
        TrackableEmitter emitter1 = new TrackableEmitter();
        TrackableEmitter emitter2 = new TrackableEmitter();

        repository.save(1L, emitter1);
        repository.save(2L, emitter2);

        repository.delete(1L);

        assertThat(repository.findById(1L)).isEmpty();
        assertThat(repository.findById(2L)).isPresent();
        assertThat(emitter2.completed.get()).isFalse();
    }

    @Test
    @DisplayName("이미터 삭제 후 조회하면 비어있어야 한다")
    void 이미터_삭제_후_조회_시_비어있다() {
        Long userId = 1L;
        repository.save(userId, new SseEmitter(1000L));
        repository.delete(userId);

        assertThat(repository.findById(userId)).isEmpty();
    }
}
