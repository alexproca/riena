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

import org.eclipse.riena.ui.ridgets.IRidget;
import org.eclipse.riena.ui.ridgets.IStatuslineNumberRidget;
import org.eclipse.riena.ui.swt.StatuslineNumber;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 *
 */
public class StatuslineNumberRidgetTest extends AbstractSWTRidgetTest {

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * @see org.eclipse.riena.internal.ui.ridgets.swt.AbstractSWTRidgetTest#createRidget()
	 */
	@Override
	protected IRidget createRidget() {
		return new StatuslineNumberRidget();
	}

	/**
	 * @see org.eclipse.riena.internal.ui.ridgets.swt.AbstractSWTRidgetTest#createWidget(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createWidget(Composite parent) {
		return new StatuslineNumber(parent, SWT.NONE);
	}

	/**
	 * @see org.eclipse.riena.internal.ui.ridgets.swt.AbstractSWTRidgetTest#getRidget()
	 */
	@Override
	protected IStatuslineNumberRidget getRidget() {
		return (IStatuslineNumberRidget) super.getRidget();
	}

	/**
	 * @see org.eclipse.riena.internal.ui.ridgets.swt.AbstractSWTRidgetTest#getWidget()
	 */
	@Override
	protected StatuslineNumber getWidget() {
		return (StatuslineNumber) super.getWidget();
	}

	/**
	 * Returns the label of {@code StatuslineNumber}.
	 * 
	 * @return label
	 */
	private Label getLabel() {
		StatuslineNumber statuslineNumber = getWidget();
		Control[] controls = statuslineNumber.getChildren();
		return (Label) controls[0];
	}

	/**
	 * Tests the method {@code setNumber(int)}.
	 */
	public void testSetNumber() {

		getRidget().setNumber(Integer.valueOf((4711)));
		assertEquals("0004711", getLabel().getText());

	}

	/**
	 * Tests the method {@code setNumberString(String)}.
	 */
	public void testSetNumberString() {

		getRidget().setNumberString("0815-12");
		assertEquals("0815-12", getLabel().getText());

	}

}
