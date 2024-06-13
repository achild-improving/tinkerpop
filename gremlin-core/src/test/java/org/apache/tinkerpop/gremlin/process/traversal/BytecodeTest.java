/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.tinkerpop.gremlin.process.traversal;

import org.apache.tinkerpop.gremlin.process.computer.Computer;
import org.apache.tinkerpop.gremlin.process.computer.traversal.strategy.decoration.VertexProgramStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.decoration.SubgraphStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.verification.ReadOnlyStrategy;
import org.apache.tinkerpop.gremlin.structure.Column;
import org.apache.tinkerpop.gremlin.structure.util.empty.EmptyGraph;
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class BytecodeTest {

    @Test
    public void shouldHaveProperHashAndEquality() {
        final GraphTraversalSource g = EmptyGraph.instance().traversal();
        final Traversal.Admin traversal1 = g.V().out().repeat(__.out().in()).times(2).groupCount().by(__.outE().count()).select(Column.keys).order().by(Order.desc).asAdmin();
        final Traversal.Admin traversal2 = g.V().out().repeat(__.out().in()).times(2).groupCount().by(__.outE().count()).select(Column.keys).order().by(Order.desc).asAdmin();
        final Traversal.Admin traversal3 = g.V().out().repeat(__.out().in()).times(2).groupCount().by(__.outE().count()).select(Column.values).order().by(Order.desc).asAdmin();

        assertEquals(traversal1, traversal2);
        assertNotEquals(traversal1, traversal3);
        assertNotEquals(traversal2, traversal3);
        //
        assertEquals(traversal1.hashCode(), traversal2.hashCode());
        assertNotEquals(traversal1.hashCode(), traversal3.hashCode());
        assertNotEquals(traversal2.hashCode(), traversal3.hashCode());
        //
        assertEquals(traversal1.getGremlinLang(), traversal2.getGremlinLang());
        assertNotEquals(traversal1.getGremlinLang(), traversal3.getGremlinLang());
        assertNotEquals(traversal2.getGremlinLang(), traversal3.getGremlinLang());
        //
        assertEquals(traversal1.getGremlinLang().hashCode(), traversal2.getGremlinLang().hashCode());
        assertNotEquals(traversal1.getGremlinLang().hashCode(), traversal3.getGremlinLang().hashCode());
        assertNotEquals(traversal2.getGremlinLang().hashCode(), traversal3.getGremlinLang().hashCode());

    }

    @Test
    public void shouldCloneCorrectly() {
        final GraphTraversalSource g = EmptyGraph.instance().traversal();
        final GremlinLang bytecode = g.V().out().asAdmin().getGremlinLang();
        final GremlinLang bytecodeClone = bytecode.clone();
        assertEquals(bytecode, bytecodeClone);
        assertEquals(bytecode.hashCode(), bytecodeClone.hashCode());
        bytecodeClone.addStep("in", "created"); // mutate the clone and the original should stay the same
        assertNotEquals(bytecode, bytecodeClone);
        assertNotEquals(bytecode.hashCode(), bytecodeClone.hashCode());
        assertEquals(2, IteratorUtils.count(bytecode.getInstructions()));
        assertEquals(3, IteratorUtils.count(bytecodeClone.getInstructions()));
    }

    @Test
    public void shouldIncludeBindingsInEquality() {
        final Bindings b = Bindings.instance();
        final GraphTraversalSource g = EmptyGraph.instance().traversal();

        final GremlinLang bytecode1 = g.V().out(b.of("a", "created")).asAdmin().getGremlinLang();
        final GremlinLang bytecode2 = g.V().out(b.of("a", "knows")).asAdmin().getGremlinLang();
        final GremlinLang bytecode3 = g.V().out(b.of("b", "knows")).asAdmin().getGremlinLang();
        final GremlinLang bytecode4 = g.V().out(b.of("b", "knows")).asAdmin().getGremlinLang();

        assertNotEquals(bytecode1, bytecode2);
        assertNotEquals(bytecode1, bytecode3);
        assertNotEquals(bytecode2, bytecode3);
        assertNotEquals(bytecode2, bytecode4);
        assertNotEquals(bytecode1, bytecode4);
        assertEquals(bytecode3, bytecode4);

        assertEquals(1, bytecode1.getBindings().size());
        assertEquals("created", bytecode1.getBindings().get("a"));
    }

    @Test
    public void shouldIncludeBindingsInNestedTraversals() {
        // adding the choose() step as part of TINKERPOP-2458
        final Bindings b = Bindings.instance();
        final GraphTraversalSource g = EmptyGraph.instance().traversal();
        final GremlinLang bytecode = g.V().in(b.of("a","created")).
                choose(__.out().count()).
                option(b.of("two", 2), __.values("name")).
                option(b.of("three",3), __.values("age")).
                where(__.out(b.of("b","knows")).
                        has("age",b.of("c",P.gt(32))).
                        map(__.values(b.of("d","name")))).
                in(b.of("a", "created")).asAdmin().getGremlinLang();
        assertEquals(6, bytecode.getBindings().size());
        assertEquals("created", bytecode.getBindings().get("a"));
        assertEquals("knows", bytecode.getBindings().get("b"));
        assertEquals(P.gt(32), bytecode.getBindings().get("c"));
        assertEquals("name", bytecode.getBindings().get("d"));
        assertEquals(2, bytecode.getBindings().get("two"));
        assertEquals(3, bytecode.getBindings().get("three"));

        GremlinLang.Binding binding = (GremlinLang.Binding) bytecode.getStepInstructions().get(1).getArguments()[0];
        assertEquals("a", binding.variable());
        assertEquals("created", binding.value());
        binding = (GremlinLang.Binding) bytecode.getStepInstructions().get(3).getArguments()[0];
        assertEquals("two", binding.variable());
        assertEquals(2, binding.value());
        binding = (GremlinLang.Binding) bytecode.getStepInstructions().get(4).getArguments()[0];
        assertEquals("three", binding.variable());
        assertEquals(3, binding.value());
        binding = (GremlinLang.Binding) ((GremlinLang) bytecode.getStepInstructions().get(5).getArguments()[0]).getStepInstructions().get(0).getArguments()[0];
        assertEquals("b", binding.variable());
        assertEquals("knows", binding.value());
        binding = (GremlinLang.Binding) ((GremlinLang) bytecode.getStepInstructions().get(5).getArguments()[0]).getStepInstructions().get(1).getArguments()[1];
        assertEquals("c", binding.variable());
        assertEquals(P.gt(32), binding.value());
        binding = (GremlinLang.Binding) ((GremlinLang) ((GremlinLang) bytecode.getStepInstructions().get(5).getArguments()[0]).getStepInstructions().get(2).getArguments()[0]).getStepInstructions().get(0).getArguments()[0];
        assertEquals("d", binding.variable());
        assertEquals("name", binding.value());
        binding = (GremlinLang.Binding) bytecode.getStepInstructions().get(6).getArguments()[0];
        assertEquals("a", binding.variable());
        assertEquals("created", binding.value());
    }

    @Test
    public void shouldConvertStrategies() {
        final GraphTraversalSource g = EmptyGraph.instance().traversal();
        GremlinLang bytecode = g.withStrategies(ReadOnlyStrategy.instance()).getGremlinLang();
        assertEquals(ReadOnlyStrategy.instance(), bytecode.getSourceInstructions().get(0).getArguments()[0]);
        bytecode = g.withStrategies(SubgraphStrategy.build().edges(__.hasLabel("knows")).create()).getGremlinLang();
        assertEquals(SubgraphStrategy.build().edges(__.hasLabel("knows")).create().getEdgeCriterion().asAdmin().getGremlinLang(),
                ((SubgraphStrategy) bytecode.getSourceInstructions().iterator().next().getArguments()[0]).getEdgeCriterion().asAdmin().getGremlinLang());
    }

    @Test
    public void shouldConvertComputer() {
        final GraphTraversalSource g = EmptyGraph.instance().traversal();
        GremlinLang bytecode = g.withComputer(Computer.compute().workers(10)).getGremlinLang();
        assertEquals(VertexProgramStrategy.build().create(), bytecode.getSourceInstructions().get(0).getArguments()[0]);
        assertEquals(VertexProgramStrategy.build().workers(10).create().getConfiguration().getInt(VertexProgramStrategy.WORKERS),
                ((VertexProgramStrategy) bytecode.getSourceInstructions().iterator().next().getArguments()[0]).getConfiguration().getInt(VertexProgramStrategy.WORKERS));
    }

    @Test
    public void shouldNotHaveHashCollisionsWithBindings() {
        // ideally we should be using guava's EqualsTester to test equals/hashCode but the equals/hashCode contract allows hash collisions.
        // Yet we should avoid hash collisions in case these objects are being used in data structures that rely on hashCode
        final GremlinLang.Binding<String> first = new GremlinLang.Binding<>("3", "7");
        final GremlinLang.Binding<String> second = new GremlinLang.Binding<>("7", "3");
        assertNotEquals(first, second);
        assertNotEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void shouldNotHaveHashCollisions() {
        // ideally we should be using guava's EqualsTester to test equals/hashCode but the equals/hashCode contract allows hash collisions.
        // Yet we should avoid hash collisions in case these objects are being used in data structures that rely on hashCode
        final GremlinLang first = new GremlinLang();
        first.addSource("3", "7");
        first.addStep("7", "3");
        final GremlinLang second = new GremlinLang();
        second.addSource("7", "3");
        second.addStep("3", "7");
        assertNotEquals(first, second);
        assertNotEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void shouldHandleArrays() {
        final GremlinLang b = new GremlinLang();
        b.addStep(GraphTraversal.Symbols.property, "k", new Object[] { new String[] {"A", "B", "C"}});
        assertEquals(1, b.getStepInstructions().size());
        assertEquals(2, b.getStepInstructions().get(0).getArguments().length);
    }

    @Test
    public void shouldAvoidArgumentsNpe() {
        final GremlinLang first = new GremlinLang();
        try {
            first.addSource("3", null);
            first.addSource(TraversalSource.Symbols.withoutStrategies, null);
        } catch (Exception e) {
            e.getCause().printStackTrace(System.err);
        }
    }
}
