package se.kth.id2203.leaderelection;

import se.kth.id2203.bootstrapping.Booted;
import se.kth.id2203.bootstrapping.Bootstrapping;
import se.kth.id2203.failuredetector.EPFDPort;
import se.kth.id2203.failuredetector.Restore;
import se.kth.id2203.failuredetector.Suspect;
import se.kth.id2203.networking.NetAddress;
import se.kth.id2203.overlay.LookupTable;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Felicia & Mikael on 2017-02-26.
 */
public class ELE extends ComponentDefinition {

    private Positive<EPFDPort> epfd = requires(EPFDPort.class);
    private Positive<Bootstrapping> boot = requires(Bootstrapping.class);
    private Positive<Network> net = requires(Network.class);
    private Negative<ELEPort> ele = provides(ELEPort.class);

    private NetAddress self = config().getValue("id2203.project.address", NetAddress.class);
    private NetAddress leader;
    private ArrayList<NetAddress> suspected;
    private Collection<NetAddress> replicationgroup;

    private LookupTable lut;

    /** Booted is used to obtain the node assignment in order get replication groups etc. */
    protected final Handler<Booted> bootHandler = new Handler<Booted>() {

        @Override
        public void handle(Booted event) {
            suspected = new ArrayList<>();

            lut = (LookupTable) event.assignment;

            //populate replication group
            for(Integer key : lut.getKeys()) {
                if(lut.lookup(key.toString()).contains(self)) {
                    replicationgroup = lut.lookup(key.toString());
                    break;
                }
            }

            leader = getHighestRank(getAliveNodes());   //Get initial leader
            trigger(new Trust(leader), ele);
        }
    };

    /** Handle Suspect events, this means one node is suspected to have died */
    protected final Handler<Suspect> suspectHandler = new Handler<Suspect>() {
        @Override
        public void handle(Suspect suspect) {
            suspected.add(suspect.getSrc());
            //Check if current leader was suspected
            if(suspect.getSrc().equals(leader)) {
                //Elect new leader
                leader = getHighestRank(getAliveNodes());
                trigger(new Trust(leader), ele);
            }
        }
    };

    /** Handle Restore events, this means that a node which was previously suspected has answered on a heartbeat */
    protected final Handler<Restore> restoreHandler =  new Handler<Restore>() {
        @Override
        public void handle(Restore restore) {
            suspected.remove(restore.getSrc());
            //Check if newly restored node is better fit to be leader
            NetAddress best = getHighestRank(getAliveNodes());
            if(!leader.equals(best)) {
                leader = best;
                trigger(new Trust(leader), ele);
            }
        }
    };

    /** Get the highest rank of the nodes in the replication group, rank is determined by port number */
    private NetAddress getHighestRank(ArrayList<NetAddress> nodes) {
        NetAddress highestRank = self;
        for(NetAddress node : nodes) {
            if(node.getPort() < highestRank.getPort()) {
                highestRank = node;
            }
        }
        return highestRank;
    }

    /** Get a list of nodes in the replication group which are not in the suspected list */
    private ArrayList<NetAddress> getAliveNodes() {
        ArrayList<NetAddress> alive = new ArrayList<>();
        ArrayList<NetAddress> repgroup = new ArrayList<>(replicationgroup);
        for(NetAddress node : repgroup) {
            if(!suspected.contains(node)) {
                alive.add(node);
            }
        }
        return alive;
    }

    {
        subscribe(suspectHandler, epfd);
        subscribe(restoreHandler, epfd);
        subscribe(bootHandler, boot);
    }

}
