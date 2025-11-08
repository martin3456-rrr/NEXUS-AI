pragma solidity ^0.8.20;

contract VotingSystem {
    mapping(uint256 => uint256) public proposalVotes;

    event Voted(address voter, uint256 proposalId);

    function vote(uint256 proposalId) public {
        proposalVotes[proposalId]++;
        emit Voted(msg.sender, proposalId);
    }
}