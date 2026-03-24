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

    @PostMapping
    public ResponseEntity<String> addIngredient(
            @RequestBody FridgeCreateRequest request
    ) {
        fridgeService.addIngredient(request);
        return ResponseEntity.ok("재료 추가 완료");
    }
}