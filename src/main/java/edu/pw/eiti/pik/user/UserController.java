package edu.pw.eiti.pik.user;

import edu.pw.eiti.pik.base.config.security.AuthorizationTokenDto;
import edu.pw.eiti.pik.base.config.web.ErrorDto;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    @PostMapping(path = "/user/login")
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
    @GetMapping(path = "/user/me")
    UserDto me() {
        User user = userService.getAuthenticatedUser();
        return userMapper.toDto(user);
    }

    @ApiOperation(value = "Get top users by name who have given role (up to number)")
    @ApiResponses({
    	@ApiResponse(code = 200, message = "Always")
    })
    @GetMapping(path = "/user/{authority}/{name}/{number}")
    List<UserDto> getUsersByRoleAndName(
    		@PathVariable String authority,
    		@PathVariable String name,
    		@PathVariable int number)
    {
    	Page<User> queryResult = userService.findByNameAndAuthorityName(
    			name.toLowerCase(), authority, PageRequest.of(0, number));
	    ArrayList<UserDto> listToReturn = new ArrayList<>(); 
    	for (User u : queryResult)
    		listToReturn.add(userMapper.toDto(u));
    	
    	return listToReturn;
    }
    
    private UserNotFoundException userNotFound() {
        return new UserNotFoundException();
    }
}
