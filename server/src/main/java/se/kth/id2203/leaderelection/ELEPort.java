package se.kth.id2203.leaderelection;

import se.sics.kompics.PortType;

/**
 * Created by Felicia & Mikael on 2017-02-26.
 */
public class ELEPort extends PortType {
    {
        indication(Trust.class);
    }
}
