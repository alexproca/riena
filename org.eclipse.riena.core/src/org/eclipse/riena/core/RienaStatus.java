/*******************************************************************************
 * Copyright (c) 2007, 2013 compeople AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    compeople AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.riena.core;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.framework.internal.core.AbstractBundle;

import org.eclipse.riena.core.util.VariableManagerUtil;
import org.eclipse.riena.internal.core.Activator;

/**
 * Utility to check the Riena status.
 */
public final class RienaStatus {

	/**
	 * This system property controls {@code RienaStatus.isDevelopment()}
	 */
	public static final String RIENA_DEVELOPMENT_SYSTEM_PROPERTY = "riena.development"; //$NON-NLS-1$

	/**
	 * This system property controls {@code RienaStatus.isTest()}
	 */
	public static final String RIENA_TEST_SYSTEM_PROPERTY = "riena.test"; //$NON-NLS-1$

	/**
	 * This is the default value (i.e. if the value is not explicitly defined)
	 * for the system property {@code RIENA_DEVELOPMENT_SYSTEM_PROPERTY}
	 */
	public static final String DEVELOPMENT_DEFAULT = inOsgiDevMode();

	/**
	 * This is the default value (i.e. if the value is not explicitly defined)
	 * for the system property {@code RIENA_TEST_SYSTEM_PROPERTY}
	 */
	public static final String TEST_DEFAULT = "false"; //$NON-NLS-1$

	/**
	 * Value of {@code getStage()} if no stage has been defined.
	 * 
	 * @since 3.0
	 */
	public static final String UNKNOWN_STAGE = "<unknown>"; //$NON-NLS-1$

	private RienaStatus() {
		// Utility
	}

	/**
	 * Riena core bundle status.
	 * 
	 * @return {@code true} if the riena.core bundle has been started; otherwise
	 *         {@code false}
	 */
	public static boolean isActive() {
		if (Activator.getDefault() == null) {
			return false;
		}
		return Activator.getDefault().isActive();
	}

	/**
	 * Are all riena startup actions executed?
	 * 
	 * @return {@code true} if all startup actions have been executed; otherwise
	 *         {@code false}
	 * 
	 * @since 3.0
	 */
	public static boolean areStartupActionsExecuted() {
		return Activator.getDefault().areStartupActionsExecuted();
	}

	/**
	 * Are we in <i>development</i>?<br>
	 * If {@code true} certain services/functionalities behave more appropriate
	 * for development time, e.g.
	 * <ul>
	 * <li>default logging is enabled</li>
	 * <li>the store of the client monitoring is cleaned up on each start
	 * <li>..</li>
	 * </ul>
	 * <b>Note:</b> This property is controlled by the system property
	 * "riena.development". If <b>NOT</b> defined it defaults to
	 * {@code System.getProperty("osgi.dev") != null}.<br>
	 * This means that the default value is {@code true} while you are within
	 * the Eclipse IDE otherwise it is {@code false}.
	 * 
	 * @return is development or not?
	 */
	public static boolean isDevelopment() {
		return Boolean.parseBoolean(System.getProperty(RIENA_DEVELOPMENT_SYSTEM_PROPERTY, DEVELOPMENT_DEFAULT));
	}

	private static String inOsgiDevMode() {
		return Boolean.toString(System.getProperty("osgi.dev") != null); //$NON-NLS-1$
	}

	/**
	 * Are we running a <i>test case</i>?<br>
	 * If {@code true} certain services/functionalities behave more appropriate
	 * for testing, e.g. ridgets are created on the fly
	 * 
	 * @return whether we are running a test
	 * 
	 * @since 2.0
	 */
	public static boolean isTest() {
		return Boolean.parseBoolean(System.getProperty(RIENA_TEST_SYSTEM_PROPERTY, TEST_DEFAULT));
	}

	/**
	 * Return the stage riena is currently running in.
	 * 
	 * @return the stage or if not set the string {@code "<unknown>"}
	 * @since 3.0
	 */
	public static String getStage() {
		try {
			return VariableManagerUtil.substitute("${riena.stage}"); //$NON-NLS-1$
		} catch (final CoreException e) {
			return UNKNOWN_STAGE;
		}
	}

	/**
	 * Create a "nice" report with a list of all unresolved bundles and their
	 * possible cause for not becoming resolved.
	 * 
	 * @return the report or {@code null} if all bundles were resolved
	 * 
	 * @since 4.0
	 */
	@SuppressWarnings("restriction")
	public static String reportUnresolvedBundles() {
		final Bundle coreBundle = FrameworkUtil.getBundle(RienaStatus.class);
		if (coreBundle == null) {
			return "Could not resolve Riena core bundle."; //$NON-NLS-1$
		}
		final BundleContext bundleContext = coreBundle.getBundleContext();
		if (bundleContext == null) {
			return "Could not get Riena bundle context."; //$NON-NLS-1$
		}
		final StringBuilder bob = new StringBuilder();
		for (final Bundle bundle : bundleContext.getBundles()) {
			if (bundle.getState() < Bundle.RESOLVED) {
				if (bob.length() == 0) {
					bob.append("Unresolved bundles:").append("\n"); //$NON-NLS-1$//$NON-NLS-2$
				}
				bob.append(" - ").append(bundle.getSymbolicName()).append(" is not resolved"); //$NON-NLS-1$ //$NON-NLS-2$
				if (bundle instanceof AbstractBundle) {
					final BundleException resolutionFailureException = ((AbstractBundle) bundle)
							.getResolutionFailureException();
					if (resolutionFailureException != null) {
						bob.append(" because: ").append(resolutionFailureException.getMessage()); //$NON-NLS-1$
					}
				}
				bob.append("\n"); //$NON-NLS-1$
			}
		}
		return bob.length() == 0 ? null : bob.toString();
	}

}
