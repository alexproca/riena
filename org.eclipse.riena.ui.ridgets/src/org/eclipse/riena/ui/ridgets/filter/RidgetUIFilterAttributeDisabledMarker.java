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
package org.eclipse.riena.ui.ridgets.filter;

import org.eclipse.riena.ui.core.marker.DisabledMarker;

/**
 * Filter attribute to provide a disabled marker for a ridget.
 */
public class RidgetUIFilterAttributeDisabledMarker extends AbstractRidgetUIFilterMarkerAttribute {

	/**
	 * Creates a new instance of {@code RidgetUIFilterAttributeDisabledMarker}.
	 * 
	 * @param id
	 *            - ID
	 * @param marker
	 *            - marker
	 */
	public RidgetUIFilterAttributeDisabledMarker(String id) {
		super(id, new DisabledMarker(false));
	}

}