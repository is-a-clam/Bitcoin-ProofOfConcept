package edu.cis.Model;

import edu.cis.Utils.Constants;
import edu.cis.Utils.Helper;

import javax.xml.crypto.dsig.TransformService;
import java.util.*;

public class BlockChain {

    private class BlockChainNode {

        private Block block;
        private BlockChainNode prevNode;
        private int blockHeight;

        public BlockChainNode(Block block, BlockChainNode prevNode) {
            this.block = block;
            this.prevNode = prevNode;
            if (prevNode == null) {
                this.blockHeight = 0;
            }
            else {
                this.blockHeight = prevNode.blockHeight + 1;
            }
        }

        public Block getBlock() {
            return block;
        }

        public void setBlock(Block block) {
            this.block = block;
        }

        public BlockChainNode getPrevNode() {
            return prevNode;
        }

        public void setPrevNode(BlockChainNode prevNode) {
            this.prevNode = prevNode;
            if (prevNode == null) {
                this.blockHeight = 0;
            }
            else {
                this.blockHeight = prevNode.blockHeight + 1;
            }
        }

        public int getBlockHeight() {
            return blockHeight;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof BlockChainNode) {
                return this.block.equals(((BlockChainNode) obj).block);
            }
            return super.equals(obj);
        }
    }

    private Helper helper;
    private BlockChainNode head;
    private TreeSet<BlockChainNode> tails;
    private HashMap<String, BlockChainNode> blockHashMap;

    public BlockChain() {
        this.helper = Helper.getInstance();
        this.tails = new TreeSet<>((node1, node2) -> node1.blockHeight - node2.blockHeight);
        this.blockHashMap = new HashMap<>();
    }

    public Block getHead() throws BlockChainException {
        if (size() == 0) {
            throw new BlockChainException("blockChainGetHead", "block chain is empty");
        }
        return head.getBlock();
    }

    public Block getCurrTail() throws BlockChainException {
        if (size() == 0) {
            throw new BlockChainException("blockChainGetTail", "block chain is empty");
        }
        return tails.last().getBlock();
    }

    public int size() {
        if (tails.isEmpty()) {
            return 0;
        }
        return tails.last().getBlockHeight();
    }

    public int getHeight(String blockHash) throws BlockChainException {
        BlockChainNode blockChainNode = blockHashMap.get(blockHash);
        if (blockChainNode == null) {
            throw new BlockChainException("blockChainGetHeight", "block does not exist");
        }
        return blockChainNode.getBlockHeight();
    }

    public Block getBlock(String blockHash) throws BlockChainException {
        BlockChainNode blockChainNode = blockHashMap.get(blockHash);
        if (blockChainNode == null) {
            throw new BlockChainException("blockChainGetBlock", "block does not exist");
        }
        return blockChainNode.getBlock();
    }

    public Block getContainerBlock(String transactionHash) throws BlockChainException {
        for (BlockChainNode node : blockHashMap.values()) {
            Block block = node.getBlock();
            try {
                Transaction transaction = block.getTransaction(transactionHash);
                return block;
            }
            catch (BlockChainException ignored) { }
        }
        throw new BlockChainException("blockChainGetContainerBlock", "container block does not exist");
    }

    public Transaction getTransaction(String transactionHash) throws BlockChainException {
        Block block = getContainerBlock(transactionHash);
        return block.getTransaction(transactionHash);
    }

    public void addBlock(Block block) throws BlockChainException {
        if (size() == 0) {
            BlockChainNode newNode = new BlockChainNode(block, null);
            head = newNode;
            tails.add(newNode);
            blockHashMap.put(helper.getHash(block), newNode);
        }
        else {
            if (blockHashMap.containsKey(helper.getHash(block))) {
                throw new BlockChainException("blockChainAdd", "block already exists");
            }
            BlockChainNode prevBlockNode = blockHashMap.get(block.getPrevHash());
            if (prevBlockNode == null) {
                throw new BlockChainException("blockChainGet", "prev hash is invalid");
            }
            BlockChainNode newNode = new BlockChainNode(block, prevBlockNode);
            tails.remove(prevBlockNode);
            tails.add(newNode);
            blockHashMap.put(helper.getHash(block), newNode);
        }
    }

    public boolean isConfirmed(String blockHash) throws BlockChainException {
        BlockChainNode blockChainNode = blockHashMap.get(blockHash);
        if (blockChainNode == null) {
            throw new BlockChainException("blockChainIsInMain", "block does not exist");
        }
        BlockChainNode currBlockNode = tails.last();
        while (currBlockNode != head) {
            if (currBlockNode.equals(blockChainNode)) {
                if (currBlockNode.getBlockHeight() + Constants.blocksToWaitForConfirm <= size()) {
                    return true;
                }
                else {
                    return false;
                }
            }
            currBlockNode = currBlockNode.getPrevNode();
        }
        return false;
    }
}
