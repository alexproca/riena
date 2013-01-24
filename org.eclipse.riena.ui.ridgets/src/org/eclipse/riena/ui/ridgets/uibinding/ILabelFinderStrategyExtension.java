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
package org.eclipse.riena.ui.ridgets.uibinding;

import org.eclipse.riena.core.injector.extension.ExtensionInterface;
import org.eclipse.riena.core.injector.extension.MapName;

/**
 * Extension interface for the {@link ILabelFinderStrategy}
 * 
 * @since 1.2
 */
@ExtensionInterface(id = "labelFinderStrategy")
public interface ILabelFinderStrategyExtension {

	/**
	 * Create a {@code ILabelFinderStrategy}
	 * 
	 * @return a {@code ILabelFinderStrategy}
	 */
	@MapName("className")
	ILabelFinderStrategy createFinderStrategy();
}
