package com.ealth.codeleat.security;

import com.ealth.codeleat.dtos.Tokens;
import com.ealth.codeleat.entities.RefreshToken;
import com.ealth.codeleat.entities.User;
import com.ealth.codeleat.exceptions.InvalidOperationException;
import com.ealth.codeleat.repositories.RefreshTokenRepository;
import com.ealth.codeleat.repositories.UserRepository;
import com.ealth.codeleat.services.UserService;
import com.ealth.codeleat.services.VerificationIdGenerator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class OAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserService userService;
    private final String frontendUrl = "https://autoloading-postmedieval-darcie.ngrok-free.dev";
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final VerificationIdGenerator verificationIdGenerator;


    public OAuthSuccessHandler(JwtService jwtService, UserService userService,
                               UserRepository userRepository, RefreshTokenRepository refreshTokenRepository,
                               RedisTemplate redisTemplate, VerificationIdGenerator verificationIdGenerator) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.redisTemplate = redisTemplate;
        this.verificationIdGenerator = verificationIdGenerator;
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

        //If the user used normal sign up last time and is now trying to log in via o-auth
        if(user.getOAuthProvider() == null) {
            user.setEmailVerified(true);
            user.setOAuthProvider("GOOGLE");
            userRepository.save(user);
        }

        //Generate JWT and Access Token
        String jwt = jwtService.generateToken(new HashMap<>(), user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        //Calculate expiry date (same as in JWT)
        Instant expiryDate = Instant.now().plus(7, ChronoUnit.DAYS);

        //Create refresh token entity
        RefreshToken tokenEntity = new RefreshToken(refreshToken, user, expiryDate);

        //Save it in DB
        refreshTokenRepository.save(tokenEntity);

        String verificationId = verificationIdGenerator.generateVerificationId();

        //Store the token in redis for setting cookies later
        redisTemplate.opsForValue().set("o-auth:" + verificationId, new Tokens(jwt, refreshToken), 1, TimeUnit.MINUTES);

        //Redirect to frontend dashboard directly
        getRedirectStrategy().sendRedirect(request, response, frontendUrl + "/auth-callback" + "?verificationId=" + verificationId);
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


}
