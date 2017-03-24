package se.kth.id2203.kvstore;

import se.kth.id2203.networking.NetAddress;

/**
 * Created by Felicia & Mikael on 2017-03-03.
 * Get operation
 * Returns the value stored under key key. Extends Operation
 */
public class GetOperation extends Operation {

    public GetOperation(NetAddress response, String key) {
        super(response, key);
    }

}
