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
package org.eclipse.riena.example.client.controllers;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.databinding.observable.list.WritableList;

import org.eclipse.riena.beans.common.Person;
import org.eclipse.riena.beans.common.PersonFactory;
import org.eclipse.riena.example.client.views.MasterDetailsSubModuleView;
import org.eclipse.riena.navigation.ui.controllers.SubModuleController;
import org.eclipse.riena.ui.core.marker.ValidationTime;
import org.eclipse.riena.ui.ridgets.AbstractMasterDetailsDelegate;
import org.eclipse.riena.ui.ridgets.IActionListener;
import org.eclipse.riena.ui.ridgets.IActionRidget;
import org.eclipse.riena.ui.ridgets.ILabelRidget;
import org.eclipse.riena.ui.ridgets.IMasterDetailsRidget;
import org.eclipse.riena.ui.ridgets.IMultipleChoiceRidget;
import org.eclipse.riena.ui.ridgets.IRidgetContainer;
import org.eclipse.riena.ui.ridgets.ISingleChoiceRidget;
import org.eclipse.riena.ui.ridgets.ITextRidget;
import org.eclipse.riena.ui.ridgets.IToggleButtonRidget;
import org.eclipse.riena.ui.ridgets.validation.NotEmpty;
import org.eclipse.riena.ui.swt.MasterDetailsComposite;

/**
 * Demonstrates use of a master/details ridget.
 * 
 * @see IMasterDetailsRidget
 * @see MasterDetailsSubModuleView
 */
public class MasterDetailsSubModuleController2 extends SubModuleController {

	/**
	 * Setup the ridgets for editing a person (text ridgets for name, single
	 * choice ridget for gender, multiple choice ridgets for pets).
	 */
	private final class PersonDelegate extends AbstractMasterDetailsDelegate {

		private final String[] GENDER = { Person.FEMALE, Person.MALE };
		private final Person workingCopy = createWorkingCopy();

		public void configureRidgets(final IRidgetContainer container) {
			final ITextRidget txtFirst = container.getRidget(ITextRidget.class, "first"); //$NON-NLS-1$
			txtFirst.setMandatory(true);
			txtFirst.setDirectWriting(true);
			txtFirst.bindToModel(workingCopy, Person.PROPERTY_FIRSTNAME);
			txtFirst.updateFromModel();

			final ITextRidget txtLast = container.getRidget(ITextRidget.class, "last"); //$NON-NLS-1$
			txtLast.setDirectWriting(true);
			txtLast.addValidationRule(new NotEmpty(), ValidationTime.ON_UI_CONTROL_EDIT);
			txtLast.bindToModel(workingCopy, Person.PROPERTY_LASTNAME);
			txtLast.updateFromModel();

			final ISingleChoiceRidget gender = container.getRidget(ISingleChoiceRidget.class, "gender"); //$NON-NLS-1$
			if (gender != null) {
				gender.bindToModel(Arrays.asList(GENDER), (List<String>) null, workingCopy, Person.PROPERTY_GENDER);
				gender.updateFromModel();
			}

			final IMultipleChoiceRidget pets = container.getRidget(IMultipleChoiceRidget.class, "pets"); //$NON-NLS-1$
			if (pets != null) {
				pets.bindToModel(Arrays.asList(Person.Pets.values()), (List<String>) null, workingCopy,
						Person.PROPERTY_PETS);
				pets.updateFromModel();
			}
		}

		public Person createWorkingCopy() {
			return new Person("", ""); //$NON-NLS-1$ //$NON-NLS-2$
		}

		public Person copyBean(final Object source, final Object target) {
			final Person from = (Person) source;
			final Person to = (Person) target;
			to.setFirstname(from.getFirstname());
			to.setLastname(from.getLastname());
			to.setGender(from.getGender());
			to.setPets(from.getPets());
			return to;
		}

		public Object getWorkingCopy() {
			return workingCopy;
		}

		@Override
		public boolean isChanged(final Object source, final Object target) {
			final Person p1 = (Person) source;
			final Person p2 = (Person) target;
			final boolean equals = p1.getFirstname().equals(p2.getFirstname())
					&& p1.getLastname().equals(p2.getLastname()) && p1.getGender().equals(p2.getGender())
					&& p1.getPets().equals(p2.getPets());
			return !equals;
		}

		@Override
		public String isValid(final IRidgetContainer container) {
			final ITextRidget txtLast = container.getRidget(ITextRidget.class, "last"); //$NON-NLS-1$
			if (txtLast.isErrorMarked()) {
				return "'Last Name' is not valid."; //$NON-NLS-1$
			}
			return null;
		}

		@Override
		public void itemCreated(final Object item) {
			lblStatus.setText("New item created"); //$NON-NLS-1$
		}

		@Override
		public void itemRemoved(final Object item) {
			lblStatus.setText("Item removed: " + item); //$NON-NLS-1$
		}

		@Override
		public void itemApplied(final Object item) {
			lblStatus.setText("Item changed: " + item); //$NON-NLS-1$
		}

		@Override
		public void itemSelected(final Object item) {
			lblStatus.setText("Item selected: " + String.valueOf(item)); //$NON-NLS-1$
		}
	}

	private final List<Person> input = PersonFactory.createPersonList();
	private ILabelRidget lblStatus;

	@Override
	public void configureRidgets() {
		final String[] properties = new String[] { "firstname", "lastname" }; //$NON-NLS-1$ //$NON-NLS-2$
		final String[] headers = new String[] { "First Name", "Last Name" }; //$NON-NLS-1$ //$NON-NLS-2$

		final IMasterDetailsRidget master2 = getRidget(IMasterDetailsRidget.class, "master2"); //$NON-NLS-1$
		master2.setDelegate(new PersonDelegate());
		master2.bindToModel(new WritableList(input, Person.class), Person.class, properties, headers);
		master2.updateFromModel();

		lblStatus = getRidget(ILabelRidget.class, "lblStatus"); //$NON-NLS-1$

		final IActionRidget actionApply = master2.getRidget(IActionRidget.class, MasterDetailsComposite.BIND_ID_APPLY);
		actionApply.setIcon("apply_h.png"); //$NON-NLS-1$

		final IActionRidget actionNew = master2.getRidget(IActionRidget.class, MasterDetailsComposite.BIND_ID_NEW);
		actionNew.setText(""); //$NON-NLS-1$
		actionNew.setIcon("new_h.png"); //$NON-NLS-1$

		final IActionRidget actionRemove = master2
				.getRidget(IActionRidget.class, MasterDetailsComposite.BIND_ID_REMOVE);
		actionRemove.setText(""); //$NON-NLS-1$
		actionRemove.setIcon("remove_h.png"); //$NON-NLS-1$

		final IToggleButtonRidget chkNoErrors = getRidget(IToggleButtonRidget.class, "chkNoErrors"); //$NON-NLS-1$
		chkNoErrors.addListener(new IActionListener() {
			public void callback() {
				final boolean noErrors = chkNoErrors.isSelected();
				master2.setApplyRequiresNoErrors(noErrors);
			}
		});
		chkNoErrors.setSelected(true);

		final IToggleButtonRidget chkNoMandatories = getRidget(IToggleButtonRidget.class, "chkNoMandatories"); //$NON-NLS-1$
		chkNoMandatories.addListener(new IActionListener() {
			public void callback() {
				final boolean noMandatories = chkNoMandatories.isSelected();
				master2.setApplyRequiresNoMandatories(noMandatories);
			}
		});

		addDefaultAction(master2, actionApply);
	}
}
