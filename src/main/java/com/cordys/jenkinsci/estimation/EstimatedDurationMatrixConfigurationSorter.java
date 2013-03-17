package com.cordys.jenkinsci.estimation;

import com.cordys.jenkinsci.ConsistentMatrixConfigurationSorter;
import hudson.Extension;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixConfigurationSorterDescriptor;
import hudson.matrix.MatrixProject;
import hudson.util.FormValidation;
import org.kohsuke.stapler.DataBoundConstructor;

public class EstimatedDurationMatrixConfigurationSorter extends ConsistentMatrixConfigurationSorter {

    private final boolean reversed;

    @DataBoundConstructor
    public EstimatedDurationMatrixConfigurationSorter(final boolean reversed) {
        this.reversed = reversed;
    }

    public boolean isReversed() {
        return reversed;
    }

    @Override
    public int compareConfigurations(final MatrixConfiguration o1, final MatrixConfiguration o2) {
        return Long.signum(o1.getEstimatedDuration() - o2.getEstimatedDuration()) * (reversed ? -1 : 1);
    }

    @Override
    public void validate(final MatrixProject arg0) throws FormValidation {
    }

    @Extension
    public static class DescriptorImpl extends MatrixConfigurationSorterDescriptor {
        @Override
        public String getDisplayName() {
            return "Estimated duration";
        }
    }
}
