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

import junit.framework.TestCase;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.menus.CommandContributionItem;

import org.eclipse.riena.core.test.collect.NonUITestCase;
import org.eclipse.riena.core.util.ReflectionUtils;
import org.eclipse.riena.ui.swt.utils.SwtUtilities;

/**
 * Tests the class {@link AbstractItemRidget}.
 */
@NonUITestCase
public class AbstractItemRidgetTest extends TestCase {

	private AbstractItemRidget itemRidget;
	private Shell shell;
	private ToolBar toolbar;

	@Override
	protected void setUp() throws Exception {
		itemRidget = new ToolItemRidget();
		shell = new Shell();
		toolbar = new ToolBar(shell, SWT.NONE);
	}

	@Override
	protected void tearDown() throws Exception {
		itemRidget = null;
		SwtUtilities.dispose(shell);
		SwtUtilities.dispose(toolbar);
	}

	/**
	 * Tests the method {@code getContributionItem()}.
	 */
	public void testGetContributionItem() {

		assertNull(getContributionItem(itemRidget));

		final ToolItem item = new ToolItem(toolbar, SWT.NONE);
		itemRidget.setUIControl(item);
		assertNull(getContributionItem(itemRidget));

		item.dispose();
		assertNull(getContributionItem(itemRidget));

	}

	/**
	 * Tests the method {@code isEnabled(()}.
	 */
	public void testIsEnabled() {

		final ToolItem item = new ToolItem(toolbar, SWT.NONE);
		itemRidget.setUIControl(item);

		assertTrue(itemRidget.isEnabled());

		itemRidget.setEnabled(false);
		assertFalse(itemRidget.isEnabled());

	}

	public void testFireAction() {
		final FTActionListener listener1 = new FTActionListener();
		final FTActionListener listener2 = new FTActionListener();

		itemRidget.addListener(listener1);

		itemRidget.fireAction();
		itemRidget.fireAction();
		assertEquals(2, listener1.getCount());

		itemRidget.addListener(listener2);
		itemRidget.fireAction();
		assertEquals(3, listener1.getCount());
		assertEquals(1, listener2.getCount());
	}

	// helping methods
	//////////////////

	private CommandContributionItem getContributionItem(final AbstractItemRidget ridget) {
		return ReflectionUtils.invokeHidden(ridget, "getContributionItem", (Object[]) null);
		// return ridget.getContributionItem();
	}

}
