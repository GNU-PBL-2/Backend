@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FridgeService {

    private final FridgeRepository fridgeRepository;

    public List<FridgeResponse> getFridgeByMemberId(Long memberId) {
        return fridgeRepository.findAllByMemberId(memberId)
                .stream()
                .map(FridgeResponse::from)
                .toList();
    }
}