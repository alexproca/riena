/*******************************************************************************
 * Copyright (c) 2010 compeople AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    compeople AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.riena.internal.ui.ridgets.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import org.eclipse.riena.ui.ridgets.IDateTextRidget;
import org.eclipse.riena.ui.ridgets.IRidget;
import org.eclipse.riena.ui.swt.DatePickerComposite;
import org.eclipse.riena.ui.swt.utils.UIControlsFactory;

/**
 * A {@link DateTextRidget} can wrap either a {@link Text} or a
 * {@link DatePickerComposite} widget. This test case is for DateTextRidget
 * using DatePickerComposite as the ridget's underlying control.
 * 
 * @see DateTextRidgetTest
 */
public class DateTextRidgetWithDatePickerCompositeTest extends AbstractSWTRidgetTest {

	@Override
	protected IRidget createRidget() {
		DateTextRidget result = new DateTextRidget();
		result.setFormat(IDateTextRidget.FORMAT_DDMMYYYY);
		return result;
	}

	@Override
	protected DateTextRidget getRidget() {
		return (DateTextRidget) super.getRidget();
	}

	@Override
	protected Control createWidget(Composite parent) {
		Control result = new DatePickerComposite(getShell(), SWT.SINGLE | SWT.RIGHT);
		result.setData(UIControlsFactory.KEY_TYPE, UIControlsFactory.TYPE_DATE);
		result.setLayoutData(new RowData(100, SWT.DEFAULT));
		return result;
	}

	@Override
	protected DatePickerComposite getWidget() {
		return (DatePickerComposite) super.getWidget();
	}

	/**
	 * With a DatePickerComposite as the ridget's underlying control,
	 * {@link IRidget#getUIControl()} returns a reference to the
	 * DatePickerComposite widget, and {@link TextRidget#getTextWidget()}
	 * returns a reference to the Text widget contained inside the
	 * DatePickerComposite instance.
	 */
	public void testUIControlVsTextWidgetDistinction() {
		DateTextRidget ridget = getRidget();

		assertTrue(ridget.getUIControl() instanceof DatePickerComposite);
		assertTrue(ridget.getTextWidget() instanceof Text);
	}

	/**
	 * Test to make sure that the whole of the date picker is hidden and
	 * re-shown. (It notably tests that not just the Text widget contained in
	 * the DatePickerComposite is hidden but the whole composite.)
	 */
	public void testVisibility() {
		DateTextRidget ridget = getRidget();
		Text text = ridget.getTextWidget();

		assertTrue(text.isVisible());

		ridget.setVisible(false);

		assertFalse(ridget.isVisible());
		assertFalse(text.getParent().isVisible());
		assertFalse(text.isVisible());
		assertTrue(text.getVisible());

		ridget.setVisible(true);

		assertTrue(ridget.isVisible());
		assertTrue(text.isVisible());
		assertTrue(text.getParent().isVisible());
	}

}
