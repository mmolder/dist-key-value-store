package se.kth.id2203.kvstore;

import se.kth.id2203.networking.NetAddress;

/**
 * Created by Felicia & Mikael on 2017-03-07.
 * Compare and Swap operation
 * Compares a value stored under key key with refVal and if they match
 * swaps the value with newVal. Extends Operation so it can be used with
 * OpResponse
 */
public class CASOperation extends Operation {
    private String refVal;
    private String newVal;

    public CASOperation(NetAddress source, String key, String refVal, String newVal) {
        super(source, key);
        this.refVal = refVal;
        this.newVal = newVal;
    }

    public String getRefVal() {
        return refVal;
    }

    public String getNewVal() {
        return newVal;
    }
}
