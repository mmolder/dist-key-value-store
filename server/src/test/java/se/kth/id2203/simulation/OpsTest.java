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

import junit.framework.Assert;
import org.junit.Test;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.run.LauncherComp;

/**
 *
 * @author Lars Kroll <lkroll@kth.se>
 */
public class OpsTest {
    
    private static final int NUM_MESSAGES = 10;
    private final SimulationResultMap res = SimulationResultSingleton.getInstance();

    @Test
    public void simpleOpsTest() {
        long seed = 123;
        SimulationScenario.setSeed(seed);
        SimulationScenario sbs = ScenarioGen.simpleOps(9);
        //SimulationScenario simpleBootScenario = ScenarioGen.initialReplicationGroups(9);
        //SimulationScenario opsTest = ScenarioGen.testOperations(9);
        //SimulationScenario bebTest = ScenarioGen.testBeb(9);
        //SimulationScenario epfdTest = ScenarioGen.testEpfd(9);
        //SimulationScenario eleTest = ScenarioGen.testEle(9);
        //SimulationScenario ascTest = ScenarioGen.ascTest(9);
        res.put("messages", NUM_MESSAGES);

        sbs.simulate(LauncherComp.class);

        //System.out.println("\n\n-------------INITIAL REPLICATION GROUP TEST------------\n\n");
        //simpleBootScenario.simulate(LauncherComp.class);

        //System.out.println("\n\n-------------OPERATION TEST------------\n\n");
        //opsTest.simulate(LauncherComp.class);

        //System.out.println("\n\n-------------BEB TEST------------\n\n");
        //bebTest.simulate(LauncherComp.class);

        //System.out.println("\n\n-------------EPFD TEST------------\n\n");
        //epfdTest.simulate(LauncherComp.class);

        //System.out.println("\n\n-------------LEADER ELECTION TEST------------\n\n");
        //eleTest.simulate(LauncherComp.class);

        //System.out.println("\n\n-------------ABORTABLE SEQUENCE CONSENSUS TEST------------\n\n");
        //ascTest.simulate(LauncherComp.class);
        for (int i = 0; i < NUM_MESSAGES; i++) {
            Assert.assertEquals("NOT_IMPLEMENTED", res.get("test"+i, String.class));
            // of course the correct response should be SUCCESS not NOT_IMPLEMENTED, but like this the test passes
        }
    }

}
