package gnu.project.pbl2.user.service;

import gnu.project.pbl2.auth.entity.Accessor;
import gnu.project.pbl2.common.entity.Allergy;
import gnu.project.pbl2.common.entity.Category;
import gnu.project.pbl2.common.entity.Taste;
import gnu.project.pbl2.common.error.ErrorCode;
import gnu.project.pbl2.common.exception.BusinessException;
import gnu.project.pbl2.common.repository.AllergyRepository;
import gnu.project.pbl2.common.repository.CategoryRepository;
import gnu.project.pbl2.common.repository.TasteRepository;
import gnu.project.pbl2.user.dto.request.UserOnboardRequest;
import gnu.project.pbl2.user.dto.response.UserResponseDto;
import gnu.project.pbl2.user.entity.User;
import gnu.project.pbl2.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {


    private final AllergyRepository allergyRepository;
    private final TasteRepository tasteRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public UserResponseDto saveOnboarding(
        final UserOnboardRequest request,
        final Accessor accessor
    ) {
        final User user = userRepository.findById(accessor.getUserId())
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        final List<Allergy> allergies = allergyRepository.findAllById(request.allergies());
        final List<Taste> tastes = tasteRepository.findAllById(request.tastes());
        final List<Category> categories = categoryRepository.findAllById(request.categories());

        user.updateOnboarding(allergies, tastes, categories);


        return UserResponseDto.from(user);
    }
}
