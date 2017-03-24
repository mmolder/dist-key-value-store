package se.kth.id2203.multipaxos;

import se.sics.kompics.KompicsEvent;

/**
 * Created by Felicia & Mikael on 2017-03-01.
 */
public class AscPropose implements KompicsEvent {
    private Object proposal;

    public AscPropose(Object proposal) {
        this.proposal = proposal;
    }

    public Object getProposal() {
        return proposal;
    }

    public void setProposal(Object proposal) {
        this.proposal = proposal;
    }
}
