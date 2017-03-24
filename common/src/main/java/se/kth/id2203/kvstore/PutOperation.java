package se.kth.id2203.kvstore;

import se.kth.id2203.networking.NetAddress;

/**
 * Created by Felicia & Mikael on 2017-03-03.
 * Put operation
 * Stores value value under key key. Extends Operation
 */
public class PutOperation extends Operation {
    public final String value;

    public PutOperation(NetAddress source, String key, String value) {
        super(source, key);
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
