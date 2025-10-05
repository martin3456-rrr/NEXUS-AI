package com.nexus.voting.service;

import com.nexus.voting.ethereum.generated.VotingSystem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;

@Service
public class VotingService {

    private final Web3j web3j;
    private final Credentials credentials;
    private final String contractAddress;

    public VotingService(
            @Value("${infura.url:http://localhost:8545}") String ethereumUrl,
            @Value("${ethereum.private.key:0x0000000000000000000000000000000000000000000000000000000000000000}") String privateKey,
            @Value("${ethereum.contract.address:0x0000000000000000000000000000000000000000}") String contractAddress
    ) {
        this.web3j = Web3j.build(new HttpService(ethereumUrl));
        this.credentials = Credentials.create(privateKey);
        this.contractAddress = contractAddress;
    }

    public String createProposal(String title, String description) {
        // The current generated contract wrapper (VotingSystem) does not expose a createProposal method.
        // If/when the Solidity contract adds it and the wrapper is regenerated, implement it here similarly to vote().
        throw new UnsupportedOperationException("Funkcja createProposal nie jest dostępna w aktualnym kontrakcie VotingSystem.");
    }

    public String vote(BigInteger proposalId) {
        try {
            VotingSystem contract = VotingSystem.load(
                    contractAddress,
                    web3j,
                    credentials,
                    new DefaultGasProvider()
            );
            TransactionReceipt tx = contract.vote(proposalId).send();
            return tx.getTransactionHash();
        } catch (Exception e) {
            throw new RuntimeException("Błąd głosowania: " + e.getMessage(), e);
        }
    }
}
