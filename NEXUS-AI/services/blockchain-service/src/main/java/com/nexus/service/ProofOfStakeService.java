package com.nexus.service;

import com.nexus.blockchain.model.ValidatorStake;
import com.nexus.blockchain.repository.ValidatorStakeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Random;

@Service
public class ProofOfStakeService {
    private final ValidatorStakeRepository repository;
    private final Map<String, Integer> validators = new TreeMap<>();
    private final Random random = new Random();

    @Autowired
    public ProofOfStakeService(ValidatorStakeRepository repository) {
        this.repository = repository;
        validators.put("Validator-Address-A", 100);
        validators.put("Validator-Address-B", 50);
        validators.put("Validator-Address-C", 75);
    }
    public String selectNextValidator() {
        List<ValidatorStake> validators = repository.findAll();
        int totalStake = validators.stream().mapToInt(ValidatorStake::getStake).sum();
        int ticket = random.nextInt(totalStake);
        int currentStake = 0;
        for (ValidatorStake entry : validators) {
            currentStake += entry.getStake();
            if (ticket < currentStake) {
                return entry.getValidatorAddress();
            }
        }
        return null;
    }
}
