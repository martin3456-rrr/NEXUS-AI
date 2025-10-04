package com.nexus.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.TreeMap;
import java.util.Random;

@Service
public class ProofOfStakeService {
    // In a real system, stakes would be managed more dynamically.
    private final Map<String, Integer> validators = new TreeMap<>();
    private final Random random = new Random();

    public ProofOfStakeService() {
        // Dummy validators with stakes
        validators.put("Validator-Address-A", 100);
        validators.put("Validator-Address-B", 50);
        validators.put("Validator-Address-C", 75);
    }

    public String selectNextValidator() {
        int totalStake = validators.values().stream().mapToInt(Integer::intValue).sum();
        int ticket = random.nextInt(totalStake);
        int currentStake = 0;
        for (Map.Entry<String, Integer> entry : validators.entrySet()) {
            currentStake += entry.getValue();
            if (ticket < currentStake) {
                return entry.getKey();
            }
        }
        return null; // Should not happen
    }
}
