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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.TreeMultimap;
import java.util.Collection;
import java.util.Set;

import se.kth.id2203.bootstrapping.NodeAssignment;
import se.kth.id2203.networking.NetAddress;

/**
 *
 * @author Lars Kroll <lkroll@kth.se>
 */
public class LookupTable implements NodeAssignment {

    private static final long serialVersionUID = -8766981433378303267L;

    public final TreeMultimap<Integer, NetAddress> partitions = TreeMultimap.create();

    public static int replicationDegree = 3;
    public static int keySpace = 99;        //hardcoded value, means that keys can only be between 0 and 99

    /** Find which partition is responsible for a given key, returns a collection of NetAddresses */
    public Collection<NetAddress> lookup(String key) {
        Integer partition = partitions.keySet().floor(Integer.parseInt(key));
        if (partition == null) {
            partition = partitions.keySet().last();
        }
        return partitions.get(partition);
    }

    public Collection<NetAddress> getNodes() {
        return partitions.values();
    }

    public Set<Integer> getKeys() {
        return partitions.keySet();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LookupTable(\n");
        for (Integer key : partitions.keySet()) {
            sb.append(key);
            sb.append(" -> ");
            sb.append(Iterables.toString(partitions.get(key)));
            sb.append("\n");
        }
        sb.append(")");
        return sb.toString();
    }

    /** Generates the initial node assignments, divides nodes into replication groups */
    static LookupTable generate(ImmutableSet<NetAddress> nodes) {
        LookupTable lut = new LookupTable();
        int numNodes = nodes.size();                                    //number of nodes
        int numGroups = Math.floorDiv(numNodes, replicationDegree);     //number of groups
        int range = keySpace/numGroups;                                 //key range of each group
        int j = 0;
        //divide nodes into groups
        for(int i = 0; i < numNodes; i++) {
            if(j == keySpace) {
                j = 0;
            }
            lut.partitions.put(j, nodes.asList().get(i));
            j += range;
        }
        return lut;
    }

}
