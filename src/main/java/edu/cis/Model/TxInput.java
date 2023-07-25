package edu.cis.Model;

import edu.cis.Utils.Helper;

import java.math.BigInteger;
import java.security.PublicKey;

/**
 * Class that represents a transaction input in a transaction
 *
 * @author Isaac Lam
 * @version 1.0
 */
public class TxInput extends Byteable {

    private Helper helper;

    private String txHash;
    private int outputIndex;
    private String signature;
    private PublicKey publicKey;

    public TxInput(String txHash, int outputIndex, String signature, PublicKey publicKey) {
        this.helper = Helper.getInstance();
        this.txHash = txHash;
        this.outputIndex = outputIndex;
        this.signature = signature;
        this.publicKey = publicKey;
    }

    public static TxInput coinBaseInput() {
        TxInput coinBaseInput = new TxInput(Helper.getInstance().byteToHex(new byte[32]),
                                            -1, null, null);
        return coinBaseInput;
    }

    public static boolean isCoinBaseInput(TxInput input) {
        if (input.getTxHash().equals(Helper.getInstance().byteToHex(new byte[32]))) {
            if (input.getOutputIndex() == -1) {
                return true;
            }
        }
        return false;
    }

    public String getTxHash() {
        return txHash;
    }

    public int getOutputIndex() {
        return outputIndex;
    }

    public String getSignature() {
        return signature;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    @Override
    public byte[] getBytes() {
        if (TxInput.isCoinBaseInput(this)) {
            return helper.combineByteArrays(helper.hexToByte(txHash),
                                            helper.numToByte(BigInteger.valueOf(outputIndex), 4));
        }
        return helper.combineByteArrays(helper.hexToByte(txHash),
                                        helper.numToByte(BigInteger.valueOf(outputIndex), 4),
                                        helper.hexToByte(signature),
                                        publicKey.getEncoded());
    }
}
