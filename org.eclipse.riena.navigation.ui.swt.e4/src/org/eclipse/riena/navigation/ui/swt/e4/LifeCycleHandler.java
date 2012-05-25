/*******************************************************************************
 * Copyright (c) 2007, 2011 compeople AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    compeople AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.riena.navigation.ui.swt.e4;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;

/**
 *
 */
public class LifeCycleHandler {

	/**
	 * 
	 */
	public LifeCycleHandler() {
		System.out.println("LifeCycleHandler.LifeCycleHandler()");
	}

	@PostContextCreate
	public void startup(final IEclipseContext context) {
		System.out.println("LifeCycleHandler.startup()");
	}

}
