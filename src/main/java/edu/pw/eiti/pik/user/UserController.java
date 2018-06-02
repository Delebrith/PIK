package edu.pw.eiti.pik.user;

import edu.pw.eiti.pik.base.config.security.AuthorizationTokenDto;
import edu.pw.eiti.pik.base.config.web.ErrorDto;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    	
    	return queryResult.stream().map(userMapper::toDto).collect(Collectors.toList());
    }


    @ApiOperation(value = "Get users by name who have given role (up to number)")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Always")
    })
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping(path = "/user/filterAndFind/{pageSize}/{pageNumber}")
    List<UserDto> getUsersByRolesAndName(
            @RequestParam String name,
            @PathVariable Integer pageNumber,
            @PathVariable Integer pageSize,
            @RequestParam(required = false) Boolean student,
            @RequestParam(required = false) Boolean teacher,
            @RequestParam(required = false) Boolean thirdParty,
            @RequestParam(required = false) Boolean authority,
            @RequestParam(required = false) Boolean employer,
            @RequestParam(required = false) Boolean admin
            ) {
        List<Authorities> authorities = getAuhorityList(student, teacher, thirdParty,
                authority, employer, admin);
        Page<User> queryResult;
        if (name != null && !name.isEmpty()) {
            queryResult = userService.findByNameAndAuthorityList(
                    name, authorities, PageRequest.of(pageNumber, pageSize));
        } else {
            queryResult = userService.findByAuthorityList(authorities, PageRequest.of(pageNumber, pageSize));
        }
        return queryResult.stream().map(userMapper::toDto).collect(Collectors.toList());
    }


    @ApiOperation(value = "Create user", notes = "Returns information on created user's location")
    @ApiResponses({
            @ApiResponse(code = 201, message = "If user was successfully created"),
            @ApiResponse(code = 403, message = "If user did not log in previously", response = ErrorDto.class)
    })
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping(path = "/user/create")
    ResponseEntity createUser(@RequestBody UserDto userDto) throws URISyntaxException {
        User created = userService.createUser(userMapper.fromDto(userDto));
        return ResponseEntity.created(new URI("/user/find/" + created.getId())).build();
    }


    @ApiOperation(value = "update user", notes = "Returns information on updated user")
    @ApiResponses({
            @ApiResponse(code = 200, message = "If user was successfully created"),
            @ApiResponse(code = 403, message = "If user did not log in previously", response = ErrorDto.class),
            @ApiResponse(code = 405, message = "If data is invalid", response = ErrorDto.class)
    })
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping(path = "/user/modify")
    ResponseEntity modifyUser(@RequestBody UserDto userDto) {
        return ResponseEntity.ok(userMapper.toDto(
                userService.updateUser(userMapper.fromDto(userDto))));
    }


    @ApiOperation(value = "find user", notes = "Returns information on user with given id")
    @ApiResponses({
            @ApiResponse(code = 200, message = "If user was found"),
            @ApiResponse(code = 403, message = "If user did not log in previously", response = ErrorDto.class),
            @ApiResponse(code = 404, message = "If user was not found", response = ErrorDto.class)
    })
    @GetMapping(path = "/user/{id}/find")
    UserDto findUser(@PathVariable long userId) {
        return userMapper.toDto(userService.findUser(userId).orElseThrow(this::userNotFound));
    }


    @ApiOperation(value = "get authorities", notes = "Returns information on all authorities")
    @ApiResponses({
            @ApiResponse(code = 200, message = "If user was found"),
            @ApiResponse(code = 403, message = "If user did not log in previously", response = ErrorDto.class)
    })
    @GetMapping(path = "/user/authorities")
    List<AuthorityDto> getAuthorities() {
        return userService.getAuthorities().stream()
                .map(userMapper::toDto).collect(Collectors.toList());
    }

    private List<Authorities> getAuhorityList(Boolean student, Boolean teacher, Boolean thirdParty,
                                              Boolean authority, Boolean employer, Boolean admin) {
        List<Authorities> authorities = new ArrayList<>();
        if (null != student && student) authorities.add(Authorities.STUDENT);
        if (null != teacher && teacher) authorities.add(Authorities.TEACHER);
        if (null != thirdParty && thirdParty) authorities.add(Authorities.THIRD_PARTY);
        if (null != authority && authority) authorities.add(Authorities.AUTHORITY);
        if (null != employer && employer) authorities.add(Authorities.EMPLOYER);
        if (null != admin && admin) authorities.add(Authorities.ADMIN);
        return authorities;
    }

    private UserNotFoundException userNotFound() {
        return new UserNotFoundException();
    }
}
