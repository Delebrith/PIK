package edu.pw.eiti.pik.user;

import edu.pw.eiti.pik.base.event.FindUserEvent;
import edu.pw.eiti.pik.base.event.UserAndProjectToParticipationEvent;
import edu.pw.eiti.pik.base.event.SignUpEvent;
import edu.pw.eiti.pik.participation.ParticipationStatus;
import edu.pw.eiti.pik.user.db.AuthorityRepository;
import edu.pw.eiti.pik.user.db.UserRepository;
import edu.pw.eiti.pik.user.es.UserESRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final UserESRepository userESRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final byte[] secretKey;
    private final ApplicationEventPublisher publisher;

    public UserServiceImpl(UserRepository userRepository, UserESRepository userESRepository, AuthorityRepository authorityRepository, String secretKey, ApplicationEventPublisher publisher) {
        this.userRepository = userRepository;
        this.userESRepository = userESRepository;
        this.authorityRepository = authorityRepository;
        this.secretKey = secretKey.getBytes();
        this.publisher = publisher;
    }

    @Override
    public User getAuthenticatedUser() {
        final String email = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Principal::getName)
                .orElseThrow(IllegalStateException::new);
        return findByEmail(email).orElseThrow(IllegalStateException::new);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(userRepository.findByEmail(email));
    }

    @Override
    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    @Override
    public Optional<User> authenticate(String email, String password) {
        final Optional<User> user = findByEmail(email);
        final boolean passwordMatches =
                user.map(u -> passwordEncoder.matches(password, u.getPassword())).orElse(false);
        return passwordMatches ? user : Optional.empty();
    }


    @Override
    public UserDetails loadUserByUsername(String username) {
        return findByEmail(username).orElseThrow(() -> new UsernameNotFoundException(username));
    }

	@Override
	public Page<User> findByNameAndAuthorityName(String name, String authority, Pageable pageable) {
		return userESRepository.findByNameAndAuthorityName(name, authority, pageable);
	}

    @Override
    @EventListener
    public void addUserToProject(FindUserEvent event) {
        User user;
        if (event.getUsername() == null)
            user = getAuthenticatedUser();
        else {
            user = findByEmail(event.getUsername()).orElseThrow(UserNotFoundException::new);
            if (!user.getAuthorities().stream().map(Authority::getName)
                    .collect(Collectors.toList()).contains(Authorities.TEACHER))
                throw new InvalidAuthorityException();
        }
        publisher.publishEvent(new UserAndProjectToParticipationEvent(event.getProject(), user, event.getStatus()));

    }

    @Override
    public Page<User> findByNameAndAuthorityList(String name, List<Authorities> authorities, Pageable pageable) {
        String authoritiesString = String.join(" ", authorities.stream().map(Authorities::toString).collect(Collectors.toList()));
        return userESRepository.findByNameAndAuthorityList(name,
                authoritiesString, pageable);
    }

    @Override
    public Page<User> findByAuthorityList(List<Authorities> authorities, Pageable pageable) {
        List<Authority> authorityList = authorityRepository.findAllByNameIn(authorities);
        List<User> users = userRepository.findAllByAuthoritiesIn(authorityList);
        return new PageImpl<>(users, pageable, users.size());
    }

    @Override
    @Transactional
    public User createUser(User user) {
        user.setParticipations(new ArrayList<>());
        String password = generatePassword();
        user.setPassword(password);
        publisher.publishEvent(new UserCreatedEvent(user.copy()));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userESRepository.save(user);
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findUser(Long userId) {
        return userRepository.findById(userId);
    }

    @Override
    @Transactional
    public User updateUser(User user) {
        try {
            User updated = userRepository.findById(user.getId()).orElseThrow(UserNotFoundException::new);
            user.setParticipations(updated.getParticipations());
            user.setPassword(updated.getPassword());
            userESRepository.save(user);
            return userRepository.save(user);
        } catch (IllegalArgumentException e) {
            throw new InvalidUserDataException();
        }
    }

    @Override
    public List<Authority> getAuthorities() {
        return authorityRepository.findAll();
    }

    @Override
    public void deleteUser(Long id) {
        User deleted = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        userRepository.delete(deleted);
        userESRepository.delete(deleted);
    }

    @EventListener
    public void handleEvent(SignUpEvent event) {
        User user = getAuthenticatedUser();
        publisher.publishEvent(new UserAndProjectToParticipationEvent(event.getProject(), user,
                ParticipationStatus.WAITING_FOR_ACCEPTANCE));
    }

    private String generatePassword() {
        return RandomStringUtils.randomAlphanumeric(10);
    }
}
