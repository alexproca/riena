package org.eclipse.riena.navigation.ui.e4.part;

import javax.inject.Inject;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.riena.navigation.ApplicationNodeManager;
import org.eclipse.riena.navigation.ui.controllers.ApplicationController;
import org.eclipse.riena.navigation.ui.swt.binding.InjectSwtViewBindingDelegate;
import org.eclipse.riena.ui.swt.DefaultStatuslineContentFactory;
import org.eclipse.riena.ui.swt.GrabCorner;
import org.eclipse.riena.ui.swt.IStatusLineContentFactory;
import org.eclipse.riena.ui.swt.Statusline;
import org.eclipse.riena.ui.swt.StatuslineSpacer;
import org.eclipse.riena.ui.swt.lnf.LnFUpdater;
import org.eclipse.riena.ui.swt.lnf.LnfKeyConstants;
import org.eclipse.riena.ui.swt.lnf.LnfManager;

/**
 * Creates the Riena status line.
 * 
 * @author jdu
 * 
 */
public class StatusLinePart {
	public static final int BOTTOM_OFFSET = 3;

	@Inject
	public void create(final Composite parent) {
		final Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new FormLayout());

		GrabCorner grabCorner = null;

		if (org.eclipse.riena.ui.swt.GrabCorner.isResizeable() && LnfManager.getLnf().getBooleanSetting(LnfKeyConstants.SHELL_HIDE_OS_BORDER)) {
			grabCorner = new GrabCorner(c, SWT.DOUBLE_BUFFERED, true);
			final FormData layoutData = (FormData) grabCorner.getLayoutData();
			layoutData.right.offset = 0;
			layoutData.bottom.offset = 0;
		}
		createStatusLine(c, grabCorner);
	}

	private void createStatusLine(final Composite parent, final Composite grabCorner) {
		//		final IStatusLineContentFactory statusLineFactory = getStatuslineContentFactory(); // TODO from extension point
		final IStatusLineContentFactory statusLineFactory = new DefaultStatuslineContentFactory();
		final Statusline statusLine = new Statusline(parent, SWT.None, StatuslineSpacer.class, statusLineFactory);
		final FormData fd = new FormData();
		//		final Rectangle navigationBounds = TitlelessStackPresentation.calcNavigationBounds(parent);
		fd.top = new FormAttachment(0, 0);
		fd.left = new FormAttachment(0, 0);
		if (grabCorner != null) {
			fd.right = new FormAttachment(grabCorner, 0);
		} else {
			fd.right = new FormAttachment(100, 0);
		}
		fd.bottom = new FormAttachment(100, -BOTTOM_OFFSET);
		statusLine.setLayoutData(fd);
		addUIControl(statusLine, "statusline"); //$NON-NLS-1$

		LnFUpdater.getInstance().updateUIControls(statusLine, true);
	}

	private void addUIControl(final Statusline statusLine, final String bindingId) {
		final InjectSwtViewBindingDelegate binding = new InjectSwtViewBindingDelegate();
		binding.addUIControl(statusLine, bindingId);
		binding.injectAndBind((ApplicationController) ApplicationNodeManager.getApplicationNode().getNavigationNodeController());
	}
}
