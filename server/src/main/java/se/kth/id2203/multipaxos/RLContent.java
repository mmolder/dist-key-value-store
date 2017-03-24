package se.kth.id2203.multipaxos;

import java.util.ArrayList;

/**
 * Created by Felicia & Mikael on 2017-03-03.
 */
public class RLContent {
    private int ts;
    private ArrayList<Object> vsuf;

    public RLContent(int ts, ArrayList<Object> vsuf) {
        this.ts = ts;
        this.vsuf = vsuf;
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

    public void setVsuf(ArrayList<Object> vsuf) {
        this.vsuf = vsuf;
    }
}
