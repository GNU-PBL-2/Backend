package gnu.project.pbl2.Fridge.service;

import gnu.project.pbl2.Fridge.dto.request.FridgeCreateRequest;
import gnu.project.pbl2.Fridge.dto.response.FridgeResponse;
import gnu.project.pbl2.Fridge.entity.Fridge;
import gnu.project.pbl2.Fridge.entity.Ingredient;
import gnu.project.pbl2.Fridge.repository.FridgeRepository;
import gnu.project.pbl2.Fridge.repository.IngredientRepository;
import gnu.project.pbl2.user.entity.User;
import gnu.project.pbl2.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FridgeService {

    private final FridgeRepository fridgeRepository;
    private final UserRepository userRepository;
    private final IngredientRepository ingredientRepository;

    public List<FridgeResponse> getFridgeByMemberId(final Long memberId) {
        return fridgeRepository.findAllByMember_Id(memberId)
            .stream()
            .map(FridgeResponse::from)
            .toList();
    }

    @Transactional
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
}
