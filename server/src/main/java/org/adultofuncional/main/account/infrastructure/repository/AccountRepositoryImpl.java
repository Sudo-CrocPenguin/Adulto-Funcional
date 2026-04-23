package com.tuapp.account.infrastructure.repository;

import com.tuapp.account.domain.model.Account;
import com.tuapp.account.domain.repository.AccountRepository;
import com.tuapp.account.infrastructure.persistence.entity.AccountEntity;
import com.tuapp.account.infrastructure.persistence.mapper.AccountMapper;
import com.tuapp.account.infrastructure.persistence.repository.SpringAccountJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class AccountRepositoryImpl implements AccountRepository {

    private final SpringAccountJpaRepository jpaRepository;
    private final AccountMapper mapper;

    public AccountRepositoryImpl(SpringAccountJpaRepository jpaRepository,
     AccountMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Account save(Account account) {
        AccountEntity entity = mapper.toEntity(account);
        AccountEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Account> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Account> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(mapper::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}