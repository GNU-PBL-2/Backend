public interface FridgeRepository extends JpaRepository<Fridge, Long> {

    List<Fridge> findByMember_MemberId(Long memberId);

}