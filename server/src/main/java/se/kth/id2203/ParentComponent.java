package se.kth.id2203;

import com.google.common.base.Optional;
import se.kth.id2203.bootstrapping.BootstrapClient;
import se.kth.id2203.bootstrapping.BootstrapServer;
import se.kth.id2203.bootstrapping.Bootstrapping;
import se.kth.id2203.broadcast.BEB;
import se.kth.id2203.broadcast.BEBPort;
import se.kth.id2203.failuredetector.EPFD;
import se.kth.id2203.failuredetector.EPFDPort;
import se.kth.id2203.kvstore.KVService;
import se.kth.id2203.leaderelection.ELE;
import se.kth.id2203.leaderelection.ELEPort;
import se.kth.id2203.multipaxos.AbortableSequenceConsensus;
import se.kth.id2203.multipaxos.MPPort;
import se.kth.id2203.networking.NetAddress;
import se.kth.id2203.overlay.Routing;
import se.kth.id2203.overlay.VSOverlayManager;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;

public class ParentComponent
        extends ComponentDefinition {

    //******* Ports ******
    protected final Positive<Network> net = requires(Network.class);
    protected final Positive<Timer> timer = requires(Timer.class);
    //******* Children ******
    protected final Component overlay = create(VSOverlayManager.class, Init.NONE);
    protected final Component kv = create(KVService.class, Init.NONE);
    protected final Component boot;
    protected final Component beb = create(BEB.class, Init.NONE);
    protected final Component epfd = create(EPFD.class, Init.NONE);
    protected final Component ele = create(ELE.class, Init.NONE);
    protected final Component asc = create(AbortableSequenceConsensus.class, Init.NONE);

    {

        Optional<NetAddress> serverO = config().readValue("id2203.project.bootstrap-address", NetAddress.class);
        if (serverO.isPresent()) { // start in client mode
            boot = create(BootstrapClient.class, Init.NONE);
        } else { // start in server mode
            boot = create(BootstrapServer.class, Init.NONE);
        }
        connect(timer, boot.getNegative(Timer.class), Channel.TWO_WAY);
        connect(net, boot.getNegative(Network.class), Channel.TWO_WAY);
        // Overlay
        connect(boot.getPositive(Bootstrapping.class), overlay.getNegative(Bootstrapping.class), Channel.TWO_WAY);
        connect(net, overlay.getNegative(Network.class), Channel.TWO_WAY);
        // KV
        connect(overlay.getPositive(Routing.class), kv.getNegative(Routing.class), Channel.TWO_WAY);
        connect(net, kv.getNegative(Network.class), Channel.TWO_WAY);

        // BEB
        connect(overlay.getNegative(BEBPort.class), beb.getPositive(BEBPort.class), Channel.TWO_WAY);
        connect(net, beb.getNegative(Network.class), Channel.TWO_WAY);

        // EPFD
        connect(ele.getNegative(EPFDPort.class), epfd.getPositive(EPFDPort.class), Channel.TWO_WAY);
        connect(boot.getPositive(Bootstrapping.class), epfd.getNegative(Bootstrapping.class), Channel.TWO_WAY);
        connect(timer, epfd.getNegative(Timer.class), Channel.TWO_WAY);
        connect(net, epfd.getNegative(Network.class), Channel.TWO_WAY);

        //ELE
        connect(overlay.getNegative(ELEPort.class), ele.getPositive(ELEPort.class), Channel.TWO_WAY);
        connect(boot.getPositive(Bootstrapping.class), ele.getNegative(Bootstrapping.class), Channel.TWO_WAY);
        connect(net, ele.getNegative(Network.class), Channel.TWO_WAY);

        //ASC
        connect(overlay.getNegative(MPPort.class), asc.getPositive(MPPort.class), Channel.TWO_WAY);
        connect(boot.getPositive(Bootstrapping.class), asc.getNegative(Bootstrapping.class), Channel.TWO_WAY);
        connect(net, asc.getNegative(Network.class), Channel.TWO_WAY);
    }
}
