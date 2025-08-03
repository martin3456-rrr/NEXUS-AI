package com.nexus.blockchain.controller;

import com.nexus.blockchain.model.Block;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class BlockchainController {
    public static List<Block> blockchain = new ArrayList<>();

    public BlockchainController() {
        if (blockchain.isEmpty()) {
            blockchain.add(new Block("Genesis Block", "0"));
        }
    }

    @GetMapping("/chain")
    public List<Block> getBlockchain() {
        return blockchain;
    }

    @PostMapping("/add")
    public Block addBlock(@RequestBody String data) {
        Block previousBlock = blockchain.get(blockchain.size() - 1);
        Block newBlock = new Block(data, previousBlock.getHash());
        blockchain.add(newBlock);
        return newBlock;
    }
}
