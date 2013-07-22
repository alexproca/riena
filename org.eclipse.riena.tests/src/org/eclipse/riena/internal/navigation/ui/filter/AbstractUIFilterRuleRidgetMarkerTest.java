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
package org.eclipse.riena.internal.navigation.ui.filter;

import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.widgets.Display;

import org.eclipse.riena.core.marker.IMarker;
import org.eclipse.riena.core.test.collect.NonUITestCase;
import org.eclipse.riena.core.util.ReflectionUtils;
import org.eclipse.riena.internal.ui.ridgets.swt.TextRidget;
import org.eclipse.riena.ui.core.marker.MandatoryMarker;
import org.eclipse.riena.ui.filter.IUIFilterRule;
import org.eclipse.riena.ui.ridgets.IBasicMarkableRidget;
import org.eclipse.riena.ui.ridgets.ITextRidget;

/**
 * Tests of the class {@link AbstractUIFilterRuleRidgetMarker}.
 */
@NonUITestCase
public class AbstractUIFilterRuleRidgetMarkerTest extends TestCase {

	private IUIFilterRule rule;

	@Override
	protected void setUp() throws Exception {

		final Display display = Display.getDefault();
		final Realm realm = SWTObservables.getRealm(display);
		assertNotNull(realm);
		ReflectionUtils.invokeHidden(realm, "setDefault", realm);

		rule = new MyUIFilterRule();
	}

	@Override
	protected void tearDown() throws Exception {
		rule = null;
	}

	/**
	 * Tests the method {@code apply(Object)}
	 */
	public void testApply() {

		final ITextRidget ridget = new TextRidget();
		assertTrue(ridget.getMarkers().isEmpty());

		rule.apply(ridget);
		assertFalse(ridget.getMarkers().isEmpty());
		assertTrue(ridget.getMarkers().contains(new MandatoryMarker(false)));

		final Map<IBasicMarkableRidget, IMarker> markerMap = ReflectionUtils.getHidden(rule, "markerMap");
		assertEquals(new MandatoryMarker(false), markerMap.get(ridget));

	}

	/**
	 * Tests the method {@code remove(Object)}
	 */
	public void testRemove() {

		final Map<IBasicMarkableRidget, IMarker> markerMap = ReflectionUtils.getHidden(rule, "markerMap");

		final ITextRidget ridget = new TextRidget();
		assertTrue(ridget.getMarkers().isEmpty());
		assertTrue(markerMap.isEmpty());

		rule.apply(ridget);
		assertTrue(ridget.getMarkers().contains(new MandatoryMarker(false)));
		assertFalse(markerMap.isEmpty());

		rule.remove(ridget);
		assertTrue(ridget.getMarkers().isEmpty());
		assertTrue(markerMap.isEmpty());

	}

	private static class MyUIFilterRule extends AbstractUIFilterRuleRidgetMarker {

		public MyUIFilterRule() {
			super("*", new MandatoryMarker(false));
		}

	}

}
