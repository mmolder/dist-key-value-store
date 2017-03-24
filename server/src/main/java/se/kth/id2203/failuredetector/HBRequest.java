package se.kth.id2203.failuredetector;

import se.kth.id2203.networking.NetAddress;
import se.kth.id2203.networking.NetMessage;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.network.Transport;

import java.io.Serializable;

/**
 * Created by Felicia & Mikael on 2017-02-22.
 */
public class HBRequest extends NetMessage implements KompicsEvent, Serializable {

    private int seqnum; //The sequence number of the hbrequest

    public HBRequest(NetAddress src, NetAddress dest, int seqnum) {
        super(src, dest, Transport.TCP);
        this.seqnum = seqnum;
    }

    public int getSeqnum() {
        return seqnum;
    }
}
