package com.ealth.codeleat.services;

import com.ealth.codeleat.dtos.JwtResponseDto;
import com.ealth.codeleat.dtos.UserLoginDto;
import com.ealth.codeleat.dtos.UserSignUpDto;
import com.ealth.codeleat.entities.User;
import com.ealth.codeleat.exceptions.DuplicateEmailException;
import com.ealth.codeleat.exceptions.InvalidOperationException;
import com.ealth.codeleat.repositories.UserRepository;
import com.ealth.codeleat.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    //fields
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    //method for user Log in
    public JwtResponseDto login(UserLoginDto loginRequestDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequestDto.getEmail(),
                            loginRequestDto.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            Map<String, Object> claims = new HashMap<>();
            claims.put("email", userDetails.getUsername());
            claims.put("role", userDetails.getAuthorities().iterator().next().getAuthority());

            String jwtToken = jwtService.generateToken(claims, userDetails.getUsername());

            return new JwtResponseDto(jwtToken, "Bearer", 604800000 / 1000);
        } catch(AuthenticationException exception) {
            throw new InvalidOperationException("Some error occurred. Please try again!");
        }
    }

    //method for user Sign Up
    public void signUp(UserSignUpDto userSignUpDto) {
        final String duplicateEmailMessage = "A user with this email id already exists!";

        //check if a user with this email already exists
        Optional<User> optionalUser = userRepository.findByEmail(userSignUpDto.getEmail());
        if (optionalUser.isPresent()) {
            //check if it's an OAuth user
            User existingUser = optionalUser.get();
            if (existingUser.getOAuthProvider() != null) {
                throw new DuplicateEmailException(
                        "This email is already registered using " +
                                existingUser.getOAuthProvider() + " login. " +
                                "Please sign in with " + existingUser.getOAuthProvider() + "."
                );
            }
            throw new DuplicateEmailException(duplicateEmailMessage);
        }

        //if no user exists already, create a new user and register in the database
        User newUser = new User();
        newUser.setFirstName(userSignUpDto.getFirstName());
        newUser.setLastName(userSignUpDto.getLastName());
        newUser.setEmail(userSignUpDto.getEmail());
        newUser.setPassword(bCryptPasswordEncoder.encode(userSignUpDto.getPassword()));
        newUser.setUsername(userSignUpDto.getUsername());
        newUser.setEmailVerified(false);
        newUser.setOAuthProvider(null);

        //using database unique constraint on email as the final safety net for this
        //low contention concurrent registration scenario. no need of explicit locks.
        //we can rely on the fact that concurrent registrations will be seldom
        try {
            userRepository.save(newUser);
        } catch(DataIntegrityViolationException exception) {
            throw new DuplicateEmailException(duplicateEmailMessage);
        }
    }
}
