package se.kth.id2203.multipaxos;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Felicia & Mikael on 2017-03-01.
 */
public class Accept implements KompicsEvent, Serializable {

    private int ts;
    private ArrayList<Object> vsuf;
    private int offs;
    private int t;

    public Accept(int ts, ArrayList<Object> vsuf, int al, int t){
        this.ts = ts;
        this.vsuf = vsuf;
        this.offs = al;
        this.t = t;
    }

    public int getTs() {
        return ts;
    }

    public void setTs(int ts) {
        this.ts = ts;
    }

    public ArrayList<Object> getVsuf() {
        return vsuf;
    }

    public void setVsuf(ArrayList<Object> suffix) {
        this.vsuf = suffix;
    }

    public int getOffs() {
        return offs;
    }

    public void setOffs(int al) {
        this.offs = al;
    }

    public int getT() {
        return t;
    }

    public void setT(int t) {
        this.t = t;
    }
}
