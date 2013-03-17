package com.cordys.jenkinsci.estimation;

import hudson.matrix.AxisList;
import hudson.matrix.Combination;
import hudson.matrix.MatrixConfiguration;
import org.apache.commons.collections.Transformer;
import org.junit.Assert;
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
public class EstimatedDurationMatrixConfigurationSorterTest {

    public static final String ESTIMATED_DURATION = "estimatedDuration";
    private final ParamsBuilder builder;

    public EstimatedDurationMatrixConfigurationSorterTest(ParamsBuilder builder) {
        this.builder = builder;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        Object[][] data = new Object[][]{
                {verifyWithValues(3, 4, 6, 1, 2, 5).expectInAscendingOrder()},
                {verifyWithValues(3, 4, 6, 1, 2, 5).expectInDescendingOrder()},
                {verifyWithValues(3, 4, 6, 1, 2, 5, 1).expectInAscendingOrder()},
                {verifyWithValues(1, 1, 1, 1, 1).expectInAscendingOrder()},


        };
        return Arrays.asList(data);
    }

    private static ParamsBuilder verifyWithValues(int... values) {
        return new ParamsBuilder().withDurations(values);
    }

    @Test
    public void testCompareConfigurations() throws Exception {
        Assert.assertTrue(builder.ascendingOrder != null);

        SortedSet<MatrixConfiguration> matrixConfigurations = new TreeSet(new EstimatedDurationMatrixConfigurationSorter(!builder.ascendingOrder));
        matrixConfigurations.addAll(builder.getConfigurations());

        assertThat(builder.getConfigurations(), hasSize(matrixConfigurations.size()));

        final Collection<String> asString = collect(matrixConfigurations, stringValueTransformer());

        final String[] expectedConfigurationsString = getExpectedConfigurations();

        assertThat(asString, hasItems(expectedConfigurationsString)); // has all items...
        assertThat(asString, contains(expectedConfigurationsString)); // ...in the right order
    }

    private String[] getExpectedConfigurations() {
        List<Integer> expected = new ArrayList(builder.axes);
        Collections.sort(expected);
        if (!builder.ascendingOrder) {
            Collections.reverse(expected);
        }

        final Object[] expectedConfigurations = collect(expected, new UniqueDurationTransformer()).toArray();

        final String[] expectedConfigurationsString = new String[expectedConfigurations.length];
        int i = 0;
        for (Object o : expectedConfigurations) {
            expectedConfigurationsString[i++] = ESTIMATED_DURATION +
                    "=" + o;
        }
        return expectedConfigurationsString;
    }

    static class ParamsBuilder {
        private LinkedList<Integer> axes = new LinkedList<Integer>();
        private Boolean ascendingOrder = null;

        private ParamsBuilder withDurations(int... durations) {
            for (int i : durations)
                axes.add(i);

            return this;
        }

        private ParamsBuilder expectInAscendingOrder() {
            ascendingOrder = true;

            return this;
        }

        private ParamsBuilder expectInDescendingOrder() {
            ascendingOrder = false;

            return this;
        }

        public Collection<? extends MatrixConfiguration> getConfigurations() {
            final AxisList axisList = new AxisList();

            axisList.add(new hudson.matrix.Axis(ESTIMATED_DURATION, new ArrayList<String>(collect(axes, new UniqueDurationTransformer()))));


            Collection<MatrixConfiguration> items = new ArrayList<MatrixConfiguration>();

            for (Combination combination : axisList.list()) {
                MatrixConfiguration configuration = mock(MatrixConfiguration.class);
                final String estimatedDuration = combination.get(ESTIMATED_DURATION);
                when(configuration.getEstimatedDuration()).thenReturn(Long.parseLong(estimatedDuration.substring(0, estimatedDuration.length() - 1)));
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

        @Override
        public String toString() {
            return "Test with values " + axes + " in " + (ascendingOrder ? "as" : "des") + "cending order";
        }
    }

    private static class UniqueDurationTransformer implements Transformer {
        private final Map<Integer, Character> dict = new HashMap<Integer, Character>();

        public String transform(Object input) {
            Integer i = (Integer) input;
            if (dict.containsKey(i)) {
                Character c = dict.get(i);
                dict.put(i, ++c);
                return i + "" + c;
            }

            dict.put(i, 'A');
            return i + "A";
        }
    }
}
