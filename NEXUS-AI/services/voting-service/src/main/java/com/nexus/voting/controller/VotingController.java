package com.nexus.voting.controller;

import com.nexus.voting.service.EthereumVotingService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vote")
public class VotingController {
    private final EthereumVotingService ethereumVotingService;

    public VotingController(EthereumVotingService ethereumVotingService) {
        this.ethereumVotingService = ethereumVotingService;
    }

    @PostMapping("/{proposalId}")
    public String vote(@PathVariable long proposalId) {
        try {
            return ethereumVotingService.castVote(proposalId);
        } catch (Exception e) {
            return "Błąd przy wysyłaniu transakcji: " + e.getMessage();
        }
    }
}
