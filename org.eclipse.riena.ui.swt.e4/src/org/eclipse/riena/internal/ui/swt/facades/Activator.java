/*******************************************************************************
 * Copyright (c) 2007, 2014 compeople AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    compeople AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.riena.internal.ui.swt.facades;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static Activator plugin;
	private BundleContext context;

	public void start(final BundleContext context) throws Exception {
		this.context = context;
		plugin = this;
	}

	public void stop(final BundleContext context) throws Exception {
		plugin = null;
	}

	/**
	 * @return the context
	 */
	public BundleContext getContext() {
		return context;
	}

	public static Activator getDefault() {
		return plugin;
	}

}
