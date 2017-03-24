package se.kth.id2203.broadcast;

import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.KompicsEvent;

import java.util.Collection;

/**
 * Created by Felicia & Mikael on 2017-02-21.
 */
public class BEBRequest implements KompicsEvent {

    private final BEBDeliver deliverEvent;          //The deliver event
    private final Collection<NetAddress> nodes;     //The nodes to broadcast to

    public BEBRequest(BEBDeliver deliverEvent, Collection <NetAddress> nodes) {
        this.deliverEvent = deliverEvent;
        this.nodes = nodes;
    }

    public BEBDeliver getDeliverEvent() {
        return deliverEvent;
    }

    public Collection<NetAddress> getBroadcastNodes() {
        return nodes;
    }
}
