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
package org.eclipse.riena.example.client.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import org.eclipse.riena.example.client.controllers.MasterDetailsSubModuleController;
import org.eclipse.riena.navigation.ui.swt.views.SubModuleView;
import org.eclipse.riena.ui.ridgets.IMasterDetailsRidget;
import org.eclipse.riena.ui.swt.ChoiceComposite;
import org.eclipse.riena.ui.swt.MasterDetailsComposite;
import org.eclipse.riena.ui.swt.lnf.LnfKeyConstants;
import org.eclipse.riena.ui.swt.lnf.LnfManager;
import org.eclipse.riena.ui.swt.utils.UIControlsFactory;

/**
 * Demonstrates use of a master/details ridget.
 * 
 * @see IMasterDetailsRidget
 * @see MasterDetailsSubModuleController
 */
public class MasterDetailsSubModuleView extends SubModuleView {

	public static final String ID = MasterDetailsSubModuleView.class.getName();

	@Override
	protected void basicCreatePartControl(final Composite parent) {

		parent.setBackground(LnfManager.getLnf().getColor(LnfKeyConstants.SUB_MODULE_BACKGROUND));

		parent.setLayout(new GridLayout(1, false));

		final Composite composite = UIControlsFactory.createComposite(parent);
		final GridData data = new GridData();
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = SWT.FILL;
		composite.setLayoutData(data);

		final FillLayout layout = new FillLayout(SWT.HORIZONTAL);
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		composite.setLayout(layout);
		createMasterDetails(composite);

		UIControlsFactory.createButton(parent, "enable/disable", "enableDisable"); //$NON-NLS-1$ //$NON-NLS-2$

	}

	// helping methods
	//////////////////

	private Group createMasterDetails(final Composite parent) {
		final Group result = UIControlsFactory.createGroup(parent, "Master/Details:"); //$NON-NLS-1$
		final FillLayout layout = new FillLayout(SWT.HORIZONTAL);
		layout.marginHeight = 20;
		layout.marginWidth = 20;
		result.setLayout(layout);

		final MasterDetailsComposite mdComposite = new MasterDetailsComposite(result, SWT.NONE, SWT.BOTTOM);
		final Composite details = mdComposite.getDetails();
		final GridLayout layout2 = new GridLayout(2, false);
		details.setLayout(layout2);

		UIControlsFactory.createLabel(details, "First Name:"); //$NON-NLS-1$
		final Text txtFirst = UIControlsFactory.createText(details, SWT.BORDER, "first"); //$NON-NLS-1$
		txtFirst.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		UIControlsFactory.createLabel(details, "Last Name:"); //$NON-NLS-1$
		final Text txtLast = UIControlsFactory.createText(details, SWT.BORDER, "last"); //$NON-NLS-1$
		txtLast.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		UIControlsFactory.createLabel(details, "Gender:"); //$NON-NLS-1$
		final ChoiceComposite ccGender = new ChoiceComposite(details, SWT.NONE, false);
		ccGender.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		ccGender.setOrientation(SWT.HORIZONTAL);
		mdComposite.addUIControl(ccGender, "gender"); //$NON-NLS-1$

		UIControlsFactory.createLabel(details, "Pets:"); //$NON-NLS-1$
		final ChoiceComposite ccPets = new ChoiceComposite(details, SWT.NONE, true);
		ccPets.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		ccPets.setOrientation(SWT.HORIZONTAL);
		mdComposite.addUIControl(ccPets, "pets"); //$NON-NLS-1$

		this.addUIControl(mdComposite, "master"); //$NON-NLS-1$

		//DefaultButtonManager dbm = new DefaultButtonManager(parent.getShell());
		//dbm.addButton(mdComposite.getButtonApply(), mdComposite);

		return result;
	}
}
