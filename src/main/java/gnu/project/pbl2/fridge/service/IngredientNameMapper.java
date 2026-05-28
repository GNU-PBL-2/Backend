package gnu.project.pbl2.fridge.service;

import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** YOLO 감지 영문 클래스명 → DB 한글 재료명 변환 */
@Component
public class IngredientNameMapper {

    private static final Map<String, String> EN_TO_KO = Map.ofEntries(
        Map.entry("tomato",       "토마토"),
        Map.entry("carrot",       "당근"),
        Map.entry("chicken",      "닭고기"),
        Map.entry("egg",          "계란"),
        Map.entry("potato",       "감자"),
        Map.entry("rice",  "쌀"),
        Map.entry("onion",        "양파"),
        Map.entry("shrimp",       "새우"),
        Map.entry("cabbage",      "양배추"),
        Map.entry("eggplant",     "가지"),
        Map.entry("beef",         "소고기"),
        Map.entry("ginger",       "생강"),
        Map.entry("ampalaya",     "여주"),
        Map.entry("bangus",       "방어"),
        Map.entry("bellpepper",   "피망"),
        Map.entry("galunggong",   "전갱이"),
        Map.entry("green chilli", "청양고추"),
        Map.entry("hotdog",       "핫도그"),
        Map.entry("kangkong",     "공심채"),
        Map.entry("okra",         "오크라"),
        Map.entry("pechay",       "청경채"),
        Map.entry("pork",         "돼지고기"),
        Map.entry("radish",       "무"),
        Map.entry("red chili",    "홍고추"),
        Map.entry("sayote",       "차요테"),
        Map.entry("sitaw",        "강낭콩"),
        Map.entry("tilapia",      "틸라피아")
    );

    /** 영문 클래스명을 한글 재료명으로 변환한다. 매핑이 없으면 empty. */
    public Optional<String> toKorean(final String englishName) {
        return Optional.ofNullable(EN_TO_KO.get(englishName.toLowerCase()));
    }
}
