package pro.deves.privatenetwork.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pro.deves.privatenetwork.api.message.request.LoginRequest;
import pro.deves.privatenetwork.api.message.request.SignupRequest;
import pro.deves.privatenetwork.api.message.response.JwtResponse;
import pro.deves.privatenetwork.api.model.Role;
import pro.deves.privatenetwork.api.model.RoleName;
import pro.deves.privatenetwork.api.model.User;
import pro.deves.privatenetwork.api.repository.RoleRepository;
import pro.deves.privatenetwork.api.repository.UserRepository;
import pro.deves.privatenetwork.api.security.jwt.JwtProvider;

import java.util.HashSet;
import java.util.Set;

/**
 * The AuthRestAPI is a rest endpoint for signing and registering of users.
 */
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/auth")
@Slf4j
public class AuthRestAPI {

    private AuthenticationManager authenticationManager;

    private UserRepository userRepository;

    private RoleRepository roleRepository;

    private PasswordEncoder passwordEncoder;

    private JwtProvider tokenProvider;

    @Autowired
    public AuthRestAPI(AuthenticationManager authenticationManager, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginData) {
        if (loginData.getUsername() == null || loginData.getPassword() == null) {
            return ResponseEntity.badRequest().build();
        }
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginData.getUsername(), loginData.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String generatedJwtToken = tokenProvider.generateJwtToken(authentication);
        log.info("GENERATED TOKEN: " + generatedJwtToken);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        return ResponseEntity.ok(new JwtResponse(generatedJwtToken, userDetails.getUsername(), userDetails.getAuthorities()));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest singupData) {

        if (userRepository.existsByUsername(singupData.getUsername())) {
            return new ResponseEntity<>("Fail -> Username is already taken!",
                    HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByEmail(singupData.getEmail())) {
            return new ResponseEntity<>("Fail -> Email is already in use!",
                    HttpStatus.BAD_REQUEST);
        }

        User user = new User(singupData.getName(),
                singupData.getUsername(),
                singupData.getEmail(),
                passwordEncoder.encode(singupData.getPassword()));

        Set<Role> roles = new HashSet<>();

        Set<String> strRoles = singupData.getRole();
        strRoles.forEach(role -> {
            if ("admin".equals(role)) {
                Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                        .orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role admin not find."));
                roles.add(adminRole);
            } else {
                Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                        .orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role default not find."));

                roles.add(userRole);
            }
        });

        user.setRoles(roles);
        userRepository.save(user);

        return new ResponseEntity<>("User registered successfully!", HttpStatus.OK);
    }
}
