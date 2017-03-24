package se.kth.id2203.multipaxos;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

/**
 * Created by Felicia & Mikael on 2017-03-01.
 */
public class Decide implements KompicsEvent, Serializable {
    private int ts;
    private int l;
    private int t;

    public Decide(int ts, int l, int t) {
        this.ts = ts;
        this. l = l;
        this.t = t;
    }

    public int getT() {
        return t;
    }

    public void setT(int t) {
        this.t = t;
    }

    public int getL() {
        return l;
    }

    public void setL(int l) {
        this.l = l;
    }

    public int getTs() {
        return ts;
    }

    public void setTs(int ts) {
        this.ts = ts;
    }
}
