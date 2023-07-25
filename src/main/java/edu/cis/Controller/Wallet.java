package edu.cis.Controller;

import com.sun.org.apache.xerces.internal.util.TeeXMLDocumentFilterImpl;
import edu.cis.Model.*;
import edu.cis.Utils.Helper;

import java.security.KeyPair;
import java.util.ArrayList;

public class Wallet extends Node {

    private ArrayList<KeyPair> unconfirmedKeyPairs;
    private ArrayList<KeyPair> confirmedKeyPairs;
    private ArrayList<TxInput> confirmedUTXOInputs;

    public Wallet() {
        super();
        this.unconfirmedKeyPairs = new ArrayList<>();
        this.confirmedKeyPairs = new ArrayList<>();
        this.confirmedUTXOInputs = new ArrayList<>();
    }

    @Override
    public boolean receiveTransaction(Transaction transaction) {
        if (super.receiveTransaction(transaction)) {
            for (TxOutput output : transaction.getOutputs()) {
                ArrayList<KeyPair> toRemove = new ArrayList<>();
                for (KeyPair keyPair : unconfirmedKeyPairs) {
                    if (helper.pubKeyToHexAddr(keyPair.getPublic()).equals(output.getHexAddr())) {
                        confirmedKeyPairs.add(keyPair);
                        toRemove.add(keyPair);
                    }
                }
                unconfirmedKeyPairs.removeAll(toRemove);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean receiveBlock(Block block) {
        if (super.receiveBlock(block)) {
            for (Transaction transaction : block.getAllTransactions()) {
                for (int outIndex = 0; outIndex < transaction.getOutputs().size(); outIndex++) {
                    TxOutput output = transaction.getOutputs().get(outIndex);
                    ArrayList<KeyPair> toRemove = new ArrayList<>();
                    for (KeyPair keyPair : confirmedKeyPairs) {
                        if (helper.pubKeyToHexAddr(keyPair.getPublic()).equals(output.getHexAddr())) {
                            TxInput UTXO = new TxInput(helper.getHash(transaction), outIndex,
                                                       helper.generateSig(keyPair.getPrivate(), helper.getHash(output)),
                                                       keyPair.getPublic());
                            confirmedUTXOInputs.add(UTXO);
                            toRemove.add(keyPair);
                        }
                    }
                    confirmedKeyPairs.removeAll(toRemove);
                }
            }
            return true;
        }
        return false;
    }

    public String generateAddress() {
        KeyPair keyPair = helper.generateKeyPair();
        unconfirmedKeyPairs.add(keyPair);
        return helper.pubKeyToHexAddr(keyPair.getPublic());
    }

    public long getBalance() {
        long balance = 0;
        for (TxInput input : confirmedUTXOInputs) {
            try {
                Block container = this.blockChain.getContainerBlock(input.getTxHash());
                if (this.blockChain.isConfirmed(helper.getHash(container))) {
                    Transaction transaction = container.getTransaction(input.getTxHash());
                    balance += transaction.getOutputs().get(input.getOutputIndex()).getAmount();
                }
            }
            catch (BlockChainException e) {
                throw new IllegalStateException(e);
            }
        }
        return balance;
    }
}
