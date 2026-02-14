package com.ealth.codeleat.repositories;

import com.ealth.codeleat.dtos.ProfileDto;
import com.ealth.codeleat.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    public Optional<User> findByEmail(String email);

    @Query("""
     SELECT new com.ealth.codeleat.dtos.ProfileDto(
     u.firstName, u.lastName, u.username, u.profilePhotoUrl, u.bio)
     FROM User u
     WHERE u.id = :userId
     """)
    ProfileDto getUserProfile(@Param("userId") Integer user);
}
