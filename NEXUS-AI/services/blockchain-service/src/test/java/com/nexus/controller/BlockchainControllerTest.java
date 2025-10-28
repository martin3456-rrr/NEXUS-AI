package com.nexus.controller;

import com.nexus.blockchain.model.Block;
import com.nexus.service.CryptographyService;
import com.nexus.service.ProofOfStakeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BlockchainController.class)
@ActiveProfiles("test")
class BlockchainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Mockujemy serwisy, aby odizolować kontroler
    @MockBean
    private ProofOfStakeService posService;

    @MockBean
    private CryptographyService cryptoService;

    @BeforeEach
    void setUp() {
        // Resetujemy łańcuch przed każdym testem
        BlockchainController.blockchain.clear();
        BlockchainController.blockchain.add(new Block("Genesis Block", "0", "genesis", "genesis-sig"));
    }

    @Test
    void getBlockchain_shouldReturnGenesisBlock() throws Exception {
        mockMvc.perform(get("/api/blockchain/chain"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].data", is("Genesis Block")));
    }

    @Test
    void addBlock_shouldAddNewBlock() throws Exception {
        // Given
        String blockData = "Nowy blok danych";
        String mockSignature = "mock-signature-123";
        String mockPublicKey = "mock-public-key-abc";

        when(cryptoService.getPublicKey()).thenReturn(mockPublicKey);
        when(cryptoService.sign(anyString())).thenReturn(mockSignature);

        // When & Then
        mockMvc.perform(post("/api/blockchain/add")
                        .contentType(MediaType.APPLICATION_JSON) // Lub TEXT_PLAIN, zależy jak wysyłasz dane
                        .content(blockData))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", is(blockData)))
                .andExpect(jsonPath("$.signature", is(mockSignature)))
                .andExpect(jsonPath("$.validator", is(mockPublicKey)))
                .andExpect(jsonPath("$.previousHash").value(BlockchainController.blockchain.get(0).getHash()));

        // Sprawdź, czy blok został dodany do statycznej listy
        assertThat(BlockchainController.blockchain).hasSize(2);
    }

    @Test
    void addBlock_shouldReturn500OnError() throws Exception {
        // Given
        String blockData = "Błędne dane";
        when(cryptoService.sign(anyString())).thenThrow(new RuntimeException("Błąd podpisu"));

        // When & Then
        mockMvc.perform(post("/api/blockchain/add")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(blockData))
                .andExpect(status().isInternalServerError());
    }
}