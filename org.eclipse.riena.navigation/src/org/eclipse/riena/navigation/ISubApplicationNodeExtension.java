/*******************************************************************************
 * Copyright (c) 2007, 2008 compeople AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    compeople AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.riena.navigation;

import org.eclipse.riena.core.extension.ExtensionInterface;
import org.eclipse.riena.core.extension.MapName;

/**
 * Interface for a SubApplicationNode extension that defines how to create a
 * node or a subtree in the application model tree.
 */
@ExtensionInterface
public interface ISubApplicationNodeExtension extends INodeExtension {

	/**
	 * @return This subapplications label
	 */
	String getLabel();

	/**
	 * @return This subapplications icon id
	 */
	String getIcon();

	/**
	 * @return For the SWT-based Riena UI this is the ID of the perspective
	 *         associated with the subapplication. Must match an perspective
	 *         elements id attribute of an "org.eclipse.ui.perspectives"
	 *         extension.
	 */
	Object getView();

	/**
	 * @return For the SWT-based Riena UI this is the ID of the perspective
	 *         associated with the subapplication. Must match an perspective
	 *         elements id attribute of an "org.eclipse.ui.perspectives"
	 *         extension.
	 */
	@MapName("view")
	String getViewId();

	/**
	 * @return A list of submodule node definitions that are children of the
	 *         receiver
	 */
	@MapName("modulegroup")
	IModuleGroupNodeExtension[] getModuleGroupNodes();

	@MapName("modulegroup")
	IModuleGroupNodeExtension[] getChildNodes();
}
