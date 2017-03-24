package se.kth.id2203.failuredetector;

import se.sics.kompics.PortType;

/**
 * Created by Felicia & Mikael on 2017-02-22.
 */
public class EPFDPort extends PortType {
    {
        indication(Suspect.class);
        indication(Restore.class);
    }
}
