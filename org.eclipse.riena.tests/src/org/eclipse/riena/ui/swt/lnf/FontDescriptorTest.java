/*******************************************************************************
 * Copyright (c) 2007, 2009 compeople AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Florian Pirchner - FontDescriptor
 *******************************************************************************/
package org.eclipse.riena.ui.swt.lnf;

import junit.framework.TestCase;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;

import org.eclipse.riena.internal.core.test.collect.NonUITestCase;
import org.eclipse.riena.ui.swt.lnf.rienadefault.RienaDefaultLnf;

/**
 * Tests of the class <code>FontDescriptor</code>.
 * 
 * @since 1.2
 */
@NonUITestCase
public class FontDescriptorTest extends TestCase {

	private RienaDefaultLnf lnf;

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		lnf = LnfManager.getLnf();
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		lnf.setTheme(null);
		LnfManager.dispose();
		lnf = null;
	}

	/**
	 * Test of the method <code>getFont(String, int, int)</code>.
	 * 
	 * @throws Exception
	 *             - handled by JUnit
	 */
	public void testGetFontWithProps() throws Exception {

		lnf.initialize();

		/*
		 * FontDescriptor without key
		 */
		FontDescriptor descriptor = new FontDescriptor(lnf);
		Font font = descriptor.getFont();
		assertNull(font);

		/*
		 * with key and height
		 */
		descriptor.setKey(LnfKeyConstants.EMBEDDED_TITLEBAR_FONT);
		descriptor.setHeight(10);

		/*
		 * Test shared fonts
		 */
		Font font1 = descriptor.getFont();
		Font font2 = descriptor.getFont();
		assertSame(font1, font2);

		/*
		 * Test new instance on changed property
		 */
		descriptor.setStyle(SWT.BOLD);
		Font font3 = descriptor.getFont();
		assertNotSame(font1, font3);

		/*
		 * Test default height
		 */
		descriptor.setHeight(-1);
		assertEquals(lnf.getIntegerSetting(LnfKeyConstants.FONTDESCRIPTOR_DEFAULT_HEIGHT).intValue(), descriptor
				.getHeight());

		/*
		 * FontDescriptor without height == -1
		 */
		descriptor = new FontDescriptor(LnfKeyConstants.EMBEDDED_TITLEBAR_FONT, -1, -1, lnf);
		assertEquals(lnf.getIntegerSetting(LnfKeyConstants.FONTDESCRIPTOR_DEFAULT_HEIGHT).intValue(), descriptor
				.getHeight());
	}
}
