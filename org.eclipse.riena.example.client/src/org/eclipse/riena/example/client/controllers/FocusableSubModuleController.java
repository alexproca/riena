/*******************************************************************************
 * Copyright (c) 2007, 2009 compeople AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    compeople AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.riena.example.client.controllers;

import org.eclipse.core.runtime.Assert;

import org.eclipse.riena.example.client.views.FocusableSubModuleView;
import org.eclipse.riena.navigation.ISubModuleNode;
import org.eclipse.riena.navigation.ui.controllers.SubModuleController;
import org.eclipse.riena.ui.ridgets.IActionListener;
import org.eclipse.riena.ui.ridgets.ITextRidget;
import org.eclipse.riena.ui.ridgets.IToggleButtonRidget;

/**
 * Controller for the {@link FocusableSubModuleView} example.
 */
public class FocusableSubModuleController extends SubModuleController {

	public FocusableSubModuleController() {
		this(null);
	}

	public FocusableSubModuleController(ISubModuleNode navigationNode) {
		super(navigationNode);
	}

	/**
	 * Binds and updates the ridgets.
	 * 
	 * @see org.eclipse.riena.ui.ridgets.IRidgetContainer#configureRidgets()
	 */
	@Override
	public void configureRidgets() {

		final IToggleButtonRidget checkVisible = (IToggleButtonRidget) getRidget("checkVisible"); //$NON-NLS-1$
		final IToggleButtonRidget buttonA0 = (IToggleButtonRidget) getRidget("buttonA0"); //$NON-NLS-1$
		final IToggleButtonRidget buttonA1 = (IToggleButtonRidget) getRidget("buttonA1"); //$NON-NLS-1$
		final IToggleButtonRidget buttonA2 = (IToggleButtonRidget) getRidget("buttonA2"); //$NON-NLS-1$
		final IToggleButtonRidget buttonA3 = (IToggleButtonRidget) getRidget("buttonA3"); //$NON-NLS-1$
		final IToggleButtonRidget buttonA4 = (IToggleButtonRidget) getRidget("buttonA4"); //$NON-NLS-1$
		final IToggleButtonRidget buttonB0 = (IToggleButtonRidget) getRidget("buttonB0"); //$NON-NLS-1$

		final ITextRidget textA0 = (ITextRidget) getRidget("textA0"); //$NON-NLS-1$
		final ITextRidget textA1 = (ITextRidget) getRidget("textA1"); //$NON-NLS-1$
		final ITextRidget textA2 = (ITextRidget) getRidget("textA2"); //$NON-NLS-1$
		final ITextRidget textA3 = (ITextRidget) getRidget("textA3"); //$NON-NLS-1$
		final ITextRidget textA4 = (ITextRidget) getRidget("textA4"); //$NON-NLS-1$
		final ITextRidget textB0 = (ITextRidget) getRidget("textB0"); //$NON-NLS-1$

		final IToggleButtonRidget[] checkButtons = new IToggleButtonRidget[] { buttonA0, buttonA1, buttonA2, buttonA3,
				buttonA4, buttonB0 };
		final ITextRidget[] textRidgets = new ITextRidget[] { textA0, textA1, textA2, textA3, textA4, textB0 };

		checkVisible.setText("show checkboxes"); //$NON-NLS-1$
		checkVisible.setSelected(true);
		checkVisible.addListener(new IActionListener() {
			public void callback() {
				boolean show = checkVisible.isSelected();
				for (IToggleButtonRidget check : checkButtons) {
					check.setVisible(show);
				}
			}
		});

		Assert.isLegal(checkButtons.length == textRidgets.length);
		for (int i = 0; i < checkButtons.length; i++) {
			IToggleButtonRidget check = checkButtons[i];
			check.setText("make focusable"); //$NON-NLS-1$
			check.setSelected(true);
			IActionListener listener = new ChangeFocusableCallback(check, textRidgets[i]);
			check.addListener(listener);
		}

		for (int i = 0; i < textRidgets.length; i++) {
			textRidgets[i].setText("Text Field #" + i); //$NON-NLS-1$
		}
	}

	// helping classes
	// ////////////////

	private static final class ChangeFocusableCallback implements IActionListener {
		private final IToggleButtonRidget buttonCheck;
		private final ITextRidget textRidget;

		private ChangeFocusableCallback(IToggleButtonRidget buttonCheck, ITextRidget textRidget) {
			this.buttonCheck = buttonCheck;
			this.textRidget = textRidget;
		}

		public void callback() {
			boolean isSelected = buttonCheck.isSelected();
			textRidget.setFocusable(isSelected);
		}
	}

}
