package se.kth.id2203.broadcast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.networking.Message;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;

import java.util.Collection;

/**
 * Created by Felicia & Mikael on 2017-02-21.
 */
public class BEB extends ComponentDefinition {

    final static Logger LOG = LoggerFactory.getLogger(BEB.class);

    private Positive<Network> net = requires(Network.class);
    private Negative<BEBPort> beb = provides(BEBPort.class);

    private NetAddress self = config().getValue("id2203.project.address", NetAddress.class);

    private Handler<BEBRequest> broadcastHandler = new Handler<BEBRequest>() {
        @Override
        public void handle(BEBRequest event) {
            Collection<NetAddress> nodes = event.getBroadcastNodes();   //Nodes to deliver to
            for (NetAddress node : nodes) {
                trigger(new Message(self, node, event.getDeliverEvent()), net); //Forward the BEBDeliver event
            }
        }
    };

    private ClassMatchedHandler<BEBDeliver, Message> deliverHandler = new ClassMatchedHandler<BEBDeliver, Message>() {
        @Override
        public void handle(BEBDeliver event, Message message) {
            LOG.info("BEBDeliver to " + self);
            trigger(event, beb);    //To be sent to the VSOverlayManager of the receiving node
        }
    };

    {
        subscribe(broadcastHandler, beb);
        subscribe(deliverHandler, net);
    }
}
