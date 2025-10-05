package com.nexus.voting.ethereum.generated;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

public class VotingSystem extends Contract {
    private static final String BINARY = "Your contract binary code";

    protected VotingSystem(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        super(BINARY, contractAddress, web3j, credentials, gasProvider);
    }

    protected VotingSystem(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider gasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, gasProvider);
    }

    public RemoteCall<TransactionReceipt> vote(BigInteger proposalId) {
        final Function function = new Function(
                "vote",
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(proposalId)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> getVotes(BigInteger proposalId) {
        final Function function = new Function("getVotes",
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(proposalId)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public static VotingSystem load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        return new VotingSystem(contractAddress, web3j, credentials, gasProvider);
    }

    public static VotingSystem load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider gasProvider) {
        return new VotingSystem(contractAddress, web3j, transactionManager, gasProvider);
    }
}
