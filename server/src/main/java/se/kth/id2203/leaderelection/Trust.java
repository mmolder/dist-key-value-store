package se.kth.id2203.leaderelection;

import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.KompicsEvent;

/**
 * Created by Felicia & Mikael on 2017-02-26.
 */
public class Trust implements KompicsEvent {

    private NetAddress leader;  //The node to be trusted as leader

    public Trust(NetAddress leader) {
        this.leader = leader;
    }

    public NetAddress getLeader() {
        return leader;
    }
}
