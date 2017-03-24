package se.kth.id2203.multipaxos;

import se.sics.kompics.KompicsEvent;

/**
 * Created by Felicia & Mikael on 2017-03-01.
 */
public class AscAbort implements KompicsEvent {
    private Object ob;

    public AscAbort(Object ob) {
        this.ob = ob;
    }

    public Object getOb() {
        return ob;
    }
}
