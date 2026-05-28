package gnu.project.pbl2.fridge.service;

import gnu.project.pbl2.common.error.ErrorCode;
import gnu.project.pbl2.common.exception.BusinessException;
import gnu.project.pbl2.fridge.dto.response.DetectedIngredientResponse;
import gnu.project.pbl2.fridge.dto.response.DetectedIngredientResponse.MatchedIngredient;
import gnu.project.pbl2.fridge.dto.response.YoloApiResponse;
import gnu.project.pbl2.fridge.entity.Ingredient;
import gnu.project.pbl2.fridge.repository.IngredientRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 카메라 이미지를 FastAPI YOLO 서버로 전달해 재료를 감지하고,
 * 감지된 재료명을 DB의 ingredient 테이블과 매칭하는 서비스.
 *
 * <p>처리 흐름:
 * <ol>
 *   <li>클라이언트가 보낸 이미지(MultipartFile)를 FastAPI {@code /detect-ingredients}로 전달</li>
 *   <li>FastAPI가 YOLO 추론 후 재료명 목록 반환</li>
 *   <li>반환된 재료명을 DB에서 검색(대소문자 무시)</li>
 *   <li>매칭된 재료와 미매칭 재료를 분리해 {@link DetectedIngredientResponse}로 반환</li>
 * </ol>
 */
@Slf4j
@Service
public class YoloService {

    private static final DateTimeFormatter TIMESTAMP_FMT =
        DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final WebClient webClient;
    private final IngredientRepository ingredientRepository;
    private final IngredientNameMapper ingredientNameMapper;
    private final String fastapiBaseUrl;

    /**
     * false 이면 FastAPI 호출을 건너뛰고 빈 결과를 반환한다.
     * FastAPI 서버를 띄울 수 없는 로컬 개발 환경에서 사용.
     * application-local.yml: yolo.enabled: false
     */
    @Value("${yolo.enabled:true}")
    private boolean yoloEnabled;

    /** true 이면 수신 이미지를 로컬 디스크에 저장 (로컬 개발 디버그용) */
    @Value("${yolo.debug.save-images:false}")
    private boolean debugSaveImages;

    /** 디버그 이미지 저장 디렉토리 (프로젝트 루트 기준 상대 경로) */
    @Value("${yolo.debug.save-dir:uploads/yolo/debug}")
    private String debugSaveDir;

    public YoloService(
        final WebClient webClient,
        final IngredientRepository ingredientRepository,
        final IngredientNameMapper ingredientNameMapper,
        @Value("${fastapi.base-url}") final String fastapiBaseUrl
    ) {
        this.webClient = webClient;
        this.ingredientRepository = ingredientRepository;
        this.ingredientNameMapper = ingredientNameMapper;
        this.fastapiBaseUrl = fastapiBaseUrl;
    }

    /**
     * 이미지를 분석해 감지된 재료를 DB와 매칭하여 반환한다.
     *
     * @param imageFile 카메라 또는 갤러리에서 업로드한 이미지 파일
     * @return 매칭된 재료 목록과 미매칭 재료명 목록
     * @throws BusinessException 이미지 파일이 아닌 경우 또는 FastAPI 호출 실패 시
     */
    public DetectedIngredientResponse detectIngredients(final MultipartFile imageFile) {
        validateImageFile(imageFile);

        // 이미지 읽기 + 디버그 저장은 yolo.enabled 여부와 무관하게 먼저 수행
        final byte[] imageBytes = readImageBytes(imageFile);
        saveDebugImage(imageBytes, imageFile.getOriginalFilename(), imageFile.getContentType());

        // yolo.enabled=false 이면 FastAPI 호출 없이 빈 결과 반환
        if (!yoloEnabled) {
            log.warn("[YOLO DISABLED] FastAPI 호출을 건너뜁니다. (yolo.enabled=false)");
            return new DetectedIngredientResponse(List.of(), List.of());
        }

        final List<String> detectedNames = callYoloApi(imageFile, imageBytes);
        log.info("YOLO 감지 결과: {} 개 재료 — {}", detectedNames.size(), detectedNames);

        return matchWithDatabase(detectedNames);
    }

    // ──────────────────────────────────────────────
    // private helpers
    // ──────────────────────────────────────────────

    /**
     * 이미지 Content-Type 유효성 검증.
     * {@code image/} 로 시작하지 않으면 예외를 발생시킨다.
     */
    private void validateImageFile(final MultipartFile imageFile) {
        final String contentType = imageFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/") || imageFile.isEmpty()) {
            throw new BusinessException(ErrorCode.YOLO_INVALID_IMAGE);
        }
    }

    /**
     * FastAPI YOLO 서버({@code /detect-ingredients})를 호출해 감지된 재료명 목록을 가져온다.
     * WebClient multipart/form-data 방식으로 이미지를 전송한다.
     *
     * @param imageFile  원본 파일 (파일명·Content-Type 참조용)
     * @param imageBytes 이미 읽은 이미지 바이트 (detectIngredients에서 전달)
     */
    private List<String> callYoloApi(final MultipartFile imageFile, final byte[] imageBytes) {
        // multipart body 조립: FastAPI가 기대하는 필드명 "image"
        final MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("image", new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                // 파일명이 null인 경우를 방어적으로 처리
                final String original = imageFile.getOriginalFilename();
                return (original != null && !original.isBlank()) ? original : "image.jpg";
            }
        }).contentType(MediaType.parseMediaType(
            imageFile.getContentType() != null ? imageFile.getContentType() : "image/jpeg"
        ));

        try {
            final YoloApiResponse response = webClient.post()
                .uri(fastapiBaseUrl + "/detect-ingredients")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .onStatus(
                    status -> status.is4xxClientError() || status.is5xxServerError(),
                    res -> res.bodyToMono(String.class)
                        .map(body -> {
                            log.error("FastAPI YOLO 오류 응답: {}", body);
                            return new BusinessException(ErrorCode.YOLO_DETECT_FAILED);
                        })
                )
                .bodyToMono(YoloApiResponse.class)
                .block(java.time.Duration.ofSeconds(30));

            if (response == null || response.ingredients() == null) {
                throw new BusinessException(ErrorCode.YOLO_DETECT_FAILED);
            }

            return response.ingredients();

        } catch (final BusinessException e) {
            throw e;
        } catch (final Exception e) {
            log.error("FastAPI YOLO 호출 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.YOLO_DETECT_FAILED);
        }
    }

    /**
     * MultipartFile의 바이트 배열을 읽는다.
     * IOException 발생 시 BusinessException으로 변환한다.
     */
    private byte[] readImageBytes(final MultipartFile imageFile) {
        try {
            return imageFile.getBytes();
        } catch (final IOException e) {
            log.error("이미지 파일 읽기 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.YOLO_DETECT_FAILED);
        }
    }

    /**
     * 디버그 모드({@code yolo.debug.save-images=true})일 때 이미지를 로컬에 저장한다.
     * yolo.enabled=false 여도 저장되므로 프론트 수신 확인에 활용할 수 있다.
     *
     * <p>저장 경로: {@code {save-dir}/received_{timestamp}_{uuid}.{ext}}
     * <p>예시: {@code uploads/yolo/debug/received_20260527_143052_a1b2c3d4.jpg}
     *
     * @param imageBytes   저장할 이미지 바이트
     * @param originalName 원본 파일명 (확장자 추출용)
     * @param contentType  Content-Type (확장자 fallback용)
     */
    private void saveDebugImage(
        final byte[] imageBytes,
        final String originalName,
        final String contentType
    ) {
        if (!debugSaveImages) {
            return;
        }

        try {
            final Path dir = Paths.get(debugSaveDir);
            Files.createDirectories(dir);

            final String ext       = resolveExtension(originalName, contentType);
            final String timestamp = LocalDateTime.now().format(TIMESTAMP_FMT);
            final String uid       = UUID.randomUUID().toString().substring(0, 8);
            final String filename  = String.format("received_%s_%s.%s", timestamp, uid, ext);

            final Path savePath = dir.resolve(filename);
            Files.write(savePath, imageBytes);

            log.info("[YOLO DEBUG] 이미지 저장 완료 → {} ({}KB)",
                savePath.toAbsolutePath(), imageBytes.length / 1024);

        } catch (final IOException e) {
            // 디버그 저장 실패는 본 기능에 영향을 주지 않는다
            log.warn("[YOLO DEBUG] 이미지 저장 실패 (무시): {}", e.getMessage());
        }
    }

    /** 파일명 또는 Content-Type 으로 확장자를 결정한다. */
    private String resolveExtension(final String originalName, final String contentType) {
        if (originalName != null && originalName.contains(".")) {
            return originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
        }
        if (contentType != null) {
            return switch (contentType) {
                case "image/jpeg" -> "jpg";
                case "image/png"  -> "png";
                case "image/webp" -> "webp";
                default           -> "bin";
            };
        }
        return "jpg";
    }

    /**
     * YOLO가 감지한 재료명 목록을 DB와 매칭한다.
     * <ul>
     *   <li>DB에 이름이 있으면 {@code matched} 목록에 추가 (ingredientId 포함)</li>
     *   <li>DB에 없으면 {@code unmatched} 목록에 추가</li>
     * </ul>
     *
     * @param detectedNames YOLO가 반환한 재료명 목록
     */
    private DetectedIngredientResponse matchWithDatabase(final List<String> detectedNames) {
        final List<MatchedIngredient> matched = new ArrayList<>();
        final List<String> unmatched = new ArrayList<>();

        for (final String name : detectedNames) {
            final String koreanName = ingredientNameMapper.toKorean(name).orElse(null);
            if (koreanName == null) {
                unmatched.add(name);
                continue;
            }
            final Optional<Ingredient> ingredientOpt = ingredientRepository.findByName(koreanName);
            if (ingredientOpt.isPresent()) {
                final Ingredient ingredient = ingredientOpt.get();
                matched.add(new MatchedIngredient(ingredient.getId(), ingredient.getName()));
            } else {
                unmatched.add(name);
            }
        }

        log.info("DB 매칭 결과 — 매칭: {} 개, 미매칭: {} 개", matched.size(), unmatched.size());
        return new DetectedIngredientResponse(matched, unmatched);
    }
}
