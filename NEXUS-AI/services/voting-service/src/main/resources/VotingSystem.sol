// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

contract VotingSystem {
    // Mapping from proposal ID to vote count
    mapping(uint256 => uint256) public proposalVotes;

    // Event to log new votes
    event Voted(address voter, uint256 proposalId);

    // Function to cast a vote for a proposal
    function vote(uint256 proposalId) public {
        // In a real dApp, you'd add checks (e.g., one vote per person)
        proposalVotes[proposalId]++;
        emit Voted(msg.sender, proposalId);
    }
}