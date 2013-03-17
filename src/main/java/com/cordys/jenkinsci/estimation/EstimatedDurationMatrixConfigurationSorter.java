package com.cordys.jenkinsci.estimation;

import hudson.Extension;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixConfigurationSorterDescriptor;
import hudson.matrix.MatrixConfigurationSorter;
import hudson.matrix.MatrixProject;
import hudson.util.FormValidation;

import org.kohsuke.stapler.DataBoundConstructor;

public class EstimatedDurationMatrixConfigurationSorter extends MatrixConfigurationSorter
{

	private final boolean reversed;

	public boolean isReversed() {
		return reversed;
	}

	@DataBoundConstructor
	public EstimatedDurationMatrixConfigurationSorter(final boolean reversed) {
		this.reversed = reversed;
	}

	public int compare(final MatrixConfiguration o1, final MatrixConfiguration o2) {
		final int signum = Long.signum(o1.getEstimatedDuration() - o2.getEstimatedDuration()) * (reversed ? -1 : 1);
		if (signum == 0) {
			return o1.getCombination().compareTo(o2.getCombination());
		}
		return signum;
	}

	@Override
	public void validate(final MatrixProject arg0) throws FormValidation {
	}

	@Extension
	public static class DescriptorImpl extends MatrixConfigurationSorterDescriptor
	{
		@Override
		public String getDisplayName() {
			return "Estimated duration";
		}
	}
}
