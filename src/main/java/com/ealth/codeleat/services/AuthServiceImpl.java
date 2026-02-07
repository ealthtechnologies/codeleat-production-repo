package com.ealth.codeleat.services;

import com.ealth.codeleat.dtos.*;
import com.ealth.codeleat.entities.User;
import com.ealth.codeleat.exceptions.DuplicateEmailException;
import com.ealth.codeleat.exceptions.InvalidOperationException;
import com.ealth.codeleat.repositories.UserRepository;
import com.ealth.codeleat.security.JwtService;
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

import javax.management.InvalidAttributeValueException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    //fields
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RedisTemplate<String, Object> stringRedisTemplate;
    private final OtpGenerator otpGenerator;
    private final ResendEmailService resendEmailService;
    private final VerificationIdGenerator verificationIdGenerator;

    //method for user Log in
    @Transactional
    public JwtResponseDto login(UserLoginDto loginRequestDto) {
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
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new InvalidOperationException("No user found with the given email!"));

            if(!user.isEmailVerified()) {
                //generate a short-lived verification id that can be sent to frontend for verifying the otp later
                String verificationId = verificationIdGenerator.generateVerificationId();
                sendOtpEmail(user.getEmail(), verificationId, user.getId());
                return new JwtResponseDto(verificationId, "Email Id not verified", 300);
            }

            Map<String, Object> claims = new HashMap<>();
            claims.put("role", userDetails.getAuthorities().iterator().next().getAuthority());

            String jwtToken = jwtService.generateToken(claims, userDetails.getUsername());

            return new JwtResponseDto(jwtToken, "Bearer", 604800000 / 1000);
        } catch(AuthenticationException exception) {
            throw new InvalidOperationException("Invalid Credentials. Please try again!");
        }
    }

    //method for user Sign Up
    @Transactional
    public String signUp(UserSignUpDto userSignUpDto) {
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

        //generate a short-lived verification id that can be sent to frontend for verifying the otp later
        String verificationId = verificationIdGenerator.generateVerificationId();

        //verify the email first by sending otp and then save the user in database
        sendOtpEmail(userSignUpDto.getEmail(), verificationId, newUser.getId());

        return verificationId;
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
}
