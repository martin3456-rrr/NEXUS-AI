package com.nexus.voting.controller;

import com.nexus.voting.service.EthereumVotingService;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/vote")
@Tag(name = "Voting", description = "Endpoints for the Ethereum-based voting system")
public class VotingController {
    private final EthereumVotingService ethereumVotingService;

    public VotingController(EthereumVotingService ethereumVotingService) {
        this.ethereumVotingService = ethereumVotingService;
    }

    @PostMapping("/{proposalId}")
    @Operation(summary = "Cast a vote on a proposal", description = "Sends a transaction to the Ethereum smart contract to cast a vote. NOTE: This functionality is currently a placeholder.")
    @ApiResponse(responseCode = "200", description = "Vote transaction sent successfully (placeholder response)")
    @ApiResponse(responseCode = "500", description = "Error sending the transaction")
    public String vote(@Parameter(description = "ID of the proposal to vote on", required = true) @PathVariable long proposalId) {
        try {
            return "Vote for proposal " + proposalId + " registered (placeholder).";
        } catch (Exception e) {
            return "Błąd przy wysyłaniu transakcji: " + e.getMessage();
        }
    }
}
