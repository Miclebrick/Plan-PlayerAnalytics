/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.utilities.html.graphs.line;

import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.data.store.mutators.TPSMutator;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static org.junit.Assert.assertEquals;

/**
 * Test class for {@link LineGraph}.
 *
 * @author Rsl1122
 */
public class LineGraphTest {

    private final List<TPS> tpsList = new ArrayList<>();

    @Before
    public void setUp() {
        for (int i = 0; i < 10; i++) {
            tpsList.add(new TPS(i, i, i, i, i, i, i, i));
        }
    }

    @Test
    public void testLineGraphsForBracketErrors() {
        TPSMutator mutator = new TPSMutator(tpsList);
        LineGraph[] graphs = new LineGraph[]{
                new CPUGraph(mutator, true),
                new PlayersOnlineGraph(mutator, false),
                new RamGraph(mutator, true),
                new TPSGraph(mutator, false),
                new EntityGraph(mutator, true),
                new ChunkGraph(mutator, false),
                new DiskGraph(mutator, false)
        };

        for (LineGraph graph : graphs) {
            System.out.print("Bracket Test: " + graph.getClass().getSimpleName() + " | ");
            String series = graph.toHighChartsSeries();

            System.out.println(series);

            char[] chars = series.toCharArray();
            assertBracketMatch(chars);
        }
    }

    private void assertBracketMatch(char[] chars) {
        Stack<Character> bracketStack = new Stack<>();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch (c) {
                case '{':
                case '[':
                case '(':
                    bracketStack.push(c);
                    break;
                case ')':
                    Character pop = bracketStack.pop();
                    assertEquals("Bracket mismatch at char: " + i + " Expected (, got " + pop, '(', (char) pop);
                    break;
                case ']':
                    Character pop1 = bracketStack.pop();
                    assertEquals("Bracket mismatch at char: " + i + " Expected [, got " + pop1, '[', (char) pop1);
                    break;
                case '}':
                    Character pop2 = bracketStack.pop();
                    assertEquals("Bracket mismatch at char: " + i + " Expected {, got " + pop2, '{', (char) pop2);
                    break;
                default:
                    break;
            }
        }
    }
}