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
package org.eclipse.riena.client.controller.test;

import java.util.Arrays;

import org.eclipse.riena.client.controller.test.ChoiceSubModuleController.CarModels;
import org.eclipse.riena.client.controller.test.ChoiceSubModuleController.CarOptions;
import org.eclipse.riena.internal.core.test.collect.NonUITestCase;
import org.eclipse.riena.navigation.ISubModuleNode;
import org.eclipse.riena.navigation.ui.swt.controllers.AbstractSubModuleControllerTest;
import org.eclipse.riena.ui.ridgets.IActionRidget;
import org.eclipse.riena.ui.ridgets.IMultipleChoiceRidget;
import org.eclipse.riena.ui.ridgets.ISingleChoiceRidget;

/**
 * Tests for the ChoiceSubModuleController.
 */
@NonUITestCase
public class ChoiceSubModuleControllerTest extends AbstractSubModuleControllerTest<ChoiceSubModuleController> {

	@Override
	protected ChoiceSubModuleController createController(final ISubModuleNode node) {
		return new ChoiceSubModuleController(node);
	}

	public void testPriceAstonMartin() {
		final ISingleChoiceRidget compositeCarModel = getController().getRidget(ISingleChoiceRidget.class,
				"compositeCarModel"); //$NON-NLS-1$
		compositeCarModel.setSelection(CarModels.ASTON_MARTIN);

		assertEquals(compositeCarModel.getSelection(), CarModels.ASTON_MARTIN);
		assertEquals(getController().getCarConfig().getPrice(), 100000);
	}

	public void testPriceAstonMartinWithOptions() {

		final ISingleChoiceRidget compositeCarModel = getController().getRidget(ISingleChoiceRidget.class,
				"compositeCarModel"); //$NON-NLS-1$
		final IMultipleChoiceRidget compositeCarExtras = getController().getRidget(IMultipleChoiceRidget.class,
				"compositeCarExtras"); //$NON-NLS-1$

		compositeCarModel.setSelection(CarModels.ASTON_MARTIN);
		compositeCarExtras.setSelection(Arrays.asList(CarOptions.FRONT_GUNS, CarOptions.UNDERWATER));

		assertEquals(compositeCarModel.getSelection(), CarModels.ASTON_MARTIN);
		assertEquals(compositeCarExtras.getSelection().size(), 2);
		assertEquals(compositeCarExtras.getSelection().get(0), CarOptions.FRONT_GUNS);
		assertEquals(compositeCarExtras.getSelection().get(1), CarOptions.UNDERWATER);

		assertEquals(getController().getCarConfig().getPrice(), 150000);
	}

	public void testQuickConfig() {

		final IActionRidget buttonPreset = getController().getRidget(IActionRidget.class, "buttonPreset"); //$NON-NLS-1$
		buttonPreset.fireAction();

		final ISingleChoiceRidget compositeCarModel = getController().getRidget(ISingleChoiceRidget.class,
				"compositeCarModel"); //$NON-NLS-1$
		final IMultipleChoiceRidget compositeCarExtras = getController().getRidget(IMultipleChoiceRidget.class,
				"compositeCarExtras"); //$NON-NLS-1$

		assertEquals(compositeCarModel.getSelection(), ChoiceSubModuleController.CarModels.BMW);
		assertEquals(compositeCarExtras.getSelection().size(), 1);
		assertEquals(compositeCarExtras.getSelection().get(0), CarOptions.PDCS);
		assertEquals(getController().getCarConfig().getPrice(), 135200);
	}

	public void testReset() {

		final IActionRidget buttonReset = getController().getRidget(IActionRidget.class, "buttonReset"); //$NON-NLS-1$
		buttonReset.fireAction();

		final ISingleChoiceRidget compositeCarModel = getController().getRidget(ISingleChoiceRidget.class,
				"compositeCarModel"); //$NON-NLS-1$
		final IMultipleChoiceRidget compositeCarExtras = getController().getRidget(IMultipleChoiceRidget.class,
				"compositeCarExtras"); //$NON-NLS-1$

		assertNull(compositeCarModel.getSelection());
		assertEquals(compositeCarExtras.getSelection().size(), 0);
		assertEquals(getController().getCarConfig().getPrice(), 0);
	}
}
