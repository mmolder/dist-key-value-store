package se.kth.id2203.multipaxos;

import se.sics.kompics.PortType;

/**
 * Created by Felicia & Mikael on 2017-03-01.
 */
public class MPPort extends PortType {
    {
        request(AscPropose.class);
        indication(AscDecide.class);
        indication(AscAbort.class);
    }
}
