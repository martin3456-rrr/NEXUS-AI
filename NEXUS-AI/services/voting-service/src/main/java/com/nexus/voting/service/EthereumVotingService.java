package com.nexus.voting.service;

import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;
//import com.nexus.ethereum.generated.VotingSystem; // po wygenerowaniu wrappera

import java.math.BigInteger;

@Service
public class EthereumVotingService {
    private final Web3j web3j;
    private final Credentials credentials;
    private final String contractAddress = "0xYourContractAddressOnSepolia";

    public EthereumVotingService() {
        String infuraUrl = System.getenv("INFURA_URL"); // e.g., "https://sepolia.infura.io/v3/YOUR_PROJECT_ID"
        String privateKey = System.getenv("ETHEREUM_PRIVATE_KEY");

        this.web3j = Web3j.build(new HttpService(infuraUrl));
        this.credentials = Credentials.create(privateKey);
    }

    public String castVote(long proposalId) throws Exception {
        // Load the contract wrapper
       // VotingSystem contract = VotingSystem.load(contractAddress, web3j, credentials, new DefaultGasProvider());
        // Call the smart contract function and send the transaction
        //TransactionReceipt receipt = contract.vote(BigInteger.valueOf(proposalId)).send();
        // Return the transaction hash as confirmation
        //return receipt.getTransactionHash();
        return null;
    }

}
