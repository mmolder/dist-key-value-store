package se.kth.id2203.broadcast;

import se.sics.kompics.PortType;

/**
 * Created by Felicia & Mikael on 2017-02-21.
 */
public class BEBPort extends PortType {
    {
        indication(BEBDeliver.class);
        request(BEBRequest.class);
    }
}
