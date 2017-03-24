package se.kth.id2203.failuredetector;

import org.slf4j.LoggerFactory;
import se.kth.id2203.bootstrapping.Booted;
import se.kth.id2203.bootstrapping.Bootstrapping;
import se.kth.id2203.networking.NetAddress;
import se.kth.id2203.overlay.LookupTable;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timeout;
import se.sics.kompics.timer.Timer;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Felicia & Mikael on 2017-02-22.
 */
public class EPFD extends ComponentDefinition {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(EPFD.class);

    private Positive<Network> net = requires(Network.class);
    private Positive<Timer> timer = requires(Timer.class);
    private Positive<Bootstrapping> boot = requires(Bootstrapping.class);
    private Negative<EPFDPort> epfd = provides(EPFDPort.class);

    private Collection<NetAddress> replicationgroup;
    private ArrayList<NetAddress> suspected;
    private ArrayList<NetAddress> alive;

    private final NetAddress self = config().getValue("id2203.project.address", NetAddress.class);

    private int heartbeatDelay = 2000;
    private int heartbeatIncrease = 200;
    private LookupTable lut;
    private int seqnum;

    /** Booted is used to obtain the node assignment in order get replication groups etc. */
    protected final Handler<Booted> bootHandler = new Handler<Booted>() {

        @Override
        public void handle(Booted event) {
            suspected = new ArrayList<>();
            alive = new ArrayList<>();
            seqnum = 0;

            lut = (LookupTable) event.assignment;
            //Populate replication group
            for(Integer key : lut.getKeys()) {
                if(lut.lookup(key.toString()).contains(self)) {
                    replicationgroup = lut.lookup(key.toString());
                    break;
                }
            }

            alive.addAll(replicationgroup);
            startTimeout(heartbeatDelay);       //Start the heartbeat timeout
        }
    };

    /** Handle heartbeat timeout events */
    protected final Handler<HeartbeatTimeout> heartbeatTimeoutHandler = new Handler<HeartbeatTimeout>() {

        @Override
        public void handle(HeartbeatTimeout e) {
            ArrayList<NetAddress> insect = new ArrayList<>(alive);
            insect.retainAll(suspected);

            if(!insect.isEmpty()) {
                heartbeatDelay += heartbeatIncrease;
            }

            seqnum += 1;

            for(NetAddress node : replicationgroup) {
                /** A node has not answered to heartbeat and was not previously suspected,
                 * add it to list of suspected nodes */
                if(!alive.contains(node) && !suspected.contains(node)) {
                    suspected.add(node);
                    LOG.info("*********" + self.toString() + " suspects: " + node.toString() + "*********");
                    trigger(new Suspect(node), epfd);
                    LOG.info(self.toString() + " suspected set contains: " + suspected);
                }
                /** A node replied to hearbeat and was previously suspected, make it live again :) */
                else if(alive.contains(node) && suspected.contains(node)) {
                    suspected.remove(node);
                    LOG.info("*********" + self.toString() + " restores: " + node.toString() + "*********");
                    trigger(new Restore(node), epfd);
                }
                /** Send a new heartbeat */
                trigger(new HBRequest(self, node, seqnum), net);
            }
            alive.clear();

            startTimeout(heartbeatDelay);
        }
    };

    private void startTimeout(int delay) {
        ScheduleTimeout st = new ScheduleTimeout(delay);
        HeartbeatTimeout timeout = new HeartbeatTimeout(st);
        st.setTimeoutEvent(timeout);
        trigger(st, timer);
    }

    public static class HeartbeatTimeout extends Timeout {
        public HeartbeatTimeout(ScheduleTimeout spt) {
            super(spt);
        }
    }

    /** Handle heartbeat requests */
    protected final Handler<HBRequest> hbRequestHandler = new Handler<HBRequest>() {
        @Override
        public void handle(HBRequest hbRequest) {
            //Respond to heartbeat request
            trigger(new HBResponse(self, hbRequest.getSource(), hbRequest.getSeqnum()), net);
        }
    };

    /** Handle heartbeat responses */
    protected final Handler<HBResponse> hbResponseHandler = new Handler<HBResponse>() {
        @Override
        public void handle(HBResponse hbResponse) {
            //Check if correct sequence number or if the responding node is suspected
            if((hbResponse.getSeqnum() == seqnum) || suspected.contains(hbResponse.getSource())) {
                alive.add(hbResponse.getSource());
            }
        }
    };

    {
        subscribe(heartbeatTimeoutHandler, timer);
        subscribe(hbRequestHandler, net);
        subscribe(hbResponseHandler, net);
        subscribe(bootHandler, boot);
    }
}
