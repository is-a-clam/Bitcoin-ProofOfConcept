package edu.cis.Model;

import edu.cis.Utils.Helper;

public abstract class Byteable {
    public abstract byte[] getBytes();

    @Override
    public boolean equals(Object obj) {
        Helper helper = Helper.getInstance();
        if (obj instanceof Byteable) {
            return helper.getHash(this).equals(helper.getHash((Byteable) obj));
        }
        return super.equals(obj);
    }
}
