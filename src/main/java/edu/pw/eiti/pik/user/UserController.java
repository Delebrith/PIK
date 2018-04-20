package edu.pw.eiti.pik.user;

import edu.pw.eiti.pik.config.security.AuthorizationTokenDto;
import edu.pw.eiti.pik.config.web.ErrorDto;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/user")
class UserController {

    private final UserService userService;
    private final UserMapper userMapper = UserMapper.getInstance();

    UserController(UserService userNotFound) {
        this.userService = userNotFound;
    }

    @ApiOperation(value = "Log in as existing user", notes = "Logs in existing user")
    @ApiResponses({
            @ApiResponse(code = 200, message = "If user successfully logs in with valid credentials"),
            @ApiResponse(code = 404, message = "If user provided invalid credentials", response = ErrorDto.class)
    })
    @PostMapping(path = "/login")
    AuthorizationTokenDto login(
            @ApiParam(value = "User's credentials", required = true)
            @RequestBody UserCredentialsDto userCredentialsDto) {
        User user = userService
                        .authenticate(userCredentialsDto.getEmail(), userCredentialsDto.getPassword())
                        .orElseThrow(this::userNotFound);
        return new AuthorizationTokenDto(userService.generateToken(user));
    }

    @ApiOperation(value = "Log in as existing user", notes = "Returns information on currently logged in user")
    @ApiResponses({
            @ApiResponse(code = 200, message = "If user was successfully logged in"),
            @ApiResponse(code = 403, message = "If user did not log in previously", response = ErrorDto.class)
    })
    @GetMapping(path = "/me")
    UserDto me() {
        User user = userService.getAuthenticatedUser();
        return userMapper.toDto(user);
    }

    private UserNotFoundException userNotFound() {
        return new UserNotFoundException();
    }
}
