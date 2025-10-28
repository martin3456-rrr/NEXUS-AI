package com.nexus.controller;

import com.nexus.blockchain.model.Block;
import com.nexus.service.CryptographyService;
import com.nexus.service.ProofOfStakeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.ArrayList;
import java.util.List;

@RestController
@Tag(name = "Blockchain", description = "Endpoints for interacting with the blockchain")
@org.springframework.web.bind.annotation.RequestMapping("/api/blockchain")
public class BlockchainController {
    public static List<Block> blockchain = new ArrayList<>();
    @Autowired
    private ProofOfStakeService posService;
    @Autowired
    private CryptographyService cryptoService;

    public BlockchainController() {
        if (blockchain.isEmpty()) {
            blockchain.add(new Block("Genesis Block", "0", "genesis", "genesis-sig"));
        }
    }

    @GetMapping("/chain")
    @Operation(summary = "Get the entire blockchain", description = "Returns a list of all blocks currently in the chain.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved the blockchain",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class)))
    public List<Block> getBlockchain() {
        return blockchain;
    }

    @PostMapping("/add")
    @Operation(summary = "Add a new block to the chain", description = "Creates and adds a new block with the provided data.")
    @ApiResponse(responseCode = "200", description = "Block added successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Block.class)))
    @ApiResponse(responseCode = "500", description = "Error while selecting a validator")
    public Block addBlock(@RequestBody String data) throws Exception {
        String validatorPublicKey = cryptoService.getPublicKey();
        Block previousBlock = blockchain.getLast();
        String dataToSign = previousBlock.getHash() + data;
        String signature = cryptoService.sign(dataToSign);
        Block newBlock = new Block(data, previousBlock.getHash(), validatorPublicKey, signature);
        blockchain.add(newBlock);
        return newBlock;
    }
}
