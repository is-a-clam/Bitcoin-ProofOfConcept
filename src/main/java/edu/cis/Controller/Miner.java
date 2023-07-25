package edu.cis.Controller;

import edu.cis.Model.Block;

public class Miner extends Wallet {

    public Miner() {
        super();
    }

    @Override
    public boolean receiveBlock(Block block) {
        if (super.receiveBlock(block)) {

            return true;
        }
        return false;
    }

    public void mineBlock(Block block) {

    }

    private boolean sendBlock(Block block) {
        return super.receiveBlock(block);
    }


}
