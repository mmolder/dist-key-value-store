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
package se.kth.id2203.kvstore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.id2203.kvstore.OpResponse.Code;
import se.kth.id2203.networking.Message;
import se.kth.id2203.networking.NetAddress;
import se.kth.id2203.overlay.Routing;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;

import java.util.HashMap;

/**
 *
 * @author Lars Kroll <lkroll@kth.se>
 */
public class KVService extends ComponentDefinition {

    final static Logger LOG = LoggerFactory.getLogger(KVService.class);
    //******* Ports ******
    protected final Positive<Network> net = requires(Network.class);
    protected final Positive<Routing> route = requires(Routing.class);
    //******* Fields ******
    final NetAddress self = config().getValue("id2203.project.address", NetAddress.class);

    /** Hardcode some default values in the store */
    public HashMap<Integer, String> store = new HashMap<Integer, String>() {
        {
            put(4, "value4");
            put(45, "value45");
            put(80, "value80");
        }
    };

    protected final ClassMatchedHandler<GetOperation, Message> getHandler = new ClassMatchedHandler<GetOperation, Message>() {
        @Override
        public void handle(GetOperation getOperation, Message message) {
            LOG.info(self + ": received a get operation, key: " + getOperation.getKey());
            String value;
            value = store.get(Integer.parseInt(getOperation.getKey()));
            if(value != null) {
                trigger(new Message(self, getOperation.source, new OpResponse(getOperation.id, value, Code.OK)), net);
            } else {
                trigger(new Message(self, getOperation.source, new OpResponse(getOperation.id, "no value for key: " + getOperation.getKey(), Code.NOT_FOUND)), net);
            }
        }
    };

    protected final ClassMatchedHandler<PutOperation, Message> putHandler = new ClassMatchedHandler<PutOperation, Message>() {
        @Override
        public void handle(PutOperation putOperation, Message message) {
            LOG.info("put operation received");
            store.put(Integer.parseInt(putOperation.getKey()), putOperation.getValue());
            trigger(new Message(self, putOperation.source, new OpResponse(putOperation.id, "put operation succeeded, key: " + putOperation.getKey() + " value: " + putOperation.getValue(), Code.OK)), net);
        }
    };

    protected final ClassMatchedHandler<CASOperation, Message> casHandler = new ClassMatchedHandler<CASOperation, Message>() {
        @Override
        public void handle(CASOperation casOperation, Message message) {
            LOG.info("cas operation received");
            String value = store.get(Integer.parseInt(casOperation.getKey()));
            if((value != null) && value.equals(casOperation.getRefVal())) {
                store.put(Integer.parseInt(casOperation.getKey()), casOperation.getNewVal());
                trigger(new Message(self, casOperation.source, new OpResponse(casOperation.id, "cas changed value of key " + casOperation.getKey() + " from " + value + " to " + casOperation.getNewVal(), Code.OK)), net);
            } else {
                trigger(new Message(self, casOperation.source, new OpResponse(casOperation.id, "ref value did not match stored value", Code.NOT_FOUND)), net);
            }
        }
    };

    {
        //subscribe(startHandler, control);
        //subscribe(opHandler, net);
        subscribe(getHandler, net);
        subscribe(putHandler, net);
        subscribe(casHandler, net);
    }

}
