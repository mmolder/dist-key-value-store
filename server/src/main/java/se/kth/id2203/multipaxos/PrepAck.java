package se.kth.id2203.multipaxos;

import se.sics.kompics.KompicsEvent;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Felicia & Mikael on 2017-03-01.
 */
public class PrepAck implements KompicsEvent, Serializable {
    private int ts;
    private int ats;
    private ArrayList<Object> suffix;
    private int al;
    private int t;

    public PrepAck(int ts, int ats, ArrayList<Object> suffix, int al, int t) {
        this.ts = ts;
        this.ats = ats;
        this.suffix = suffix;
        this.al = al;
        this.t = t;
    }

    public int getTs() {
        return ts;
    }

    public void setTs(int ts) {
        this.ts = ts;
    }

    public int getAts() {
        return ts;
    }

    public void setAts(int ats) {
        this.ats = ats;
    }

    public ArrayList<Object> getSuffix() {
        return suffix;
    }

    public void setSuffix(ArrayList<Object> suffix) {
        this.suffix = suffix;
    }

    public int getAl() {
        return al;
    }

    public void setAl(int al) {
        this.al = al;
    }

    public int getT() {
        return t;
    }

    public void setT(int t) {
        this.t = t;
    }
}
