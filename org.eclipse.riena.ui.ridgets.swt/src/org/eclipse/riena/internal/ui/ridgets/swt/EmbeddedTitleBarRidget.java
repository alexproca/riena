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
package org.eclipse.riena.internal.ui.ridgets.swt;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.riena.ui.ridgets.ILabelRidget;
import org.eclipse.riena.ui.ridgets.IWindowRidget;
import org.eclipse.riena.ui.ridgets.listener.IWindowRidgetListener;
import org.eclipse.riena.ui.swt.EmbeddedTitleBar;
import org.eclipse.swt.graphics.Image;

/**
 * Ridget for {@link EmbeddedTitleBar}.
 */
public class EmbeddedTitleBarRidget extends AbstractSWTRidget implements IWindowRidget {

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private String text = EMPTY_STRING;
	private String icon;
	private List<IWindowRidgetListener> windowRidgetListeners;

	/**
	 * Creates a new instance of {@code EmbeddedTitleBarRidget}.
	 */
	public EmbeddedTitleBarRidget() {
		windowRidgetListeners = new LinkedList<IWindowRidgetListener>();
	}

	/**
	 * Creates a new instance of {@code EmbeddedTitleBarRidget}.
	 * 
	 * @param window
	 *            - UI Control
	 */
	public EmbeddedTitleBarRidget(EmbeddedTitleBar window) {
		this();
		setUIControl(window);
	}

	/**
	 * @see org.eclipse.riena.ui.ridgets.IRidget#getUIControl()
	 */
	@Override
	public EmbeddedTitleBar getUIControl() {
		return (EmbeddedTitleBar) super.getUIControl();
	}

	/**
	 * @see org.eclipse.riena.ui.ridgets.IWindowRidget#setTitle(java.lang.String)
	 */
	public void setTitle(String title) {
		String oldValue = this.text;
		this.text = title;
		updateTextInControl();
		firePropertyChange(ILabelRidget.PROPERTY_TEXT, oldValue, this.text);
	}

	/**
	 * Returns the title of the title bar.
	 * 
	 * @return text of title
	 */
	public String getTitle() {
		return text;
	}

	/**
	 * @see org.eclipse.riena.ui.ridgets.IWindowRidget#setIcon(java.lang.String)
	 */
	public void setIcon(String icon) {
		String oldIcon = this.icon;
		this.icon = icon;
		if (hasChanged(oldIcon, icon)) {
			updateIconInControl();
		}
	}

	/**
	 * Returns the icon of the title bar.
	 * 
	 * @return icon
	 */
	public String getIcon() {
		return icon;
	}

	/**
	 * @see org.eclipse.riena.internal.ui.ridgets.swt.AbstractSWTRidget#bindUIControl
	 *      ()
	 */
	@Override
	protected void bindUIControl() {
		updateTextInControl();
	}

	/**
	 * @see org.eclipse.riena.internal.ui.ridgets.swt.AbstractSWTRidget#checkUIControl
	 *      (java.lang.Object)
	 */
	@Override
	protected void checkUIControl(Object uiControl) {
		AbstractSWTRidget.assertType(uiControl, EmbeddedTitleBar.class);
	}

	/**
	 * @see org.eclipse.riena.internal.ui.ridgets.swt.AbstractSWTRidget#unbindUIControl
	 *      ()
	 */
	@Override
	protected void unbindUIControl() {
		// unused
	}

	/**
	 * @see org.eclipse.riena.ui.ridgets.IWindowRidget#addWindowRidgetListener(org.eclipse.riena.ui.ridgets.listener.IWindowRidgetListener)
	 */
	public void addWindowRidgetListener(IWindowRidgetListener listener) {
		windowRidgetListeners.add(listener);
	}

	/**
	 * @see org.eclipse.riena.ui.ridgets.IWindowRidget#removeWindowRidgetListener(org.eclipse.riena.ui.ridgets.listener.IWindowRidgetListener)
	 */
	public void removeWindowRidgetListener(IWindowRidgetListener listener) {
		windowRidgetListeners.remove(listener);
	}

	/**
	 * @see org.eclipse.riena.ui.ridgets.IWindowRidget#getDefaultButton()
	 */
	public Object getDefaultButton() {
		// unused
		return null;
	}

	/**
	 * @see org.eclipse.riena.ui.ridgets.IWindowRidget#setDefaultButton(java.lang
	 *      .Object)
	 */
	public void setDefaultButton(Object defaultButton) {
		// unused
	}

	// helping methods
	// ////////////////

	/**
	 * Updates the text of the UI control.
	 */
	private void updateTextInControl() {
		EmbeddedTitleBar control = getUIControl();
		if (control != null) {
			control.setTitle(this.text);
		}
	}

	/**
	 * Updates the icon of the UI control.
	 */
	private void updateIconInControl() {
		EmbeddedTitleBar control = getUIControl();
		if (control != null) {
			Image image = null;
			if (icon != null) {
				image = getManagedImage(icon);
			}
			// if (image == getMissingImage()) {
			// image = null;
			// }
			control.setImage(image);
		}
	}

}
