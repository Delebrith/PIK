package edu.pw.eiti.pik.user;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureDataJpa
public class UserServiceImplTest {

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private UserServiceImpl userService;

    private User mockUser;

    @Before
    public void init() {
        List<Authority> authorities = new ArrayList<>();
        authorities.add(Authority.builder().name(Authorities.EMPLOYER).build());
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        mockUser = User.builder()
                .email("user@mail.com")
                .password(passwordEncoder.encode("password"))
                .authorities(authorities)
                .build();
    }

    @Test
    @WithMockUser(username = "user@mail.com")
    public void getAuthenticatedUser() {
        when(userRepository.findByEmail("user@mail.com"))
                .thenReturn(mockUser);
        assertEquals("user@mail.com", userService.getAuthenticatedUser().getEmail());
    }

    @Test
    public void findByEmailUserExists() {
        when(userRepository.findByEmail(mockUser.getEmail()))
                .thenReturn(mockUser);
        assertEquals(userService.findByEmail(mockUser.getEmail()).get(), mockUser);
    }

    @Test
    public void findByEmailUserDoesNotExists() {
        when(userRepository.findByEmail(any()))
                .thenReturn(null);
        assertEquals(Optional.empty(), userService.findByEmail("some@email.com"));
    }


    @Test
    public void authenticateValidCredentials() {
        when(userRepository.findByEmail(mockUser.getEmail())).thenReturn(mockUser);
        assertEquals(mockUser, userService.authenticate(mockUser.getEmail(), "password").get());
    }

    @Test
    public void authenticateInvalidCredentials() {
        when(userRepository.findByEmail(any())).thenReturn(null);
        assertEquals(Optional.empty(), userService.authenticate(mockUser.getEmail(), mockUser.getPassword()));
    }

    @Test
    public void loadUserByUsernameExistingUser() {
        when(userRepository.findByEmail(mockUser.getEmail())).thenReturn(mockUser);
        assertEquals(mockUser, userService.findByEmail(mockUser.getEmail()).get());
    }


    @Test(expected = UsernameNotFoundException.class)
    public void loadUserByUsernameNonExistingUser() {
        when(userRepository.findByEmail(mockUser.getUsername())).thenReturn(null);
        userService.loadUserByUsername(mockUser.getUsername());
    }
}