package com.ealth.codeleat.security;

import com.ealth.codeleat.entities.User;
import com.ealth.codeleat.exceptions.InvalidOperationException;
import com.ealth.codeleat.repositories.UserRepository;
import com.ealth.codeleat.services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class OAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserService userService;
    private final String frontendUrl = "https://codeleat.com/oauth/callback";
    private final UserRepository userRepository;

    public OAuthSuccessHandler(JwtService jwtService, UserService userService,
                               UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        if(email == null || email.isEmpty()) {
            getRedirectStrategy().sendRedirect(request, response,
                    frontendUrl + "?error=email_required");
            return;
        }

        Optional<User> optionalUser = userRepository.findByEmail(email);

        //If the user is not present register him first and then generate jwt for him
        User user = null;
        if(optionalUser.isEmpty()) {
            try {
                user = createOAuthUser(oAuth2User, email);
            } catch(DataIntegrityViolationException ex) {
                throw new InvalidOperationException("A user with this email id already exists!");
            }
        } else {
            user = optionalUser.get();
        }

        //if the user used normal sign up last time and is now trying to log in via o-auth
        if(user.getOAuthProvider() == null) {
            user.setEmailVerified(true);
            user.setOAuthProvider("GOOGLE");
            userRepository.save(user);
        }

        //Generate JWT
        String jwt = jwtService.generateToken(new HashMap<>(), user.getEmail());

        //Store the jwt in a cookie which will be automatically sent to api every time
        generateJwtCookie(jwt, response);

        //Redirect to frontend dashboard directly
        getRedirectStrategy().sendRedirect(request, response, frontendUrl);
    }

    //helper method to create o-auth user
    public User createOAuthUser(OAuth2User oAuth2User, String email) {
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setFirstName(oAuth2User.getAttribute("given_name"));
        newUser.setLastName(oAuth2User.getAttribute("family_name"));
        newUser.setOAuthProvider("GOOGLE");
        newUser.setUsername(null);
        newUser.setEmailVerified(true);
        return userRepository.save(newUser);
    }

    //helper method to generate cookie for storing jwt
    public void generateJwtCookie(String jwt, HttpServletResponse response) {
        // Manually append SameSite attribute
        response.setHeader("Set-Cookie",
                String.format("ACCESS_TOKEN=%s; Max-Age=%d; Path=/; HttpOnly; Secure; SameSite=None",
                        jwt, 15*60));
    }
}
