package com.cordys.jenkinsci.headstails;

import com.cordys.jenkinsci.ConsistentMatrixConfigurationSorter;
import hudson.Extension;
import hudson.matrix.Axis;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixConfigurationSorterDescriptor;
import hudson.matrix.MatrixProject;
import hudson.util.FormValidation;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Comparator;

public class HeadsAndTailsMatrixConfigurationSorter extends ConsistentMatrixConfigurationSorter {
    private final Axis axis;
    private final String heads;
    private final String tails;
    private volatile Comparator<String> nameComparator;

    @DataBoundConstructor
    public HeadsAndTailsMatrixConfigurationSorter(String axis, String heads, String tails) {
        this.axis = new Axis(axis, "anyValue");
        this.heads = heads;
        this.tails = tails;
    }

    private static <T> T[] reverse(T[] array) {
        int size = array.length;

        for (int i = 0; i < size / 2; i++) {
            T temp = array[i];
            array[i] = array[size - i - 1];
            array[size - i - 1] = temp;
        }
        return array;
    }

    public String getAxis() {
        return axis.getName();
    }

    public String getHeads() {
        return heads;
    }

    public String getTails() {
        return tails;
    }

    /**
     * Checks if this sorter is properly configured and applicable for the given project.
     * <p/>
     * <p/>
     * This method is invoked when the configuration is submitted to ensure that the sorter is compatible
     * with the current project configuration (most probably with its {@link hudson.matrix.Axis}.)
     *
     * @param p Project for which this sorter is being used for.
     * @throws hudson.util.FormValidation If you need to report an error to the user and reject the form submission.
     */
    @Override
    public void validate(MatrixProject p) throws FormValidation {
        if (!p.getAxes().contains(axis))
            FormValidation.error("Axis '" + axis + "' undefined in project.");
    }

    @Override
    public int compareConfigurations(MatrixConfiguration matrixConfigurationA, MatrixConfiguration matrixConfigurationB) {
        final String valueA = matrixConfigurationA.getCombination().get(axis);
        final String valueB = matrixConfigurationB.getCombination().get(axis);

        return getNameComparator().compare(valueA, valueB);
    }

    Comparator<String> getNameComparator() {
        if (nameComparator == null)
            nameComparator = new HeadsAndTailsAxisNameComparator();
        return nameComparator;
    }

    @Extension
    public static class DescriptorImpl extends MatrixConfigurationSorterDescriptor {
        @Override
        public String getDisplayName() {
            return "Heads and tails";
        }
    }

    public class HeadsAndTailsAxisNameComparator implements Comparator<String> {

        public int compare(String valueA, String valueB) {
            String[] first = getHeads();
            String[] last = getTails();

            int indexA = 0, indexB = 0;

            for (int i = 1; i < last.length + 1; ++i) {
                if (valueA.equals(last[i - 1])) {
                    indexA = i;
                }
                if (valueB.equals(last[i - 1])) {
                    indexB = i;
                }
            }
            for (int i = first.length; i > 0; --i) {
                if (valueA.equals(first[i - 1])) {
                    indexA = -i;
                }
                if (valueB.equals(first[i - 1])) {
                    indexB = -i;
                }
            }


            final int difference = indexA - indexB;

            // Keep behavior consistent
            if (difference == 0) {
                return valueA.compareTo(valueB);
            }

            return difference;
        }

        private String[] getHeads() {
            return reverse(getStrings(heads));
        }

        private String[] getTails() {
            return getStrings(tails);
        }

        private String[] getStrings(String s) {
            if (s == null)
                return new String[0];

            return s.split(" ");
        }
    }
}
