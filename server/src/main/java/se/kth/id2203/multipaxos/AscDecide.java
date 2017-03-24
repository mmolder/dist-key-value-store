package se.kth.id2203.multipaxos;

import se.sics.kompics.KompicsEvent;

/**
 * Created by Felicia & Mikael on 2017-03-03.
 */
public class AscDecide implements KompicsEvent {
    private Object value;

    public AscDecide(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
