package edu.cis.Controller;

import com.sun.org.apache.xerces.internal.util.TeeXMLDocumentFilterImpl;
import edu.cis.Model.*;
import edu.cis.Utils.Constants;
import edu.cis.Utils.Helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Node {

    protected Helper helper;

    protected BlockChain blockChain;
    protected ArrayList<Transaction> transactionPool;

    private ArrayList<Node> neighbours;
    private HashMap<String, ArrayList<TxOutput>> utxoStorage;
    private int nextUpdateUTXO;

    public Node() {
        this.helper = Helper.getInstance();
        this.neighbours = new ArrayList<>();
        this.blockChain = new BlockChain();
        this.transactionPool = new ArrayList<>();
        this.utxoStorage = new HashMap<>();
        this.nextUpdateUTXO = 0;
    }

    public void addNeighbour(Node... newNeighbours) {
        neighbours.addAll(Arrays.asList(newNeighbours));
    }

    public boolean receiveTransaction(Transaction transaction) {
        if (transactionPool.contains(transaction)) {
            return false;
        }
        if (validateTransaction(transaction)) {
            transactionPool.add(transaction);
            for (Node neighbour : neighbours) {
                neighbour.receiveTransaction(transaction);
            }
            return true;
        }
        return false;
    }

    public boolean receiveBlock(Block block) {
        // Make sure block does not already exist in the blockchain
        try {
            blockChain.getBlock(helper.getHash(block));
            return false;
        }
        catch (BlockChainException ignored) { }
        // Validate block, then add to blockchain, then pass on to neighbouring nodes
        if (validateBlock(block)) {
            try {
                blockChain.addBlock(block);
            }
            catch (BlockChainException e) {
                // Prev hash does not exist
                return false;
            }
            transactionPool.removeAll(block.getAllTransactions());
            for (Node neighbour : neighbours) {
                neighbour.receiveBlock(block);
            }
            return true;
        }
        return false;
    }

    private boolean validateTransaction(Transaction transaction) {
        try {
            // Input and outputs must not be empty
            if (transaction.getInputs().isEmpty() || transaction.getOutputs().isEmpty()) {
                return false;
            }

            // Sum of outputs must be larger than 0
            long totalOutputAmount = transaction.getTotalAmount();
            if (totalOutputAmount == 0) {
                return false;
            }

            // Get current UTXO and UTXO used in transaction pool
            ArrayList<TxOutput> UTXOs = trackUTXO(helper.getHash(blockChain.getCurrTail()));
            ArrayList<TxOutput> poolUTXOs = new ArrayList<>();
            for (Transaction poolTransaction : transactionPool) {
                for (TxInput poolInput : poolTransaction.getInputs()) {
                    Transaction prevTransaction = blockChain.getTransaction(poolInput.getTxHash());
                    TxOutput prevOutput = prevTransaction.getOutputs().get(poolInput.getOutputIndex());
                    poolUTXOs.add(prevOutput);
                }
            }

            long totalInputAmount = 0;
            for (TxInput input : transaction.getInputs()) {
                // Make sure input is not a coinBase input (coinBase input should not be broadcasted)
                if (TxInput.isCoinBaseInput(input)) {
                    return false;
                }
                // Make sure input is in UTXO set, and not already reference in the transaction pool
                Transaction prevTransaction = blockChain.getTransaction(input.getTxHash());
                TxOutput prevOutput = prevTransaction.getOutputs().get(input.getOutputIndex());
                if (!UTXOs.contains(prevOutput)) {
                    return false;
                }
                if (poolUTXOs.contains(prevOutput)) {
                    return false;
                }
                // Make sure input signature and public key is valid
                if (!helper.pubKeyToHexAddr(input.getPublicKey()).equals(prevOutput.getHexAddr())) {
                    return false;
                }
                if (!helper.verifySig(input.getPublicKey(), helper.getHash(prevOutput), input.getSignature())) {
                    return false;
                }
                totalInputAmount += prevOutput.getAmount();
            }

            // Make sure total input amount is larger than 0
            if (totalInputAmount == 0 || totalInputAmount < totalOutputAmount) {
                return false;
            }
        }
        catch (BlockChainException e) {
            throw new IllegalStateException(e);
        }
        return true;
    }

    private boolean validateBlock(Block block) {
        try {
            ArrayList<Transaction> transactions = block.getAllTransactions();
            long totalFee = 0;
            for (int i = 1; i < transactions.size(); i++) {
                if (!validateTransaction(transactions.get(i))) {
                    return false;
                }
                long totalInputAmount = 0;
                for (TxInput input : transactions.get(i).getInputs()) {
                    Transaction prevTransaction = blockChain.getTransaction(input.getTxHash());
                    TxOutput prevOutput = prevTransaction.getOutputs().get(input.getOutputIndex());
                    totalInputAmount += prevOutput.getAmount();
                }
                totalFee += totalInputAmount - transactions.get(i).getTotalAmount();
            }
            // Make sure first transaction has only one coinbase input
            if (transactions.get(0).getInputs().size() != 1) {
                return false;
            }
            TxInput coinBaseInput = transactions.get(0).getInputs().get(0);
            if (!TxInput.isCoinBaseInput(coinBaseInput)) {
                return false;
            }
            // Make sure output of coinbase transaction is correct
            if (transactions.get(0).getTotalAmount() != Constants.miningReward + totalFee) {
                return false;
            }
        }
        catch (BlockChainException e) {
            throw new IllegalStateException(e);
        }
        return true;
    }

    private ArrayList<TxOutput> trackUTXO(String blockHash) throws BlockChainException {
        if (utxoStorage.get(blockHash) != null) {
            nextUpdateUTXO = blockChain.getHeight(blockHash) + Constants.utxoStorageFreq;
            return utxoStorage.get(blockHash);
        }
        Block block = blockChain.getBlock(blockHash);
        ArrayList<TxOutput> prevUTXO;
        if (Block.isGenesis(block)) {
            prevUTXO = new ArrayList<>();
        }
        else {
            prevUTXO = trackUTXO(block.getPrevHash());
        }
        for (Transaction transaction : block.getAllTransactions()) {
            for (TxInput input : transaction.getInputs()) {
                if (!TxInput.isCoinBaseInput(input)) {
                    Transaction prevTransaction = blockChain.getTransaction(input.getTxHash());
                    TxOutput prevOutput = prevTransaction.getOutputs().get(input.getOutputIndex());
                    prevUTXO.remove(prevOutput);
                }
            }
            prevUTXO.addAll(transaction.getOutputs());
        }
        if (blockChain.getHeight(blockHash) == nextUpdateUTXO) {
            utxoStorage.put(blockHash, prevUTXO);
        }
        return prevUTXO;
    }

}
