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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.osgi.service.log.LogService;

import org.eclipse.riena.core.logging.ConsoleLogger;
import org.eclipse.riena.ui.ridgets.AbstractRidget;
import org.eclipse.riena.ui.ridgets.IInfoFlyoutRidget;
import org.eclipse.riena.ui.ridgets.uibinding.IBindingPropertyLocator;
import org.eclipse.riena.ui.swt.InfoFlyout;
import org.eclipse.riena.ui.swt.facades.SWTFacade;
import org.eclipse.riena.ui.swt.lnf.LnfKeyConstants;
import org.eclipse.riena.ui.swt.lnf.LnfManager;
import org.eclipse.riena.ui.swt.uiprocess.SwtUISynchronizer;
import org.eclipse.riena.ui.swt.utils.SWTBindingPropertyLocator;

/**
 * A ridget for the {@link InfoFlyout}
 */
public class InfoFlyoutRidget extends AbstractRidget implements IInfoFlyoutRidget {

	private InfoFlyout infoFlyout;
	private BlockingQueue<InfoFlyoutData> infos;
	private SwtUISynchronizer syncher;
	private Worker worker = null;

	public InfoFlyoutRidget() {
		super();
		infos = new LinkedBlockingQueue<InfoFlyoutData>();
		syncher = new SwtUISynchronizer();
	}

	public InfoFlyoutRidget(final InfoFlyout infoFlyout) {
		setUIControl(infoFlyout);
	}

	public InfoFlyout getUIControl() {
		return infoFlyout;
	}

	public void setUIControl(final Object uiControl) {
		infoFlyout = (InfoFlyout) uiControl;
	}

	public String getID() {
		final IBindingPropertyLocator locator = SWTBindingPropertyLocator.getInstance();
		return locator.locateBindingProperty(getUIControl());
	}

	public void addInfo(final InfoFlyoutData info) {
		if (worker == null) {
			worker = new Worker();
			worker.start();
		}
		SWTFacade.getDefault().beforeInfoFlyoutShow(infoFlyout);
		try {
			infos.put(info);
		} catch (final InterruptedException e) {
			new ConsoleLogger(InfoFlyoutRidget.class.getName()).log(LogService.LOG_ERROR,
					"Queueing info failed: " + info, e); //$NON-NLS-1$
		}
	}

	private class Worker extends Thread {

		private final int sleepTime = LnfManager.getLnf().getIntegerSetting(
				LnfKeyConstants.INFO_FLYOUT_SHOW_AND_HIDE_ANIMATION_TIME)
				* 2
				+ LnfManager.getLnf().getIntegerSetting(LnfKeyConstants.INFO_FLYOUT_WAIT_ANIMATION_TIME)
				+ LnfManager.getLnf().getIntegerSetting(LnfKeyConstants.INFO_FLYOUT_PAUSE_ANIMATION_TIME);

		@Override
		public void run() {
			InfoFlyoutData info;
			while (true) {
				try {
					info = infos.take();
					processInfo(info);
					Thread.sleep(sleepTime);
				} catch (final InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
			}
		}

		private void processInfo(final InfoFlyoutData info) {

			infoFlyout.waitForClosing();

			syncher.syncExec(new Runnable() {
				public void run() {
					try {
						infoFlyout.setMessage(info.getMessage());
						infoFlyout.setIcon(info.getIcon());
						infoFlyout.openFlyout();
					} finally {
						if (infos.isEmpty()) {
							SWTFacade.getDefault().afterInfoFlyoutShow(infoFlyout);
						}
					}
				}
			});

		}
	}

	/// methods that are not really needed
	//////////////////////////////////////

	public void requestFocus() {
		throw new UnsupportedOperationException("not supported"); //$NON-NLS-1$
	}

	public boolean hasFocus() {
		throw new UnsupportedOperationException("not supported"); //$NON-NLS-1$
	}

	public boolean isFocusable() {
		throw new UnsupportedOperationException("not supported"); //$NON-NLS-1$
	}

	public void setFocusable(final boolean focusable) {
		throw new UnsupportedOperationException("not supported"); //$NON-NLS-1$
	}

	public String getToolTipText() {
		throw new UnsupportedOperationException("not supported"); //$NON-NLS-1$
	}

	public void setToolTipText(final String toolTipText) {
		throw new UnsupportedOperationException("not supported"); //$NON-NLS-1$
	}

	public boolean isEnabled() {
		throw new UnsupportedOperationException("not supported"); //$NON-NLS-1$
	}

	public void setEnabled(final boolean enabled) {
		throw new UnsupportedOperationException("not supported"); //$NON-NLS-1$
	}

	public boolean isVisible() {
		throw new UnsupportedOperationException("not supported"); //$NON-NLS-1$
	}

	public void setVisible(final boolean visible) {
		throw new UnsupportedOperationException("not supported"); //$NON-NLS-1$
	}

}
