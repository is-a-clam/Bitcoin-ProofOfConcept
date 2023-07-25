package edu.cis.Model;

import edu.cis.Controller.Wallet;
import edu.cis.Utils.Constants;
import edu.cis.Utils.Helper;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class that represents a block in the blockchain
 *
 * @author Isaac Lam
 * @version 1.0
 */
public class Block extends Byteable {

    private Helper helper;

    private String prevHash;
    private String merkleRoot;
    private long timestamp;
    private int nonce;
    private ArrayList<Transaction> transactions;
    private HashMap<String, Transaction> transactionHashMap;

    public Block(String prevHash) {
        this.helper = Helper.getInstance();
        this.prevHash = prevHash;
        this.timestamp = System.currentTimeMillis();
        this.nonce = 0;
        this.transactions = new ArrayList<>();
        this.transactionHashMap = new HashMap<>();
    }

    public static Block genesis(Wallet creator) {
        try {
            Block genesis = new Block(Helper.getInstance().byteToHex(new byte[32]));
            ArrayList<TxInput> inputs = new ArrayList<>();
            inputs.add(TxInput.coinBaseInput());
            ArrayList<TxOutput> outputs = new ArrayList<>();
            outputs.add(new TxOutput(Constants.miningReward, creator.generateAddress()));
            Transaction coinBase = new Transaction(inputs, outputs);
            genesis.addTransaction(coinBase);
            genesis.setMerkleRoot();
            return genesis;
        }
        catch (BlockChainException e) {
            throw new IllegalStateException(e);
        }
    }

    public static boolean isGenesis(Block block) {
        if (block.getPrevHash().equals(Helper.getInstance().byteToHex(new byte[32]))) {
            return true;
        }
        return false;
    }

    public String getPrevHash() {
        return prevHash;
    }

    /**
     * Used when all possible values of the nonce has been tried, and a solution has not been found. By changing the
     * timestamp, all possible values of the nonce can be tried again, since the hash will be different
     */
    public void updateTimestamp() {
        if (timestamp < System.currentTimeMillis()) {
            timestamp = System.currentTimeMillis();
        }
        else {
            // Used when all possible values of the nonce has been looped through in under a second
            timestamp += 1;
        }
    }

    public void incrementNonce() {
        if (nonce == Integer.MAX_VALUE) {
            nonce = Integer.MIN_VALUE;
        }
        else if (nonce == -1) {
            nonce = 0;
            updateTimestamp();
        }
        else {
            nonce++;
        }
    }

    public ArrayList<Transaction> getAllTransactions() {
        return transactions;
    }

    public Transaction getTransaction(String transactionHash) throws BlockChainException {
        Transaction transaction = transactionHashMap.get(transactionHash);
        if (transaction == null) {
            throw new BlockChainException("blockGetTransaction", "Transaction does not exist");
        }
        return transaction;
    }

    public void addTransaction(Transaction transaction) throws BlockChainException {
        if (transactionHashMap.get(helper.getHash(transaction)) != null) {
            throw new BlockChainException("blockAddTransaction", "Transaction already exists");
        }
        transactions.add(transaction);
        transactionHashMap.put(helper.getHash(transaction), transaction);
    }

    public String getMerkleRoot() {
        return merkleRoot;
    }

    public void setMerkleRoot() {
        this.merkleRoot = helper.byteToHex(computeMerkle(0 ,transactions.size()));
    }

    private byte[] computeMerkle(int start, int end) {
        if (end <= start + 1) {
            return helper.hexToByte(helper.getHash(transactions.get(start)));
        }
        byte[] combinedHash = helper.combineByteArrays(computeMerkle(start, (start + end) / 2),
                                                       computeMerkle((start + end) / 2, end));
        return helper.SHA256(helper.SHA256(combinedHash));
    }

    @Override
    public byte[] getBytes() {
        return getBytes(false);
    }

    public byte[] getBytes(boolean omitMerkleRecalculate) {
        if (!omitMerkleRecalculate) {
            setMerkleRoot();
        }
        return helper.combineByteArrays(helper.hexToByte(prevHash),
                                        helper.hexToByte(merkleRoot),
                                        helper.numToByte(BigInteger.valueOf(timestamp), 4),
                                        helper.numToByte(BigInteger.valueOf(nonce), 4));
    }
}
