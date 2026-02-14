package com.ealth.codeleat.services;

import com.ealth.codeleat.dtos.*;
import com.ealth.codeleat.entities.RefreshToken;
import com.ealth.codeleat.entities.User;
import com.ealth.codeleat.enums.AccountStatus;
import com.ealth.codeleat.exceptions.DuplicateEmailException;
import com.ealth.codeleat.exceptions.InvalidOperationException;
import com.ealth.codeleat.repositories.RefreshTokenRepository;
import com.ealth.codeleat.repositories.UserRepository;
import com.ealth.codeleat.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    //Fields
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RedisTemplate<String, Object> stringRedisTemplate;
    private final OtpGenerator otpGenerator;
    private final ResendEmailService resendEmailService;
    private final VerificationIdGenerator verificationIdGenerator;
    private final RefreshTokenRepository refreshTokenRepository;

    //Method for user Log in
    @Transactional
    public AuthResponseDto login(UserLoginDto loginRequestDto, HttpServletResponse response) {
        // Check if user exists and how they registered
        User user = userRepository.findByEmail(loginRequestDto.getEmail())
                .orElse(null);

        if (user != null && user.getPassword() == null) {
            // User registered via OAuth
            throw new InvalidOperationException(
                    "This account was created using Google Sign-In. Please use 'Sign in with Google' to continue."
            );
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequestDto.getEmail(),
                            loginRequestDto.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            //check if the email of the user is verified or not. if not make him verify it first.
            //do not log him in
            user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new InvalidOperationException("No user found with the given email!"));

            if(!user.isEmailVerified()) {
                //generate a short-lived verification id that can be sent to frontend for verifying the otp later
                String verificationId = verificationIdGenerator.generateVerificationId();
                sendOtpEmail(user.getEmail(), verificationId, user.getId());
                return new AuthResponseDto(AccountStatus.EMAIL_NOT_VERIFIED, verificationId, "An account with this email exists but the email is not verified. We have sent an otp via email to verify your account.");
            }

            Map<String, Object> claims = new HashMap<>();
            claims.put("role", userDetails.getAuthorities().iterator().next().getAuthority());

            String jwtToken = jwtService.generateToken(claims, userDetails.getUsername());

            // Manually set Access Token
            response.setHeader("Set-Cookie",
                    String.format("ACCESS_TOKEN=%s; Max-Age=%d; Path=/; HttpOnly; Secure; SameSite=None",
                            jwtToken, 15 * 60));

            String refreshToken = jwtService.generateRefreshToken(user.getEmail());

            // Calculate expiry date (same as in JWT)
            Instant expiryDate = Instant.now().plus(7, ChronoUnit.DAYS);

            // Create refresh token entity
            RefreshToken tokenEntity = new RefreshToken(refreshToken, user, expiryDate);

            // Save it in DB
            refreshTokenRepository.save(tokenEntity);

            //Manually set Refresh Token
            response.addHeader("Set-Cookie",
                    String.format("REFRESH_TOKEN=%s; Max-Age=%d; Path=/auth/refresh; HttpOnly; Secure; SameSite=None",
                            refreshToken, 7*24*60*60));

            return new AuthResponseDto(AccountStatus.SUCCESS, "Login Successful");
        } catch(AuthenticationException exception) {
            throw new InvalidOperationException("Invalid Credentials. Please try again!");
        }
    }

    //Method for user Sign Up
    @Transactional
    public AuthResponseDto signUp(UserSignUpDto userSignUpDto) {
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
            } else if (!existingUser.isEmailVerified()) {
                // Account exists but not verified
                String verificationId = verificationIdGenerator.generateVerificationId();
                String otp = otpGenerator.generateNumericOtp();

                stringRedisTemplate.opsForValue().set(
                        "otp:" + verificationId, new OtpEntry(existingUser.getId(), otp),
                        5,
                        TimeUnit.MINUTES
                );

                resendEmailService.sendOtpEmail(existingUser.getEmail(), otp);

                return new AuthResponseDto(AccountStatus.EMAIL_NOT_VERIFIED, verificationId, "An account with this email exists but the email is not verified. We have sent an otp via email to verify your account.");
            } else {
                // Account exists and is verified
                throw new InvalidOperationException(
                        "An account with this email already exists. Please login instead."
                );
            }
        }

        //If no user exists already, create a new user and register in the database
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

        //generate a short-lived verification id that can be sent to frontend for verifying the otp later
        String verificationId = verificationIdGenerator.generateVerificationId();

        //verify the email first by sending otp and then save the user in database
        sendOtpEmail(userSignUpDto.getEmail(), verificationId, newUser.getId());

        return new AuthResponseDto(AccountStatus.EMAIL_NOT_VERIFIED, verificationId, "An otp has been sent to your email for verification.");
    }

    //helper method to send otp email
    public void sendOtpEmail(String toEmail, String verificationId, Integer userId) {
        final String redisOtpNamespace = "otp:";

        //generate a 6 digit otp for the user
        String otp = otpGenerator.generateNumericOtp();

        //store the otp in redis for future validation
        stringRedisTemplate.opsForValue().set(redisOtpNamespace + verificationId, new OtpEntry(userId, otp) , 5, TimeUnit.MINUTES);

        //send the email using RESEND email service
        resendEmailService.sendOtpEmail(toEmail, otp);
    }

    //method to verify otp
    @Transactional
    public void verifyOtp(OtpVerificationDto otpVerificationDto) {
        String verificationId = otpVerificationDto.getVerificationId();
        String enteredOtp = otpVerificationDto.getEnteredOtp();

        //Get the otp from redis using the verification id from the front end
        OtpEntry otpEntry = (OtpEntry) stringRedisTemplate.opsForValue().get("otp:" + verificationId);

        //If the returned String is null, that means the time for verification expired
        if(otpEntry == null) {
            throw new InvalidOperationException("Oops! Time for verification expired. Try again with another otp.");
        }

        String savedOtp = otpEntry.getOtp();

        //Check if the enteredOtp and savedOtp is the same
        if(!enteredOtp.equals(savedOtp)) {
            throw new InvalidOperationException("Oops! Entered otp did not match. Please try again!");
        }

        //If all the above check pass, delete the otp from redis and let the frontend redirect the user to login page
        stringRedisTemplate.delete("otp:" + verificationId);

        //Mark the email as verified for the user
        User userForVerification = userRepository.findById(otpEntry.getUserId())
                .orElseThrow(() -> new InvalidOperationException("No user found!"));

        userForVerification.setEmailVerified(true);
        userRepository.save(userForVerification);
    }

    //Method to handle Forgot Password
    @Override
    public void forgotPassword(ForgotPasswordDto forgotPasswordDto) {
        final String email = forgotPasswordDto.getEmail();
        final String verificationLink = "https://codeleat.com/reset-password";

        //Check if a user with the given email id exists in the database or not
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidOperationException("No user with the given email id exists!"));

        //Generate a verificationId
        String verificationId = verificationIdGenerator.generateVerificationId();

        //Store the verification id in Redis to retrieve it later
        stringRedisTemplate.opsForValue().set("pwd_reset:" + verificationId, user.getEmail(), 5, TimeUnit.MINUTES);

        //Send email with verification link
        if(user.getPassword() == null) {
            resendEmailService.sendAddPasswordEmail(user.getEmail(), verificationLink + "?verificationId=" + verificationId, user.getOAuthProvider());
        } else {
            resendEmailService.sendResetPasswordEmail(user.getEmail(), verificationLink + "?verificationId=" + verificationId);
        }
    }

    //Method to handle Reset Password
    public void resetPassword(ResetPasswordDto resetPasswordDto) {
        String verificationId = resetPasswordDto.getVerificationId();
        String newPassword = resetPasswordDto.getNewPassword();

        //Fetch the user email for the received verification id from Redis
        String userEmail = (String) stringRedisTemplate.opsForValue().get("pwd_reset:" + verificationId);

        //If there is no such key, reset time expired
        if(userEmail == null) {
            throw new InvalidOperationException("Invalid or Expired verification link. Please try again!");
        }

        User userForPasswordReset = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new InvalidOperationException("No user with the given email if found!"));

        //Set the new password for the user
        userForPasswordReset.setPassword(bCryptPasswordEncoder.encode(newPassword));
        userRepository.save(userForPasswordReset);

        //Delete the verification id from redis
        stringRedisTemplate.delete("pwd_reset:" + verificationId);
    }

    //Method to resend otp
    public AuthResponseDto resendOtp(ResendOtpDto resendOtpDto) {
        String oldVerificationId = resendOtpDto.getVerificationId();
        String email = resendOtpDto.getEmail();

        OtpEntry otpEntry = (OtpEntry) stringRedisTemplate.opsForValue().get("otp:" + oldVerificationId);

        if(otpEntry != null) {
            //delete the old verification id from redis
            stringRedisTemplate.delete("otp:" + oldVerificationId);
        }

        //Check if a user with the given email id exists
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidOperationException("No user with the given email id exists!"));

        //Check if the account of the user is already verified
        if(user.isEmailVerified()) {
            throw new InvalidOperationException("Your account is already verified. You can login with your credentials!");
        }

        //Generate a new verification id and a new otp for the user
        String verificationId = verificationIdGenerator.generateVerificationId();

        //Send the otp email
        sendOtpEmail(user.getEmail(), verificationId, user.getId());

        return new AuthResponseDto(AccountStatus.EMAIL_NOT_VERIFIED, verificationId, "An otp has been sent to your email for verification.");
    }

    //Method to set cookies after o-auth redirect
    public void setCookies(String verificationId, HttpServletResponse response) {
        //Extract the jwt and refresh token from redis
        Tokens tokens = (Tokens) stringRedisTemplate.opsForValue().get("o-auth:" + verificationId);

        if(tokens == null) {
            throw new InvalidOperationException("Time for tokens expired");
        }

        String jwt = tokens.getAccessToken();
        String refreshToken = tokens.getRefreshToken();

        //Manually set Access Token Cookie
        response.setHeader("Set-Cookie",
                String.format("ACCESS_TOKEN=%s; Max-Age=%d; Path=/; HttpOnly; Secure; SameSite=None",
                        jwt, 15 * 60));

        //Manually set Refresh Token Cookie
        response.addHeader("Set-Cookie",
                String.format("REFRESH_TOKEN=%s; Max-Age=%d; Path=/auth/refresh; HttpOnly; Secure; SameSite=None",
                        refreshToken, 7*24*60*60));

        //Delete the tokens from redis
        stringRedisTemplate.delete("o-auth:" + verificationId);
    }
}
