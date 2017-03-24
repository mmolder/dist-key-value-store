package se.kth.id2203.broadcast;

import se.kth.id2203.networking.NetAddress;
import se.kth.id2203.overlay.RouteMsg;
import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

/**
 * Created by Felicia & Mikael on 2017-02-21.
 */
public class BEBDeliver implements KompicsEvent, Serializable {

    private NetAddress source;  //Source of the deliver event
    private RouteMsg data;      //Payload

    public BEBDeliver(NetAddress source, RouteMsg data) {
        this.source = source;
        this.data = data;
    }

    public NetAddress getSource() {
        return source;
    }

    public RouteMsg getData() {
        return data;
    }
}
