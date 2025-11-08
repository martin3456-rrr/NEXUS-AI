package com.nexus.blockchain.repository;

import com.nexus.blockchain.model.ValidatorStake;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ValidatorStakeRepository extends JpaRepository<ValidatorStake, String> {
}