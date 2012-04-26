/*******************************************************************************
 * Copyright (c) 2007, 2012 compeople AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    compeople AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.riena.ui.ridgets.databinding;

import java.util.GregorianCalendar;

import junit.framework.TestCase;

import org.easymock.EasyMock;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.conversion.NumberToStringConverter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.riena.core.util.ReflectionUtils;
import org.eclipse.riena.internal.core.test.collect.NonUITestCase;
import org.eclipse.riena.ui.ridgets.ValueBindingSupport;

/**
 * Tests of the class <code>RidgetUpdateValueStrategy</code>.
 */
@NonUITestCase
public class RidgetUpdateValueStrategyTest extends TestCase {

	public void testCreateConverter() throws Exception {

		final RidgetUpdateValueStrategy strategy = new RidgetUpdateValueStrategy(new ValueBindingSupport(EasyMock.createNiceMock(IObservableValue.class)));

		IConverter converter = ReflectionUtils.invokeHidden(strategy, "createConverter", String.class, Double.TYPE);
		assertTrue(converter instanceof StringToNumberAllowingNullConverter);

		converter = ReflectionUtils.invokeHidden(strategy, "createConverter", String.class, Float.TYPE);
		assertTrue(converter instanceof StringToNumberAllowingNullConverter);

		converter = ReflectionUtils.invokeHidden(strategy, "createConverter", String.class, Long.TYPE);
		assertTrue(converter instanceof StringToNumberAllowingNullConverter);

		converter = ReflectionUtils.invokeHidden(strategy, "createConverter", String.class, Integer.TYPE);
		assertTrue(converter instanceof StringToNumberAllowingNullConverter);

		converter = ReflectionUtils.invokeHidden(strategy, "createConverter", String.class, GregorianCalendar.class);
		assertTrue(converter instanceof StringToGregorianCalendarConverter);

		converter = ReflectionUtils.invokeHidden(strategy, "createConverter", GregorianCalendar.class, String.class);
		assertTrue(converter instanceof GregorianCalendarToStringConverter);

		converter = ReflectionUtils.invokeHidden(strategy, "createConverter", Integer.class, String.class);
		assertTrue(converter instanceof NumberToStringConverter);

	}

	public void testConstructors() throws Exception {
		try {
			new RidgetUpdateValueStrategy(null);
			fail("expected RuntimeException");
		} catch (final RuntimeException e) {
			// everything is fine
		}

		try {
			new RidgetUpdateValueStrategy(null, UpdateValueStrategy.POLICY_UPDATE);
			fail("expected RuntimeException");
		} catch (final RuntimeException e) {
			// everything is fine
		}

		try {
			new RidgetUpdateValueStrategy(null, true, UpdateValueStrategy.POLICY_UPDATE);
			fail("expected RuntimeException");
		} catch (final RuntimeException e) {
			// everything is fine
		}

	}

	public void testValidateAfterSetWithSetError() throws Exception {
		final RidgetUpdateValueStrategy strategy = new RidgetUpdateValueStrategy(new ValueBindingSupport(EasyMock.createNiceMock(IObservableValue.class)));
		final IStatus setStatus = new Status(IStatus.ERROR, "plugin.id", "some message");
		assertSame(setStatus, ReflectionUtils.<IStatus> invokeHidden(strategy, "validateAfterSet", setStatus));

		strategy.setAfterSetValidator(EasyMock.createMock(IValidator.class));
		assertSame(setStatus, ReflectionUtils.<Object> invokeHidden(strategy, "validateAfterSet", setStatus));
	}

	public void testValidateAfterSetWithSetOk() throws Exception {
		final RidgetUpdateValueStrategy strategy = new RidgetUpdateValueStrategy(new ValueBindingSupport(EasyMock.createNiceMock(IObservableValue.class)));
		final IStatus setStatus = Status.OK_STATUS;
		assertSame(setStatus, ReflectionUtils.<IStatus> invokeHidden(strategy, "validateAfterSet", setStatus));

		final IStatus s1 = new Status(IStatus.ERROR, "plugin.id", "some message");
		final IValidator validator = EasyMock.createMock(IValidator.class);
		EasyMock.expect(validator.validate(EasyMock.anyObject())).andReturn(s1);
		EasyMock.replay(validator);
		strategy.setAfterSetValidator(validator);
		assertSame(s1, ReflectionUtils.<Object> invokeHidden(strategy, "validateAfterSet", setStatus));
		EasyMock.verify(validator);
	}
}
