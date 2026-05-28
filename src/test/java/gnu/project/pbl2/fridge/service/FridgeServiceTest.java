package gnu.project.pbl2.fridge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import gnu.project.pbl2.common.error.ErrorCode;
import gnu.project.pbl2.common.exception.BusinessException;
import gnu.project.pbl2.fridge.dto.request.FridgeCreateRequest;
import gnu.project.pbl2.fridge.dto.request.FridgeUpdateRequest;
import gnu.project.pbl2.fridge.dto.response.FridgeResponse;
import gnu.project.pbl2.fridge.entity.Fridge;
import gnu.project.pbl2.fridge.entity.Ingredient;
import gnu.project.pbl2.fridge.repository.FridgeRepository;
import gnu.project.pbl2.fridge.repository.IngredientRepository;
import gnu.project.pbl2.user.entity.User;
import gnu.project.pbl2.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * FridgeService 단위 테스트.
 * 스프링 컨텍스트 없이 Mockito만으로 비즈니스 로직을 검증한다.
 *
 * <p>LENIENT 모드: @BeforeEach 공통 픽스처 stub이 일부 테스트에서
 * 사용되지 않더라도 UnnecessaryStubbingException 이 발생하지 않도록 한다.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FridgeServiceTest {

    @Mock private FridgeRepository fridgeRepository;
    @Mock private UserRepository userRepository;
    @Mock private IngredientRepository ingredientRepository;

    @InjectMocks private FridgeService fridgeService;

    // ── 공통 테스트 픽스처 ──────────────────────────────────────
    private User mockUser;
    private Ingredient mockIngredient;
    private Fridge mockFridge;

    @BeforeEach
    void setUp() {
        mockUser = mock(User.class);
        given(mockUser.getId()).willReturn(1L);

        mockIngredient = mock(Ingredient.class);
        given(mockIngredient.getId()).willReturn(10L);
        given(mockIngredient.getName()).willReturn("토마토");

        mockFridge = mock(Fridge.class);
        given(mockFridge.getFridgeId()).willReturn(100L);
        given(mockFridge.getMember()).willReturn(mockUser);
        given(mockFridge.getIngredient()).willReturn(mockIngredient);
        given(mockFridge.getQuantity()).willReturn(BigDecimal.valueOf(2));
        given(mockFridge.getUnit()).willReturn("개");
        given(mockFridge.getExpiryDate()).willReturn(LocalDate.of(2026, 12, 31));
    }

    // ════════════════════════════════════════════════════
    // getFridgeByMemberId
    // ════════════════════════════════════════════════════
    @Nested
    @DisplayName("getFridgeByMemberId — 냉장고 목록 조회")
    class GetFridgeByMemberId {

        @Test
        @DisplayName("회원 ID로 냉장고 재료 목록을 조회하면 FridgeResponse 목록이 반환된다")
        void 냉장고_목록_조회_성공() {
            given(fridgeRepository.findAllByMember_Id(1L)).willReturn(List.of(mockFridge));

            List<FridgeResponse> result = fridgeService.getFridgeByMemberId(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).ingredientId()).isEqualTo(10L);
            assertThat(result.get(0).ingredientName()).isEqualTo("토마토");
            assertThat(result.get(0).quantity()).isEqualByComparingTo(BigDecimal.valueOf(2));
        }

        @Test
        @DisplayName("냉장고가 비어 있으면 빈 목록이 반환된다")
        void 냉장고_비어있으면_빈목록() {
            given(fridgeRepository.findAllByMember_Id(1L)).willReturn(List.of());

            List<FridgeResponse> result = fridgeService.getFridgeByMemberId(1L);

            assertThat(result).isEmpty();
        }
    }

    // ════════════════════════════════════════════════════
    // addIngredient
    // ════════════════════════════════════════════════════
    @Nested
    @DisplayName("addIngredient — 냉장고 재료 추가")
    class AddIngredient {

        private FridgeCreateRequest validRequest;

        @BeforeEach
        void setUp() {
            validRequest = new FridgeCreateRequest(
                1L, 10L, BigDecimal.valueOf(3), "개", LocalDate.of(2026, 12, 31)
            );
        }

        @Test
        @DisplayName("정상 요청이면 냉장고 재료가 저장되고 FridgeResponse가 반환된다")
        void 재료_추가_성공() {
            given(userRepository.findById(1L)).willReturn(Optional.of(mockUser));
            given(ingredientRepository.findById(10L)).willReturn(Optional.of(mockIngredient));
            given(fridgeRepository.save(any(Fridge.class))).willReturn(mockFridge);

            FridgeResponse result = fridgeService.addIngredient(validRequest);

            assertThat(result.fridgeId()).isEqualTo(100L);
            assertThat(result.ingredientName()).isEqualTo("토마토");
            then(fridgeRepository).should().save(any(Fridge.class));
        }

        @Test
        @DisplayName("존재하지 않는 회원 ID면 IllegalArgumentException이 발생한다")
        void 회원_없으면_예외() {
            given(userRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> fridgeService.addIngredient(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 회원");
        }

        @Test
        @DisplayName("존재하지 않는 재료 ID면 IllegalArgumentException이 발생한다")
        void 재료_없으면_예외() {
            given(userRepository.findById(1L)).willReturn(Optional.of(mockUser));
            given(ingredientRepository.findById(10L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> fridgeService.addIngredient(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 재료");
        }
    }

    // ════════════════════════════════════════════════════
    // updateIngredient
    // ════════════════════════════════════════════════════
    @Nested
    @DisplayName("updateIngredient — 냉장고 재료 수정")
    class UpdateIngredient {

        @Test
        @DisplayName("수량, 단위, 유통기한 모두 변경하면 각 필드가 업데이트된다")
        void 재료_전체_수정_성공() {
            given(fridgeRepository.findById(100L)).willReturn(Optional.of(mockFridge));
            FridgeUpdateRequest request = new FridgeUpdateRequest(
                BigDecimal.TEN, "kg", LocalDate.of(2027, 6, 30)
            );

            fridgeService.updateIngredient(100L, request);

            then(mockFridge).should().updateQuantity(BigDecimal.TEN);
            then(mockFridge).should().updateUnit("kg");
            then(mockFridge).should().updateExpiryDate(LocalDate.of(2027, 6, 30));
        }

        @Test
        @DisplayName("null 필드는 업데이트하지 않는다 (부분 수정)")
        void 재료_부분_수정_성공() {
            given(fridgeRepository.findById(100L)).willReturn(Optional.of(mockFridge));
            FridgeUpdateRequest request = new FridgeUpdateRequest(BigDecimal.TEN, null, null);

            fridgeService.updateIngredient(100L, request);

            then(mockFridge).should().updateQuantity(BigDecimal.TEN);
            // unit/expiryDate 가 null 이면 서비스에서 해당 메서드를 호출하지 않는다
            then(mockFridge).should(never()).updateUnit(any());
            then(mockFridge).should(never()).updateExpiryDate(any());
        }

        @Test
        @DisplayName("존재하지 않는 fridgeId면 FRIDGE_NOT_FOUND 예외가 발생한다")
        void 냉장고_없으면_예외() {
            given(fridgeRepository.findById(999L)).willReturn(Optional.empty());
            FridgeUpdateRequest request = new FridgeUpdateRequest(BigDecimal.TEN, null, null);

            assertThatThrownBy(() -> fridgeService.updateIngredient(999L, request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.FRIDGE_NOT_FOUND);
        }
    }

    // ════════════════════════════════════════════════════
    // deleteIngredient
    // ════════════════════════════════════════════════════
    @Nested
    @DisplayName("deleteIngredient — 냉장고 재료 삭제")
    class DeleteIngredient {

        @Test
        @DisplayName("존재하는 fridgeId면 냉장고 재료가 삭제된다")
        void 재료_삭제_성공() {
            given(fridgeRepository.findById(100L)).willReturn(Optional.of(mockFridge));

            fridgeService.deleteIngredient(100L);

            then(fridgeRepository).should().delete(mockFridge);
        }

        @Test
        @DisplayName("존재하지 않는 fridgeId면 FRIDGE_NOT_FOUND 예외가 발생한다")
        void 냉장고_없으면_예외() {
            given(fridgeRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> fridgeService.deleteIngredient(999L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.FRIDGE_NOT_FOUND);
        }
    }
}
