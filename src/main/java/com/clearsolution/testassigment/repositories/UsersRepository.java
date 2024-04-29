package com.clearsolution.testassigment.repositories;

import com.clearsolution.testassigment.models.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findUserEntityByEmail(String email);

    List<UserEntity> findUserEntityByBirthDateBetween(Date from, Date to);
}
