@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fridge")
public class FridgeController {

    private final FridgeService fridgeService;

    @GetMapping("/{memberId}")
    public ResponseEntity<List<FridgeResponse>> getFridge(
            @PathVariable Long memberId
    ) {
        return ResponseEntity.ok(fridgeService.getFridgeByMemberId(memberId));
    }
}