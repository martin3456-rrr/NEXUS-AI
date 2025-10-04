package com.nexus.service;

import com.nexus.service.ProofOfStakeService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProofOfStakeServiceTest {

    @Test
    void whenValidatorIsSelected_thenItShouldNotBeNull() {
        ProofOfStakeService posService = new ProofOfStakeService();
        String validator = posService.selectNextValidator();
        assertNotNull(validator, "Wybrany walidator nie powinien być nullem.");
        assertTrue(validator.startsWith("Validator-Address-"), "Nazwa walidatora powinna mieć poprawny format.");
    }
}