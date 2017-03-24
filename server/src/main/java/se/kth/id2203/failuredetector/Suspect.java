package se.kth.id2203.failuredetector;

import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.KompicsEvent;

/**
 * Created by Felicia & Mikael on 2017-02-22.
 */
public class Suspect implements KompicsEvent {
    private final NetAddress src;

    public Suspect(NetAddress src) {
        this.src = src;
    }

    public final NetAddress getSrc() {
        return src;
    }

}
