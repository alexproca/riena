/*******************************************************************************
 * Copyright (c) 2007, 2008 compeople AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    compeople AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.riena.core.logging;

import org.eclipse.equinox.log.ExtendedLogReaderService;
import org.eclipse.riena.core.injector.Inject;
import org.eclipse.riena.core.service.ServiceInjector;
import org.eclipse.riena.internal.core.Activator;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;

/**
 *
 */
public class LogServiceLogCatcher implements ILogCatcher, LogListener {

	private ServiceInjector logReaderInjector;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.riena.core.logging.ILogCatcher#attach()
	 */
	public void attach() {
		logReaderInjector = Inject.service(LogReaderService.class).useRanking().into(this).andStart(
				Activator.getDefault().getContext());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.riena.core.logging.ILogCatcher#detach()
	 */
	public void detach() {
		logReaderInjector.stop();
	}

	public void bind(LogReaderService logReaderService) {
		if (logReaderService instanceof ExtendedLogReaderService) {
			return;
		}
		logReaderService.addLogListener(this);
	}

	public void unbind(LogReaderService logReaderService) {
		if (logReaderService instanceof ExtendedLogReaderService) {
			return;
		}
		logReaderService.removeLogListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.service.log.LogListener#logged(org.osgi.service.log.LogEntry)
	 */
	public void logged(LogEntry entry) {
		Activator.getDefault().getLogger("Bundle " + entry.getBundle()).log(entry.getServiceReference(), //$NON-NLS-1$
				entry.getLevel(), entry.getMessage(), entry.getException());
	}
}
