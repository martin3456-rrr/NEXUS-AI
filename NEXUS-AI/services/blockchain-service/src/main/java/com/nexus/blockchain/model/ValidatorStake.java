package com.nexus.blockchain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;


@Entity
public class ValidatorStake {
    @Id
    private String validatorAddress;
    private Integer stake;

    public String getValidatorAddress() { return validatorAddress; }
    public void setValidatorAddress(String validatorAddress) { this.validatorAddress = validatorAddress; }
    public Integer getStake() { return stake; }
    public void setStake(Integer stake) { this.stake = stake; }

}
