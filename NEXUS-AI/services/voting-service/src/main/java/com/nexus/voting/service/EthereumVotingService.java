package com.nexus.voting.service;

import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;
import com.nexus.ethereum.generated.VotingSystem; // po wygenerowaniu wrappera

@Service
public class EthereumVotingService {
    private final Web3j web3j;
    private final Credentials credentials;
    private final String contractAddress = "ADRES_TWOJEGO_KONTRAKTU_NA_SIECI_TESTOWEJ";

    public EthereumVotingService() {
        this.web3j = Web3j.build(new HttpService("https://sepolia.infura.io/v3/TWOJ_INFURA_PROJECT_ID"));
        this.credentials = Credentials.create("TWÓJ_KLUCZ_PRYWATNY"); // NIE trzymaj klucza w kodzie produkcyjnym!
    }

    public String castVote(long proposalId) throws Exception {
        VotingSystem contract = VotingSystem.load(contractAddress, web3j, credentials, new DefaultGasProvider());
        TransactionReceipt receipt = contract.vote(BigInteger.valueOf(proposalId)).send();
        return receipt.getTransactionHash();
        return "Głos na propozycję " + proposalId + " został wysłany (symulacja).";
    }
}
