package gnu.project.pbl2.notification.controller.docs;

import gnu.project.pbl2.auth.entity.Accessor;
import gnu.project.pbl2.notification.dto.response.NotificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "Notification", description = "알림 API")
public interface NotificationControllerDocs {

    @Operation(
        summary = "SSE 알림 구독",
        description = """
            SSE 연결을 통해 유통기한 임박 알림을 실시간으로 수신합니다.

            - 연결 직후 미읽은 알림이 즉시 전송됩니다.
            - 매일 오전 9시에 유통기한 3일 이내 재료에 대한 알림이 발송됩니다.
            - 이벤트 타입: `connect` (연결 확인), `notification` (알림)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "SSE 연결 성공")
    })
    SseEmitter subscribe(
        @Parameter(hidden = true) Accessor accessor
    );

    @Operation(
        summary = "알림 목록 조회",
        description = "로그인한 사용자의 전체 알림 목록을 최신순으로 반환합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = NotificationResponse.class))
        )
    })
    ResponseEntity<List<NotificationResponse>> getNotifications(
        @Parameter(hidden = true) Accessor accessor
    );

    @Operation(
        summary = "알림 읽음 처리",
        description = "특정 알림을 읽음 상태로 변경합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "읽음 처리 성공"),
        @ApiResponse(responseCode = "403", description = "다른 사용자의 알림", content = @Content(schema = @Schema(hidden = true))),
        @ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음", content = @Content(schema = @Schema(hidden = true)))
    })
    ResponseEntity<Void> markAsRead(
        @Parameter(description = "알림 ID", example = "1") Long notificationId,
        @Parameter(hidden = true) Accessor accessor
    );
}
