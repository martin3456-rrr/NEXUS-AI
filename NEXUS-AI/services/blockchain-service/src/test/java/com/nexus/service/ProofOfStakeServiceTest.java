package com.nexus.service;

import com.nexus.blockchain.repository.ValidatorStakeRepository;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ProofOfStakeServiceTest {

    @Test
    void whenValidatorIsSelected_thenItShouldNotBeNull() {
        ValidatorStakeRepository mockRepository = mock(ValidatorStakeRepository.class);
        ProofOfStakeService posService = new ProofOfStakeService(mockRepository);

        String validator = posService.selectNextValidator();

        assertNotNull(validator, "Wybrany walidator nie powinien być nullem.");
        assertTrue(validator.startsWith("Validator-Address-"), "Nazwa walidatora powinna mieć poprawny format.");
    }
}