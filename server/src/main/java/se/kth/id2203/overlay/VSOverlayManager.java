/*
 * The MIT License
 *
 * Copyright 2017 Lars Kroll <lkroll@kth.se>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package se.kth.id2203.overlay;

import com.larskroll.common.J6;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.bootstrapping.*;
import se.kth.id2203.broadcast.BEBDeliver;
import se.kth.id2203.broadcast.BEBPort;
import se.kth.id2203.broadcast.BEBRequest;
import se.kth.id2203.leaderelection.ELEPort;
import se.kth.id2203.leaderelection.Trust;
import se.kth.id2203.multipaxos.AscAbort;
import se.kth.id2203.multipaxos.AscDecide;
import se.kth.id2203.multipaxos.MPPort;
import se.kth.id2203.multipaxos.AscPropose;
import se.kth.id2203.networking.Message;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;

/**
 * The V(ery)S(imple)OverlayManager.
 * <p>
 * Keeps all nodes in a single partition in one replication group.
 * <p>
 * Note: This implementation does not fulfill the project task. You have to
 * support multiple partitions!
 * <p>
 * @author Lars Kroll <lkroll@kth.se>
 */
public class VSOverlayManager extends ComponentDefinition {

    final static Logger LOG = LoggerFactory.getLogger(VSOverlayManager.class);

    //******* Ports ******
    protected final Negative<Routing> route = provides(Routing.class);
    protected final Positive<Bootstrapping> boot = requires(Bootstrapping.class);
    protected final Positive<Network> net = requires(Network.class);
    protected final Positive<Timer> timer = requires(Timer.class);
    protected final Positive<BEBPort> beb = requires(BEBPort.class);
    protected final Positive<ELEPort> ele = requires(ELEPort.class);
    protected final Positive<MPPort> asc = requires(MPPort.class);

    //******* Fields ******
    final NetAddress self = config().getValue("id2203.project.address", NetAddress.class);
    private static LookupTable lut = null;

    private boolean isLeader = false;
    private NetAddress leader;

    //******* Handlers ******

    /**
     * Generate the initial lookuptable for the nodes in the network
     */
    protected final Handler<GetInitialAssignments> initialAssignmentHandler = new Handler<GetInitialAssignments>() {

        @Override
        public void handle(GetInitialAssignments event) {
            LOG.info("Generating LookupTable...");
            LookupTable lut = LookupTable.generate(event.nodes);
            LOG.debug("Generated assignments:\n{}", lut);
            trigger(new InitialAssignments(lut), boot);
        }
    };

    /**
     * When the bootstrapping is done and seeding the lookuptable is complete, the bootstrap server triggers this event
     */
    protected final Handler<Booted> bootHandler = new Handler<Booted>() {

        @Override
        public void handle(Booted event) {
            // Check that the lookuptable was generated successfully
            if (event.assignment instanceof LookupTable) {
                LOG.info("Got NodeAssignment, overlay ready.");
                lut = (LookupTable) event.assignment;   // Save the generated lut
            } else {
                LOG.error("Got invalid NodeAssignment type. Expected: LookupTable; Got: {}", event.assignment.getClass());
            }
        }
    };

    /**
     * RouteMsg event from ClientService, comes with a key and an operation to execute
     */
    protected final ClassMatchedHandler<RouteMsg, Message> routeHandler = new ClassMatchedHandler<RouteMsg, Message>() {

        @Override
        public void handle(RouteMsg content, Message context) {
            Collection<NetAddress> partition = lut.lookup(content.getKey());     //Check which partition the key is part of
            //if correct group
            if(partition.contains(self)) {
                //if im leader
                if(leader.equals(self)) {
                    //handle request
                    LOG.info("Request arrived at leader, starting to propose");
                    trigger(new AscPropose(content.getMsg()), asc);
                } else {
                    //send to leader
                    LOG.info("Forwarding to leader");
                    trigger(new Message(self, leader, content), net);
                }
            } else {
                //send to correct group
                LOG.info("Forwarding to correct group");
                BEBRequest bebRequest = new BEBRequest(new BEBDeliver(context.getSource(), content), partition);  // Create a request for all nodes in the partition
                trigger(bebRequest, beb);   // Broadcast to all nodes in the partition
            }
        }
    };
    protected final Handler<RouteMsg> localRouteHandler = new Handler<RouteMsg>() {

        @Override
        public void handle(RouteMsg event) {
            Collection<NetAddress> partition = lut.lookup(event.getKey());
            NetAddress target = J6.randomElement(partition);
            LOG.info("Routing message for key {} to {}", event.getKey(), target);
            trigger(new Message(self, target, event.getMsg()), net);
        }
    };

    /**
     * New incoming connection from a client
     */
    protected final ClassMatchedHandler<Connect, Message> connectHandler = new ClassMatchedHandler<Connect, Message>() {

        @Override
        public void handle(Connect content, Message context) {
            if (lut != null) {
                LOG.debug("Accepting connection request from {}", context.getSource());
                int size = lut.getNodes().size();
                trigger(new Message(self, context.getSource(), content.ack(size)), net);    //Return size of lut
            } else {
                LOG.info("Rejecting connection request from {}, as system is not ready, yet.", context.getSource());
            }
        }
    };

    /** Handle BEBDeliver events */
    protected final Handler<BEBDeliver> bebDeliverHandler = new Handler<BEBDeliver>() {
        @Override
        public void handle(BEBDeliver bebDeliver) {
            //if im the leader
            if(self.equals(leader)) {
                LOG.info("Leader got BEBDeliver, starting to propose");
                trigger(new AscPropose(bebDeliver.getData().getMsg()), asc);
            }
            //forward to leader
            else {
                LOG.info("Forwarding to leader");
                trigger(new Message(self, leader, bebDeliver.getData()), net);
            }
        }
    };

    /** Handle trust events, a new leader is elected */
    protected final Handler<Trust> trustHandler = new Handler<Trust>() {
        @Override
        public void handle(Trust trust) {
            if(trust.getLeader() != leader) {
                LOG.info("Got trust, new leader is {}", trust.getLeader().toString());
            }
            leader = trust.getLeader();
        }
    };

    /** Handle asc decide events, an operations has been decided upon */
    protected final Handler<AscDecide> ascDecideHandler = new Handler<AscDecide>() {
        @Override
        public void handle(AscDecide ascDecide) {
            LOG.info("Asc decide received");
            LOG.info("{}", (KompicsEvent)ascDecide.getValue());
            trigger(new Message(self, self, (KompicsEvent)ascDecide.getValue()), net);
        }
    };

    /** Handle asc abort events, a proposal was aborted for some reason */
    protected final Handler<AscAbort> ascAbortHandler = new Handler<AscAbort>() {
        @Override
        public void handle(AscAbort ascAbort) {
            System.out.println("Received ascabort, should do something?");
        }
    };

    {
        subscribe(initialAssignmentHandler, boot);
        subscribe(bootHandler, boot);
        subscribe(routeHandler, net);
        subscribe(localRouteHandler, route);
        subscribe(connectHandler, net);
        subscribe(bebDeliverHandler, beb);
        subscribe(trustHandler, ele);
        subscribe(ascDecideHandler, asc);
        subscribe(ascAbortHandler, asc);
    }
}
