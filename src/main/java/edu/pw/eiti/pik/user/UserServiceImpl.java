package edu.pw.eiti.pik.user;

import edu.pw.eiti.pik.base.event.EmailParticipationCreationEvent;
import edu.pw.eiti.pik.base.event.AuthenticatedParticipationCreationEvent;
import edu.pw.eiti.pik.base.event.FindUserEvent;
import edu.pw.eiti.pik.base.event.UserAndProjectToParticipationEvent;
import edu.pw.eiti.pik.user.db.UserRepository;
import edu.pw.eiti.pik.user.es.UserESRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.hibernate.mapping.Collection;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
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
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final UserESRepository userESRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final byte[] secretKey;
    private final ApplicationEventPublisher publisher;

    public UserServiceImpl(UserRepository userRepository, UserESRepository userESRepository, String secretKey, ApplicationEventPublisher publisher) {
        this.userRepository = userRepository;
        this.userESRepository = userESRepository;
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
    public void addUser(FindUserEvent event) {
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
}
