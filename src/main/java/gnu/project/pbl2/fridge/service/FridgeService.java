package gnu.project.pbl2.fridge.service;

import gnu.project.pbl2.fridge.dto.request.FridgeCreateRequest;
import gnu.project.pbl2.fridge.dto.request.FridgeUpdateRequest;
import gnu.project.pbl2.fridge.dto.response.FridgeResponse;
import gnu.project.pbl2.fridge.entity.Fridge;
import gnu.project.pbl2.fridge.entity.Ingredient;
import gnu.project.pbl2.fridge.repository.FridgeRepository;
import gnu.project.pbl2.fridge.repository.IngredientRepository;
import gnu.project.pbl2.user.entity.User;
import gnu.project.pbl2.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
/**
 * 냉장고 재료 도메인의 비즈니스 로직을 처리하는 서비스.
 * 조회, 등록, 수정, 삭제 과정에서 필요한 엔티티 조회와 상태 변경을 담당한다.
 */
public class FridgeService {

    // 냉장고 재료 저장소
    private final FridgeRepository fridgeRepository;
    // 회원 존재 여부 확인 및 회원 조회용 저장소
    private final UserRepository userRepository;
    // 재료 존재 여부 확인 및 재료 조회용 저장소
    private final IngredientRepository ingredientRepository;

    // 회원 ID로 냉장고 재료 목록을 조회하고 응답 DTO로 변환한다.
    public List<FridgeResponse> getFridgeByMemberId(final Long memberId) {
        return fridgeRepository.findAllByMember_Id(memberId)
            .stream()
            .map(FridgeResponse::from)
            .toList();
    }

    @Transactional
    // 회원과 재료를 검증한 뒤 냉장고 재료를 새로 등록한다.
    public FridgeResponse addIngredient(final FridgeCreateRequest request) {
        final User member = userRepository.findById(request.memberId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        final Ingredient ingredient = ingredientRepository.findById(request.ingredientId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 재료입니다."));

        final Fridge fridge = Fridge.create(
            member,
            ingredient,
            request.quantity(),
            request.unit(),
            request.expiryDate()
        );

        return FridgeResponse.from(fridgeRepository.save(fridge));
    }

    @Transactional
    // 냉장고 재료를 조회한 뒤 수량, 단위, 유통기한을 변경한다.
    public FridgeResponse updateIngredient(
        final Long fridgeId,
        final FridgeUpdateRequest request
    ) {
        final Fridge fridge = fridgeRepository.findById(fridgeId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 냉장고 재료입니다."));

        fridge.updateQuantity(request.quantity());
        fridge.updateUnit(request.unit());
        fridge.updateExpiryDate(request.expiryDate());

        return FridgeResponse.from(fridge);
    }

    @Transactional
    // 냉장고 재료를 조회한 뒤 삭제한다.
    public void deleteIngredient(final Long fridgeId) {
        final Fridge fridge = fridgeRepository.findById(fridgeId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 냉장고 재료입니다."));

        fridgeRepository.delete(fridge);
    }
}
