package edu.cis.Model;

public class BlockChainException extends Exception {

    private String action;
    private String reason;

    public BlockChainException(String action, String reason) {
        super(reason);
        this.reason = reason;
        this.action = action;
    }

    @Override
    public String toString() {
        return "BlockChainException { " +
                "action: '" + action + '\'' +
                ", reason: '" + reason + '\'' +
                " }";
    }
}
