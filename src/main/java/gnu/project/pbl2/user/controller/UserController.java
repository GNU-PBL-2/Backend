package gnu.project.pbl2.user.controller;

import gnu.project.pbl2.auth.aop.Auth;
import gnu.project.pbl2.auth.aop.OnlyUser;
import gnu.project.pbl2.auth.entity.Accessor;
import gnu.project.pbl2.user.dto.request.UserOnboardRequest;
import gnu.project.pbl2.user.dto.response.UserResponseDto;
import gnu.project.pbl2.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1")
public class UserController {

    private final UserService userService;

    @OnlyUser
    @PostMapping("/onboard")
    public ResponseEntity<UserResponseDto> saveOnboard(
        @RequestBody final UserOnboardRequest request,
        @Auth final Accessor accessor
    ){
        return ResponseEntity.ok(userService.saveOnboarding(request,accessor));
    }

}
