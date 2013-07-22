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
package org.eclipse.riena.core.logging.log4j;

import org.apache.log4j.MDC;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.riena.core.test.RienaTestCase;
import org.eclipse.riena.core.test.collect.NonUITestCase;
import org.eclipse.riena.core.util.VariableManagerUtil;

/**
 * Test the {@code Log4jMDCDiagnosticContext}.
 */
@NonUITestCase
public class Log4jMDCDiagnosticContextTest extends RienaTestCase {

	private Log4jMDCDiagnosticContext mdc;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mdc = new Log4jMDCDiagnosticContext();
	}

	public void testStaticSubstitution() throws CoreException {
		VariableManagerUtil.addVariable("userS", "STATIC");
		mdc.setInitializationData(null, null, "user=${userS}");
		mdc.push();
		assertEquals("STATIC", MDC.get("user"));
		VariableManagerUtil.removeVariable("userS");
		VariableManagerUtil.addVariable("userS", "STATIC2");
		mdc.push();
		assertEquals("STATIC", MDC.get("user"));
	}

	public void testDynamicSubstitution() throws CoreException {
		VariableManagerUtil.addVariable("userD", "DYNAMIC");
		mdc.setInitializationData(null, null, "*user=${userD}");
		mdc.push();
		assertEquals("DYNAMIC", MDC.get("user"));
		VariableManagerUtil.removeVariable("userD");
		VariableManagerUtil.addVariable("userD", "MORE_DYNAMIC");
		mdc.push();
		assertEquals("MORE_DYNAMIC", MDC.get("user"));
	}
}
