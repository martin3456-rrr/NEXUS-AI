package com.nexus.voting.service;

import com.nexus.voting.ethereum.generated.VotingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;

@Service
public class EthereumVotingService {
    private static final Logger logger = LoggerFactory.getLogger(EthereumVotingService.class);

    private final Web3j web3j;
    private final Credentials credentials;
    private final String contractAddress;

    public EthereumVotingService(
            @Value("${infura.url}") String infuraUrl,
            @Value("${ethereum.private.key}") String privateKey,
            @Value("${ethereum.contract.address}") String contractAddress) {

        this.web3j = Web3j.build(new HttpService(infuraUrl));
        this.credentials = Credentials.create(privateKey);
        this.contractAddress = contractAddress;
        logger.info("EthereumVotingService initialized for contract at address: {}", contractAddress);
    }

    public String castVote(long proposalId) throws Exception {
        logger.info("Attempting to cast vote for proposal ID: {}", proposalId);
        VotingSystem contract = VotingSystem.load(contractAddress, web3j, credentials, new DefaultGasProvider());
        TransactionReceipt receipt = contract.vote(BigInteger.valueOf(proposalId)).send();
        logger.info("Vote transaction sent successfully. Hash: {}", receipt.getTransactionHash());
        return receipt.getTransactionHash();
    }
    public BigInteger getVoteCount(long proposalId) throws Exception {
        logger.info("Retrieving vote count for proposal ID: {}", proposalId);
        VotingSystem contract = VotingSystem.load(contractAddress, web3j, credentials, new DefaultGasProvider());
        BigInteger votes = contract.getVotes(BigInteger.valueOf(proposalId)).send();
        logger.info("Proposal {} has {} votes", proposalId, votes);
        return votes;
    }
}
