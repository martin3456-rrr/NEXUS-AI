package com.nexus.voting.controller;

import com.nexus.voting.service.EthereumVotingService;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;

@RestController
@RequestMapping("/vote")
@Tag(name = "Voting", description = "Endpoints for the Ethereum-based voting system")
public class VotingController {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(EthereumVotingService.class);
    private final EthereumVotingService ethereumVotingService;

    public VotingController(EthereumVotingService ethereumVotingService) {
        this.ethereumVotingService = ethereumVotingService;
    }

    @PostMapping("/{proposalId}")
    @Operation(summary = "Cast a vote on a proposal", description = "Sends a transaction to the Ethereum smart contract to cast a vote.")
    @ApiResponse(responseCode = "200", description = "Vote transaction sent successfully")
    @ApiResponse(responseCode = "500", description = "Error sending the transaction")
    public ResponseEntity<?> vote(@Parameter(description = "ID of the proposal to vote on", required = true) @PathVariable long proposalId) {
        try {
            String txHash = ethereumVotingService.castVote(proposalId);
            logger.info("Vote cast for proposal {}, TxHash: {}", proposalId, txHash);
            return ResponseEntity.ok(Map.of(
                    "message", "Vote transaction sent successfully!",
                    "transactionHash", txHash
            ));
        } catch (Exception e) {
            logger.error("Error casting vote: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Błąd przy wysyłaniu transakcji: " + e.getMessage()
            ));
        }
    }
}
