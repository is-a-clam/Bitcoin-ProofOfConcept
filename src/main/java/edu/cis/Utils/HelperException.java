package edu.cis.Utils;

public class HelperException extends Exception {

    private String action;
    private String reason;

    public HelperException(String action, String reason) {
        super(reason);
        this.reason = reason;
        this.action = action;
    }

    @Override
    public String toString() {
        return "HelperException { " +
                "action: '" + action + '\'' +
                ", reason: '" + reason + '\'' +
                " }";
    }
}
