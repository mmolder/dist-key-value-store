package se.kth.id2203.multipaxos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.bootstrapping.Booted;
import se.kth.id2203.bootstrapping.Bootstrapping;
import se.kth.id2203.networking.Message;
import se.kth.id2203.networking.NetAddress;
import se.kth.id2203.overlay.LookupTable;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;

import java.util.*;

/**
 * Created by Felicia & Mikael on 2017-03-01.
 */
public class AbortableSequenceConsensus extends ComponentDefinition {

    protected final Positive<Network> net = requires(Network.class);
    protected final Positive<Bootstrapping> boot = requires(Bootstrapping.class);
    protected final Negative<MPPort> asc = provides(MPPort.class);

    private NetAddress self = config().getValue("id2203.project.address", NetAddress.class);
    private Collection<NetAddress> replicationgroup;

    private LookupTable lut;

    private int t;                                  //Logical clock
    private int prepts;                             //Prepared timestamp
    private ArrayList<Object> acceptedSequence;     //Accepted sequence
    private int ats;                                //Acceptor timestamp
    private int al;                                 //Length of decided seq
    private ArrayList<Object> proposedSequence;     //Proposed sequence
    private int pts;                                //Proposer timestamp
    private int pl;                                 //Length of learned seq
    private ArrayList<Object> proposedValues;       //Proposed values

    private HashMap<NetAddress, RLContent> readlist;
    private HashMap<NetAddress, Integer> accepted;
    private HashMap<NetAddress, Integer> decided;
    private Object currProp;

    private int ts_prime;
    private ArrayList<Object> vsuf_prime;
    private int num;

    final static Logger LOG = LoggerFactory.getLogger(AbortableSequenceConsensus.class);

    protected final Handler<Booted> bootedHandler = new Handler<Booted>() {
        @Override
        public void handle(Booted booted) {
            //populate replication group
            lut = (LookupTable) booted.assignment;
            for(Integer key : lut.getKeys()) {
                if(lut.lookup(key.toString()).contains(self)) {
                    replicationgroup = lut.lookup(key.toString());
                    break;
                }
            }

            t = 0;
            prepts = 0;
            acceptedSequence = new ArrayList<>();
            ats = 0;
            al = 0;
            proposedSequence = new ArrayList<>();
            pts = 0;
            pl = 0;
            proposedValues = new ArrayList<>();
            readlist = new HashMap<>();
            accepted = new HashMap<>();
            decided = new HashMap<>();
        }
    };

    /** Handle proposals. This will only get triggered by the leader of a replication group */
    protected final Handler<AscPropose> proposeHandler = new Handler<AscPropose>() {
        @Override
        public void handle(AscPropose ascPropose) {
            t += 1;     //increase clock
            Object proposal = ascPropose.getProposal();     //the current proposal
            currProp = proposal;
            //Initial proposal
            if(pts == 0) {
                LOG.info("{} Got initial proposal", self);
                pts = (t * replicationgroup.size()) + self.getPort();
                proposedSequence = prefix(acceptedSequence, al);
                pl = 0;
                proposedValues.add(proposal);
                readlist.clear();
                accepted = new HashMap<>();
                decided = new HashMap<>();

                //send prepare messages to all nodes in replication group
                for(NetAddress node : replicationgroup) {
                    LOG.info("{} Sending prepare message", self);
                    trigger(new Message(self, node, new Prepare(pts, al, t)), net);
                }
            }
            //not initial proposal and majority is not met
            else if(readlist.size() <= replicationgroup.size()/2) {
                proposedValues.add(proposal);
            }
            //majority is met and proposal is not previously proposed
            else if(!proposedSequence.contains(proposal)) {
                proposedSequence.add(proposal);
                for(NetAddress node : replicationgroup) {
                    if(readlist.containsKey(node)) {
                        ArrayList<Object> prop = new ArrayList<>();
                        prop.add(proposal);
                        LOG.info("{} Sending accept", self);
                        trigger(new Message(self, node, new Accept(pts, prop, proposedSequence.size() - 1, t)), net);
                    }
                }
            }

        }
    };

    /** Handle prepare events */
    protected final ClassMatchedHandler<Prepare, Message> prepareHandler = new ClassMatchedHandler<Prepare, Message>() {
        @Override
        public void handle(Prepare prepare, Message context) {
            t = Math.max(t, prepare.getT()) + 1;    //update clock to the highest of the received and own and increase by one
            if(prepare.getTs() < prepts) {
                LOG.info("{} Bad timestamp, sending nack", self);
                trigger(new Message(self, context.getSource(), new Nack(prepare.getTs(), t)), net);
            }
            else {
                LOG.info("{} Sending prepack", self);
                prepts = prepare.getTs();
                trigger(new Message(self, context.getSource(),
                        new PrepAck(prepare.getTs(), ats, suffix(acceptedSequence, prepare.getL()), al, t)), net);
            }
        }
    };

    /** Handle nack events, something went wrong... */
    protected final ClassMatchedHandler<Nack, Message> nackHandler = new ClassMatchedHandler<Nack, Message>() {
        @Override
        public void handle(Nack nack, Message context) {
            t = Math.max(t, nack.getT()) + 1;
            if(nack.getTs() == pts) {
                LOG.info("{} Sending abort", self);
                pts = 0;
                trigger(new AscAbort(currProp), asc);
            }
        }
    };

    /** Handle prepare ack events */
    protected final ClassMatchedHandler<PrepAck, Message> prepAckHandler = new ClassMatchedHandler<PrepAck, Message>() {
        @Override
        public void handle(PrepAck prepAck, Message context) {
            t = Math.max(t, prepAck.getT()) + 1;    //update clock to the highest of the received and own and increase by one
            //its the ack we expected
            if(prepAck.getTs() == pts) {
                readlist.put(context.getSource(), new RLContent(prepAck.getTs(), prepAck.getSuffix()));
                decided.put(context.getSource(), prepAck.getAl());
                //majority met
                if(readlist.size() == (replicationgroup.size()/2 + 1)) {
                    ts_prime = 0;
                    vsuf_prime = new ArrayList<>();

                    for(Map.Entry<NetAddress, RLContent> entry : readlist.entrySet()) {
                        if((ts_prime < entry.getValue().getTs()) || ((ts_prime == entry.getValue().getTs()) && vsuf_prime.size() < entry.getValue().getVsuf().size())) {
                            ts_prime = entry.getValue().getTs();
                            vsuf_prime.addAll(entry.getValue().getVsuf());
                        }
                    }

                    proposedSequence.addAll(vsuf_prime);
                    for(Object v : proposedValues) {

                        if(!proposedSequence.contains(v)) {
                            proposedSequence.add(v);
                        }
                    }
                    for(NetAddress node : replicationgroup) {
                        if(readlist.containsKey(node)) {
                            int l_prime = decided.get(node);
                            LOG.info("{} Sending accept", self);
                            trigger(new Message(self, node, new Accept(pts, suffix(proposedSequence, l_prime), l_prime, t)), net);
                        }
                    }
                }
                //more than majority
                else if(readlist.size() > (replicationgroup.size()/2 + 1)) {
                    LOG.info("{} Sending accept", self);
                    trigger(new Message(self, context.getSource(), new Accept(pts, suffix(proposedSequence, prepAck.getAl()), prepAck.getAl(), t)), net);
                    if(pl != 0) {
                        LOG.info("{} Sending decide", self);
                        trigger(new Message(self, context.getSource(), new Decide(pts, pl, t)), net);
                    }
                }
            }
        }
    };

    /** Handle accept events */
    protected final ClassMatchedHandler<Accept, Message> acceptHandler = new ClassMatchedHandler<Accept, Message>(){
        @Override
        public void handle(Accept accept, Message context) {
            t = Math.max(t, accept.getT()) + 1;     //update clock to the highest of the received and own and increase by one
            //not the accept we expected
            if(accept.getTs() != prepts) {
                LOG.info("{} Sending nack", self);
                trigger(new Message(self, context.getSource(), new Nack(accept.getTs(), t)), net);
            }
            else {
                ats = accept.getTs();
                if(accept.getOffs() < acceptedSequence.size()) {
                    acceptedSequence = prefix(acceptedSequence, accept.getOffs());
                }
                acceptedSequence.addAll(accept.getVsuf());
                LOG.info("{} Sending accept ack", self);
                trigger(new Message(self, context.getSource(), new AccAck(accept.getTs(), acceptedSequence.size(), t)), net);
            }
        }
    };

    /** Handle accept acc events */
    protected final ClassMatchedHandler<AccAck, Message> accAckHandler = new ClassMatchedHandler<AccAck, Message>(){
        @Override
        public void handle(AccAck accAck, Message context){
            t = Math.max(t, accAck.getT()) + 1;     //update clock to the highest of the received and own and increase by one
            //its the ack we expected
            if(accAck.getTs() == pts) {
                accepted.put(context.getSource(), accAck.getAvLength());
                num = 0;
                for(Map.Entry<NetAddress, Integer> node : accepted.entrySet()) {
                    int length = accepted.get(node.getKey());
                    if(length >= accAck.getAvLength()) {
                        num++;
                    }
                }
                if((pl < accAck.getAvLength()) && (num > replicationgroup.size()/2)) {
                   pl = accAck.getAvLength();
                   for(NetAddress p : replicationgroup){
                       if(readlist.containsKey(p)) {
                           LOG.info("{} Sending decide", self);
                           trigger(new Message(self, p, new Decide(pts, pl, t)), net);
                       }
                   }
                }
            }
        }
    };

    /** Handle decide events */
    protected final ClassMatchedHandler<Decide, Message> decideHandler = new ClassMatchedHandler<Decide, Message>() {
        @Override
        public void handle(Decide decide, Message context) {
            t = Math.max(t, decide.getT()) + 1;     //update clock to the highest of the received and own and increase by one
            if(decide.getTs() == prepts) {
                while(al < decide.getL()) {
                    LOG.info("{} Sending asc decide", self);
                    trigger(new AscDecide(acceptedSequence.get(al)), asc);
                    al += 1;
                }
            }
        }
    };

    /** Get the prefix of an arraylist from index 0 to al */
    private ArrayList<Object> prefix(ArrayList<Object> acceptedSequence, int al) {
        ArrayList<Object> prefix = new ArrayList<>();
        for(int i = 0; i < al; i++) {
            prefix.add(acceptedSequence.get(i));
        }
        return prefix;
    }

    /** Get the suffix of an arraylist from index al to acceptedSequence.size */
    private ArrayList<Object> suffix(ArrayList<Object> acceptedSequence, int al) {
        return new ArrayList<Object>(acceptedSequence.subList(al, acceptedSequence.size()));
    }

    {
        subscribe(bootedHandler, boot);
        subscribe(proposeHandler, asc);
        subscribe(prepareHandler, net);
        subscribe(nackHandler, net);
        subscribe(prepAckHandler, net);
        subscribe(acceptHandler, net);
        subscribe(accAckHandler, net);
        subscribe(decideHandler, net);
    }

}
