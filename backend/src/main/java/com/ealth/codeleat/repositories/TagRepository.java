package com.ealth.codeleat.repositories;

import com.ealth.codeleat.entities.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Integer> {
    public Optional<Tag> findByName(String name);
}
