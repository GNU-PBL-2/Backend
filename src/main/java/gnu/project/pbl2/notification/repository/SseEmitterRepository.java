package gnu.project.pbl2.notification.repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class SseEmitterRepository {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public void save(Long userId, SseEmitter emitter) {
        emitters.compute(userId, (id, old) -> {
            if (old != null) {
                old.complete();
            }
            return emitter;
        });
    }

    public Optional<SseEmitter> findById(Long userId) {
        return Optional.ofNullable(emitters.get(userId));
    }

    public void delete(Long userId) {
        emitters.remove(userId);
    }
}
