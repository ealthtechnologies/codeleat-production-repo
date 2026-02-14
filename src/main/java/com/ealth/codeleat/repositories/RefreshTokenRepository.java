package com.ealth.codeleat.repositories;

import com.ealth.codeleat.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Ref;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    public Optional<RefreshToken> findByToken(String token);
    public Optional<RefreshToken> findByUser_Id(Integer userId);
}
