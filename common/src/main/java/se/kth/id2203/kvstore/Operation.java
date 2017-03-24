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

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.UUID;

import se.kth.id2203.networking.NetAddress;
import se.sics.kompics.KompicsEvent;

/**
 *
 * @author Lars Kroll <lkroll@kth.se>
 */
public class Operation implements KompicsEvent, Serializable {

    private static final long serialVersionUID = 2525600659083087179L;

    public final String key;
    public final UUID id;
    public final NetAddress source;

    public Operation(NetAddress source, String key) {
        this.source = source;
        this.key = key;
        this.id = UUID.randomUUID();
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("key", key)
                .toString();
    }

    /** Override equals to compare th UUID of an operation.
     * Needed for abortable sequence consensus in order to
     * detect duplicate proposals.
     */
    @Override
    public boolean equals(Object ob) {
        Operation op = (Operation) ob;
        if(this.id.equals(op.id)) {
            return true;
        }
        return false;
    }
}