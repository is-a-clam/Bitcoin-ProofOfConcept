package edu.cis.Model;

import edu.cis.Utils.Helper;

import java.util.ArrayList;

/**
 * Class that represents a transaction in the blockchain
 *
 * @author Isaac Lam
 * @version 1.0
 */
public class Transaction extends Byteable {

    private Helper helper;

    private ArrayList<TxInput> inputs;
    private ArrayList<TxOutput> outputs;

    public Transaction(ArrayList<TxInput> inputs, ArrayList<TxOutput> outputs) {
        this.helper = Helper.getInstance();
        this.inputs = inputs;
        this.outputs = outputs;
    }

    public ArrayList<TxInput> getInputs() {
        return inputs;
    }

    public ArrayList<TxOutput> getOutputs() {
        return outputs;
    }

    public long getTotalAmount() {
        long totalAmount = 0;
        for (TxOutput output : outputs) {
            totalAmount += output.getAmount();
        }
        return totalAmount;
    }

    @Override
    public byte[] getBytes() {
        byte[][] byteArrays = new byte[inputs.size() + outputs.size()][];
        for (int i = 0; i < inputs.size(); i++) {
            byteArrays[i] = inputs.get(i).getBytes();
        }
        for (int i = 0; i < outputs.size(); i++) {
            byteArrays[i + inputs.size()] = outputs.get(i).getBytes();
        }
        return helper.combineByteArrays(byteArrays);
    }
}
