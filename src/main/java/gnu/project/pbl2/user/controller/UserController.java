package gnu.project.pbl2.user.controller;

import gnu.project.pbl2.auth.aop.Auth;
import gnu.project.pbl2.auth.aop.OnlyUser;
import gnu.project.pbl2.auth.entity.Accessor;
import gnu.project.pbl2.user.controller.docs.UserDocs;
import gnu.project.pbl2.user.dto.request.UserOnboardRequest;
import gnu.project.pbl2.user.dto.response.UserResponseDto;
import gnu.project.pbl2.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController implements UserDocs {

    private final UserService userService;

    @OnlyUser
    @PostMapping("/onboard")
    public ResponseEntity<UserResponseDto> saveOnboard(
        @RequestBody final UserOnboardRequest request,
        @Auth final Accessor accessor
    ){
        return ResponseEntity.ok(userService.saveOnboarding(request,accessor));
    }

    @OnlyUser
    @GetMapping()
    public ResponseEntity<UserResponseDto> getUserInfo(
        @Auth final Accessor accessor
    ){
        return ResponseEntity.ok(
            userService.getUserInfo(accessor)
        );
    }
    @OnlyUser
    @DeleteMapping()
    public ResponseEntity<String> deleteUser(
        @Auth final Accessor accessor
    ){
        return ResponseEntity.ok(
            userService.deleteUser(accessor)
        );
    }
}
