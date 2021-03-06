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
package org.eclipse.riena.internal.ui.ridgets.swt;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Label;

import org.eclipse.riena.ui.ridgets.swt.AbstractLabelRidget;

/**
 * Ridget for an SWT {@link Label} widget.
 */
public class LabelRidget extends AbstractLabelRidget {

	public LabelRidget() {
		this(null);
	}

	public LabelRidget(final Label label) {
		setUIControl(label);
	}

	@Override
	protected void checkUIControl(final Object uiControl) {
		checkType(uiControl, Label.class);
	}

	@Override
	public Label getUIControl() {
		return (Label) super.getUIControl();
	}

	@Override
	protected String getUIControlText() {
		return getUIControl().getText();
	}

	@Override
	protected void setUIControlText(final String text) {
		getUIControl().setText(text);
	}

	@Override
	protected void setUIControlImage(final Image image) {
		getUIControl().setImage(image);
	}

}
