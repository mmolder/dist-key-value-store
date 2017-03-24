package se.kth.id2203.multipaxos;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

/**
 * Created by Felicia & Mikael on 2017-03-01.
 */
public class Nack implements KompicsEvent, Serializable {
    private int ts;
    private int t;

    public Nack(int ts, int t) {
        this.ts = ts;
        this.t = t;
    }

    public int getT() {
        return t;
    }

    public void setT(int t) {
        this.t = t;
    }

    public int getTs() {
        return ts;
    }

    public void setTs(int ts) {
        this.ts = ts;
    }
}
