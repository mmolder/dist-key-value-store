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
package se.kth.id2203.simulation;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import se.kth.id2203.ParentComponent;
import se.kth.id2203.kvstore.*;
import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Init;
import se.sics.kompics.KompicsEvent;
import se.sics.kompics.Start;
import se.sics.kompics.network.Address;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.adaptor.Operation1;
import se.sics.kompics.simulator.adaptor.Operation2;
import se.sics.kompics.simulator.adaptor.distributions.ConstantDistribution;
import se.sics.kompics.simulator.adaptor.distributions.extra.BasicIntSequentialDistribution;
import se.sics.kompics.simulator.events.system.KillNodeEvent;
import se.sics.kompics.simulator.events.system.StartNodeEvent;


/**
 *
 * @author Lars Kroll <lkroll@kth.se>
 */
public abstract class ScenarioGen {

    private static final Operation1 startServerOp = new Operation1<StartNodeEvent, Integer>() {

        @Override
        public StartNodeEvent generate(final Integer self) {
            return new StartNodeEvent() {
                final NetAddress selfAdr;
                final NetAddress bsAdr;

                {
                    try {
                        selfAdr = new NetAddress(InetAddress.getByName("192.168.0.1"), 45678 + self);
                        bsAdr = new NetAddress(InetAddress.getByName("192.168.0.1"), 45679);
                    } catch (UnknownHostException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                @Override
                public Address getNodeAddress() {
                    return selfAdr;
                }

                @Override
                public Class getComponentDefinition() {
                    return ParentComponent.class;
                }

                @Override
                public String toString() {
                    return "StartNode<" + selfAdr.toString() + ">";
                }

                @Override
                public Init getComponentInit() {
                    return Init.NONE;
                }

                @Override
                public Map<String, Object> initConfigUpdate() {
                    HashMap<String, Object> config = new HashMap<>();
                    config.put("id2203.project.address", selfAdr);
                    if (self != 1) { // don't put this at the bootstrap server, or it will act as a bootstrap client
                        config.put("id2203.project.bootstrap-address", bsAdr);
                    }
                    return config;
                }
            };
        }
    };

    private static final Operation1 startClientOp = new Operation1<StartNodeEvent, Integer>() {

        @Override
        public StartNodeEvent generate(final Integer self) {
            return new StartNodeEvent() {
                final NetAddress selfAdr;
                final NetAddress bsAdr;

                {
                    try {
                        selfAdr = new NetAddress(InetAddress.getByName("192.168.1." + self), 45678);
                        bsAdr = new NetAddress(InetAddress.getByName("192.168.0.1"), 45678);
                    } catch (UnknownHostException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                @Override
                public Address getNodeAddress() {
                    return selfAdr;
                }

                @Override
                public Class getComponentDefinition() {
                    return ScenarioClient.class;
                }

                @Override
                public String toString() {
                    return "StartClient<" + selfAdr.toString() + ">";
                }

                @Override
                public Init getComponentInit() {
                    return Init.NONE;
                }

                @Override
                public Map<String, Object> initConfigUpdate() {
                    HashMap<String, Object> config = new HashMap<>();
                    config.put("id2203.project.address", selfAdr);
                    config.put("id2203.project.bootstrap-address", bsAdr);
                    return config;
                }
            };
        }
    };


    private static final Operation1 killNodeOp = new Operation1<KillNodeEvent, Integer>() {
        @Override
        public KillNodeEvent generate(final Integer killPort) {
            return new KillNodeEvent() {
                NetAddress selfAdr;

                {
                    try {
                        selfAdr = new NetAddress(InetAddress.getByName("192.168.0.1"), killPort);
                    } catch (UnknownHostException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                @Override
                public Address getNodeAddress() {
                    return selfAdr;
                }
            };
        }
    };

    private static final Operation1 resurrectLeaderNode = new Operation1<StartNodeEvent, Integer>(){
        @Override
        public StartNodeEvent generate(final Integer self) {
            return new StartNodeEvent() {
                NetAddress selfAdr;
                NetAddress bsAdr;
                {
                    try {
                        selfAdr = new NetAddress(InetAddress.getByName("192.168.0.1"), 45680);
                        bsAdr = new NetAddress(InetAddress.getByName("192.168.0.1"), 45679);

                    } catch (UnknownHostException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                @Override
                public Address getNodeAddress() {
                    return selfAdr;
                }

                @Override
                public Class<? extends ComponentDefinition> getComponentDefinition() {
                    return ParentComponent.class;
                }

                @Override
                public Init getComponentInit() {
                    return null;
                }

                @Override
                public Map<String, Object> initConfigUpdate() {
                    HashMap<String, Object> config = new HashMap<>();
                    config.put("id2203.project.address", selfAdr);
                    if (self != 1) { // don't put this at the bootstrap server, or it will act as a bootstrap client
                        config.put("id2203.project.bootstrap-address", bsAdr);
                    }
                    return config;
                }
            };
        }
    };

    private static final Operation2 getOp = new Operation2<StartNodeEvent, Integer, Integer>() {
        @Override
        public StartNodeEvent generate(final Integer source, final Integer target) {
            return new StartNodeEvent() {
                NetAddress sourceAdr;
                NetAddress targetAdr;

                {
                    try {
                        sourceAdr = new NetAddress(InetAddress.getByName("192.168.0.1"), 45669);
                        targetAdr = new NetAddress(InetAddress.getByName("192.168.0.1"), 45679);
                    } catch (UnknownHostException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                @Override
                public Address getNodeAddress() {
                    return sourceAdr;
                }

                @Override
                public Class getComponentDefinition() {
                    return GetTest.class;
                }

                @Override
                public String toString() {
                    return "StartClient<" + sourceAdr.toString() + ">";
                }

                @Override
                public Init getComponentInit() {
                    return Init.NONE;
                }

                @Override
                public Map<String, Object> initConfigUpdate() {
                    HashMap<String, Object> config = new HashMap<>();
                    config.put("id2203.project.address", sourceAdr);
                    config.put("id2203.project.bootstrap-address", targetAdr);
                    return config;
                }
            };
        }
    };

    private static final Operation2 putOp = new Operation2<StartNodeEvent, Integer, Integer>() {
        @Override
        public StartNodeEvent generate(final Integer source, final Integer target) {
            return new StartNodeEvent() {
                NetAddress sourceAdr;
                NetAddress targetAdr;

                {
                    try {
                        sourceAdr = new NetAddress(InetAddress.getByName("192.168.0.1"), 45668);
                        targetAdr = new NetAddress(InetAddress.getByName("192.168.0.1"), 45679);
                    } catch (UnknownHostException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                @Override
                public Address getNodeAddress() {
                    return sourceAdr;
                }

                @Override
                public Class<? extends ComponentDefinition> getComponentDefinition() {
                    return PutTest.class;
                }

                @Override
                public Init getComponentInit() {
                    return null;
                }

                @Override
                public Map<String, Object> initConfigUpdate() {
                    HashMap<String, Object> config = new HashMap<>();
                    config.put("id2203.project.address", sourceAdr);
                    config.put("id2203.project.bootstrap-address", targetAdr);
                    return config;
                }
            };
        }
    };

    private static final Operation2 casOp = new Operation2<StartNodeEvent, Integer, Integer>() {
        @Override
        public StartNodeEvent generate(final Integer source, final Integer target) {
            return new StartNodeEvent() {
                NetAddress sourceAdr;
                NetAddress targetAdr;

                {
                    try {
                        sourceAdr = new NetAddress(InetAddress.getByName("192.168.0.1"), 45667);
                        targetAdr = new NetAddress(InetAddress.getByName("192.168.0.1"), 45679);
                    } catch (UnknownHostException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                @Override
                public Address getNodeAddress() {
                    return sourceAdr;
                }

                @Override
                public Class<? extends ComponentDefinition> getComponentDefinition() {
                    return CasTest.class;
                }

                @Override
                public Init getComponentInit() {
                    return null;
                }

                @Override
                public Map<String, Object> initConfigUpdate() {
                    HashMap<String, Object> config = new HashMap<>();
                    config.put("id2203.project.address", sourceAdr);
                    config.put("id2203.project.bootstrap-address", targetAdr);
                    return config;
                }
            };
        }
    };

    public static SimulationScenario simpleOps(final int servers) {
        return new SimulationScenario() {
            {
                SimulationScenario.StochasticProcess startCluster = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(servers, startServerOp, new BasicIntSequentialDistribution(1));
                    }
                };

                SimulationScenario.StochasticProcess startClients = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, startClientOp, new BasicIntSequentialDistribution(1));
                    }
                };
                startCluster.start();
                startClients.startAfterTerminationOf(10000, startCluster);
                terminateAfterTerminationOf(100000, startClients);
            }
        };
    }

    public static SimulationScenario initialReplicationGroups(final int servers) {
        SimulationScenario testReplicaitonGroups = new SimulationScenario() {
            {
                SimulationScenario.StochasticProcess startServer = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(0));
                        raise(servers, startServerOp, new BasicIntSequentialDistribution(1));
                    }
                };

                SimulationScenario.StochasticProcess getOperation = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(0));
                        raise(1, getOp, new BasicIntSequentialDistribution(1), new BasicIntSequentialDistribution(2));

                    }
                };
                startServer.start();
                getOperation.startAfterStartOf(10000,startServer);

                terminateAfterTerminationOf(10000,getOperation);
            }
        };
        return testReplicaitonGroups;
    }

    public static SimulationScenario testOperations(final int servers) {
        SimulationScenario testOps = new SimulationScenario() {
            {
                SimulationScenario.StochasticProcess startServers = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(0));
                        raise(servers, startServerOp, new BasicIntSequentialDistribution(1));
                    }
                };

                SimulationScenario.StochasticProcess startPutClient = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(0));
                        raise(1, putOp, new BasicIntSequentialDistribution(1), new BasicIntSequentialDistribution(2));
                    }
                };

                SimulationScenario.StochasticProcess startCasClient = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(0));
                        raise(1, casOp, new BasicIntSequentialDistribution(1), new BasicIntSequentialDistribution(2));
                    }
                };

                SimulationScenario.StochasticProcess startGetClient = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(0));
                        raise(1, getOp, new BasicIntSequentialDistribution(1), new BasicIntSequentialDistribution(2));
                    }
                };

                startServers.start();
                startPutClient.startAfterStartOf(10000, startServers);
                startCasClient.startAfterTerminationOf(10000, startPutClient);
                startGetClient.startAfterTerminationOf(10000, startCasClient);
                terminateAfterTerminationOf(10000, startGetClient);
            }
        };
        return testOps;
    }

    public static SimulationScenario testBeb(final int servers) {
        SimulationScenario bebTest = new SimulationScenario() {
            {
                SimulationScenario.StochasticProcess startServers = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(0));
                        raise(servers, startServerOp, new BasicIntSequentialDistribution(1));
                    }
                };

                SimulationScenario.StochasticProcess kill = new SimulationScenario.StochasticProcess(){
                    {
                        eventInterArrivalTime(constant(0));
                        raise(1, killNodeOp, new ConstantDistribution<>(Integer.class, 45680));
                    }
                };

                SimulationScenario.StochasticProcess startGetClient = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(0));
                        raise(1, getOp, new BasicIntSequentialDistribution(1), new BasicIntSequentialDistribution(2));
                    }
                };
                startServers.start();
                kill.startAfterTerminationOf(10000, startServers);
                startGetClient.startAfterTerminationOf(10000, kill);
                terminateAfterTerminationOf(10000, startGetClient);
            }
        };
        return bebTest;
    }
    public static SimulationScenario testEpfd(final int servers){
        SimulationScenario epfdTest = new SimulationScenario() {
            {
                SimulationScenario.StochasticProcess startServers = new SimulationScenario.StochasticProcess(){
                    {
                        eventInterArrivalTime(constant(0));
                        raise(servers, startServerOp, new BasicIntSequentialDistribution(1));
                    }
                };

                SimulationScenario.StochasticProcess kill = new SimulationScenario.StochasticProcess(){
                    {
                        eventInterArrivalTime(constant(0));
                        raise(1, killNodeOp, new ConstantDistribution<>(Integer.class, 45680));
                    }
                };
                startServers.start();
                kill.startAfterTerminationOf(10000, startServers);
                terminateAfterTerminationOf(10000, kill);
            }
        };
        return epfdTest;
    }

    public static SimulationScenario testEle(final int servers){
        SimulationScenario eleTest = new SimulationScenario() {
            {
                SimulationScenario.StochasticProcess startServers = new SimulationScenario.StochasticProcess(){
                    {
                       eventInterArrivalTime(constant(0));
                       raise(servers, startServerOp, new BasicIntSequentialDistribution(1));
                    }
                };
                SimulationScenario.StochasticProcess kill = new SimulationScenario.StochasticProcess(){
                    {
                        eventInterArrivalTime(constant(0));
                        raise(1, killNodeOp, new ConstantDistribution<>(Integer.class, 45680));
                    }
                };
                SimulationScenario.StochasticProcess restart = new SimulationScenario.StochasticProcess(){
                    {
                        eventInterArrivalTime(constant(0));
                        raise(1, resurrectLeaderNode, new BasicIntSequentialDistribution(1));
                    }
                };
                startServers.start();
                kill.startAfterTerminationOf(10000, startServers);
                restart.startAfterTerminationOf(10000, kill);
                terminateAfterTerminationOf(10000, restart);
            }
        };
        return eleTest;
    }

    public static SimulationScenario ascTest(final int servers) {
        SimulationScenario testAsc = new SimulationScenario() {
            {
                SimulationScenario.StochasticProcess startNodes = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(0));
                        raise(servers, startServerOp, new BasicIntSequentialDistribution(1));
                    }
                };

                SimulationScenario.StochasticProcess startPutClient = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(0));
                        raise(1, putOp, new BasicIntSequentialDistribution(1), new BasicIntSequentialDistribution(2));
                    }
                };

                SimulationScenario.StochasticProcess startCasClient = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(0));
                        raise(1, casOp, new BasicIntSequentialDistribution(1), new BasicIntSequentialDistribution(2));
                    }
                };

                SimulationScenario.StochasticProcess startGetClient = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(0));
                        raise(1, getOp, new BasicIntSequentialDistribution(1), new BasicIntSequentialDistribution(2));
                    }
                };

                SimulationScenario.StochasticProcess killNode = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(0));
                        raise(1, killNodeOp, new ConstantDistribution<>(Integer.class, 45683));
                    }
                };

                SimulationScenario.StochasticProcess killNode2 = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(0));
                        raise(1, killNodeOp, new ConstantDistribution<>(Integer.class, 45686));
                    }
                };


                SimulationScenario.StochasticProcess putOpe = new SimulationScenario.StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(0));
                        raise(1, putOp, new BasicIntSequentialDistribution(1), new BasicIntSequentialDistribution(2));
                    }
                };

                startNodes.start();
                startPutClient.startAfterTerminationOf(10000, startNodes);
                startCasClient.startAfterTerminationOf(10000, startPutClient);
                startGetClient.startAfterTerminationOf(10000, startPutClient);
                killNode.startAfterTerminationOf(10000, startGetClient);
                killNode2.startAfterTerminationOf(10000, killNode);
                putOpe.startAfterTerminationOf(10000, killNode2);
                terminateAfterTerminationOf(10000, putOpe);

            }
        };
        return testAsc;
    }

}
