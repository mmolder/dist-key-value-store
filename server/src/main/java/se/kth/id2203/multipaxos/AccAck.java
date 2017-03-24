package se.kth.id2203.multipaxos;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;

/**
 * Created by Felicia & Mikael on 2017-03-01.
 */
public class AccAck implements KompicsEvent, Serializable{

    private int ts;
    private int avLength;
    private int t;

    public AccAck(int ts, int avLength, int t){
        this.ts = ts;
        this.avLength = avLength;
        this.t = t;
    }

    public int getTs() {
        return ts;
    }

    public void setTs(int ts) {
        this.ts = ts;
    }

    public int getAvLength() {
        return avLength;
    }

    public void setAvLength(int avLength) {
        this.avLength = avLength;
    }

    public int getT() {
        return t;
    }

    public void setT(int t) {
        this.t = t;
    }
}