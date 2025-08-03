// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

contract VotingSystem {
    mapping(uint => uint) public voteCounts;

    function vote(uint proposalId) public {
        voteCounts[proposalId]++;
    }
}
