package edu.cis.Model;

import edu.cis.Utils.Helper;

import java.math.BigInteger;

/**
 * Class that represents a transaction output in a transaction
 *
 * @author Isaac Lam
 * @version 1.0
 */
public class TxOutput extends Byteable {

    private Helper helper;

    private int amount;
    private String hexAddr;

    public TxOutput(int amount, String hexAddr) {
        this.helper = Helper.getInstance();
        this.amount = amount;
        this.hexAddr = hexAddr;
    }

    public int getAmount() {
        return amount;
    }

    public String getHexAddr() {
        return hexAddr;
    }

    @Override
    public byte[] getBytes() {
        return helper.combineByteArrays(helper.numToByte(BigInteger.valueOf(amount), 8),
                                        helper.hexToByte(hexAddr));
    }
}
