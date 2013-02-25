package com.cordys.jenkinsci.headstails;

import hudson.matrix.AxisList;
import hudson.matrix.Combination;
import hudson.matrix.MatrixConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.*;

import static org.apache.commons.collections.CollectionUtils.collect;
import static org.apache.commons.collections.TransformerUtils.stringValueTransformer;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class HeadsAndTailsMatrixConfigurationSorterTest {


    public static final String A = "a";
    public static final String B = "b";
    public static final String C = "c";
    public static final String D = "d";
    private ParamsBuilder builder;

    public HeadsAndTailsMatrixConfigurationSorterTest(ParamsBuilder builder) {
        this.builder = builder;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        Object[][] data = new Object[][]{
                {verifyWithValues(A, B, C).expectInOrder(A, B, C)},
                {verifyWithValues(B, A, C).expectInOrder(A, B, C)},
                {verifyWithValues(A, B, C).withHeads(A).withTails(C).expectInOrder(A, B, C)},
                {verifyWithValues(A, B, C, D).withHeads(A).withTails(C).expectInOrder(A, B, D, C)},
                {verifyWithValues(A, B, C, D).withTails(A).expectInOrder(B, C, D, A)},
                {verifyWithValues(A, B, C).withHeads(C, A).withTails(A, B).expectInOrder(C, A, B)},
                {verifyWithValues(A, B, C).withHeads(C).expectInOrder(C, A, B)},
                {verifyWithValues(A, B, C).andAxis("2nd").withValues(A, B, C).withHeads(B).expectInOrder(B, A,/**/B, B,/**/B, C,/**/A, A,/**/A, B,/**/A, C,/**/C, A,/**/C, B,/**/C, C)},
                {verifyWithValues(A, B, C).andAxis("0nd").withValues(A, B, C).withHeads(B).expectInOrder(B, A,/**/B, B,/**/B, C,/**/A, A,/**/A, B,/**/A, C,/**/C, A,/**/C, B,/**/C, C)},
                {verifyWithValues(A, B, C).andAxis("2nd").withValues(A, B, C).withHeads(B).withTails(A).expectInOrder(B, A,/**/B, B,/**/B, C,/**/C, A,/**/C, B,/**/C, C,/**/A, A,/**/A, B,/**/A, C)},
                {verifyWithValues(A, B, C).andAxis("2nd").withValues(A, B, C).andAxis("3rd").withValues(A).withHeads(B).withTails(A).expectInOrder(B, A, A,/**/B, B, A,/**/B, C, A,/**/C, A, A,/**/C, B, A,/**/C, C, A,/**/A, A, A,/**/A, B, A,/**/A, C, A)},

        };
        return Arrays.asList(data);
    }

    private static ParamsBuilder verify(String axis) {
        return new ParamsBuilder(axis);
    }

    private static ParamsBuilder verify() {
        return verify("1st");
    }

    private static ParamsBuilder verifyWithValues(String... values) {
        return verify().withValues(values);
    }

    @Test
    public void validate() throws Exception {
        SortedSet<MatrixConfiguration> matrixConfigurations = new TreeSet(getMatrixConfigurationSorter());
        matrixConfigurations.addAll(builder.getConfigurations());

        assertThat(builder.getConfigurations(), hasSize(builder.getExpectedSize()));

        final Collection<String> asString = collect(matrixConfigurations, stringValueTransformer());

        assertThat(asString, hasItems(builder.order)); // has all items...
        assertThat(asString, contains(builder.order)); // ...in the right order
    }

    private HeadsAndTailsMatrixConfigurationSorter getMatrixConfigurationSorter() {
        return new HeadsAndTailsMatrixConfigurationSorter(builder.axes.getFirst().name, builder.heads, builder.tails);
    }

    static class ParamsBuilder {
        private LinkedList<ParamsBuilder.Axis> axes = new LinkedList<ParamsBuilder.Axis>();
        private String heads;
        private String tails;
        private String[] order;

        ParamsBuilder(String axis) {
            andAxis(axis);
        }

        ParamsBuilder andAxis(String axis) {
            this.axes.add(new Axis(axis));
            return this;
        }

        ParamsBuilder withValues(String... values) {
            this.axes.getLast().values = values;
            return this;
        }

        ParamsBuilder withHeads(String heads, String... additional) {
            this.heads = heads;
            for (String s : additional) {
                this.heads += " " + s;
            }
            return this;
        }

        ParamsBuilder withTails(String tails, String... additional) {
            this.tails = tails;
            for (String s : additional) {
                this.tails += " " + s;
            }
            return this;
        }

        ParamsBuilder expectInOrder(String... order) {
            this.order = new String[order.length / this.axes.size()];
            String[] values = new String[this.axes.size()];
            for (int i = 0; i < order.length; i++) {
                values[i % this.axes.size()] = order[i];
                if ((i + 1) % this.axes.size() == 0) {
                    Map<String, String> kvp = new TreeMap<String, String>();
                    for (int j = 0; j < this.axes.size(); j++) {
                        kvp.put(this.axes.get(j).name, values[j]);
                    }
                    this.order[i / this.axes.size()] = new Combination(kvp).toString();
                }
            }
            return this;
        }

        ParamsBuilder expectInOrderDirectly(String... order) {
            this.order = order;
            return this;
        }

        @Override
        public String toString() {
            String ts = "Given ";
            for (Axis a : axes) {
                ts += "axis '" + a.name + '\'' + " with values " + (a.values == null ? null : Arrays.asList(a.values)) + ',';
            }
            ts += (heads == null ? "" : ", heads '" + heads + '\'');
            ts += (tails == null ? "" : " and tails '" + tails + '\'');
            ts += ", expected order is " + (order == null ? null : Arrays.asList(order));
            return ts.replaceAll(",,", ",");
        }

        int getExpectedSize() {
            int expectedSize = 1;
            for (ParamsBuilder.Axis a : this.axes) {
                expectedSize *= a.values.length;
            }
            return expectedSize;
        }

        Collection<MatrixConfiguration> getConfigurations() {
            final AxisList axisList = new AxisList();
            for (ParamsBuilder.Axis a : this.axes) {
                axisList.add(new hudson.matrix.Axis(a.name, a.values));
            }

            Collection<MatrixConfiguration> items = new ArrayList<MatrixConfiguration>();

            for (Combination combination : axisList.list()) {
                MatrixConfiguration configuration = mock(MatrixConfiguration.class);
                when(configuration.getCombination()).thenReturn(combination);
                when(configuration.toString()).then(new Answer<String>() {
                    public String answer(InvocationOnMock invocationOnMock) throws Throwable {
                        return ((MatrixConfiguration) invocationOnMock.getMock()).getCombination().toString();
                    }
                });
                items.add(configuration);
            }

            return items;
        }

        private class Axis {
            String name;
            String[] values;

            private Axis(String name, String... values) {
                this.name = name;
                this.values = values;
            }
        }
    }
}
