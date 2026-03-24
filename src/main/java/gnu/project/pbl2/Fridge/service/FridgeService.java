@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FridgeService {

    private final FridgeRepository fridgeRepository;
    private final MemberRepository memberRepository;
    private final IngredientRepository ingredientRepository;

    public List<FridgeResponse> getFridgeByMemberId(Long memberId) {
        return fridgeRepository.findAllByMemberId(memberId)
                .stream()
                .map(FridgeResponse::from)
                .toList();
    }

    @Transactional
    public FridgeResponse addIngredient(FridgeCreateRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Ingredient ingredient = ingredientRepository.findById(request.getIngredientId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 재료입니다."));

        Fridge fridge = new Fridge(
                member,
                ingredient,
                request.getQuantity(),
                request.getUnit(),
                request.getExpiryDate()
        );

        fridgeRepository.save(fridge);

        return FridgeResponse.from(fridge);
    }
}