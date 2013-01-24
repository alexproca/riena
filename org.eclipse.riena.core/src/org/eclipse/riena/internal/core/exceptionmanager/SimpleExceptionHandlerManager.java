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
package org.eclipse.riena.internal.core.exceptionmanager;

import java.util.List;

import org.osgi.service.log.LogService;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.equinox.log.Logger;

import org.eclipse.riena.core.Log4r;
import org.eclipse.riena.core.exception.IExceptionHandler;
import org.eclipse.riena.core.exception.IExceptionHandlerManager;
import org.eclipse.riena.core.injector.InjectionFailure;
import org.eclipse.riena.core.util.Orderer;
import org.eclipse.riena.core.util.StringUtils;
import org.eclipse.riena.core.wire.InjectExtension;
import org.eclipse.riena.core.wire.InjectService;
import org.eclipse.riena.internal.core.Activator;

/**
 * A simple implementation of a {@code IExceptionHandlerManager}.<br>
 * This exception handler manager asks the configured {@code IExceptionHandler}s
 * whether they can handle a given exception, e.g. display the exception in
 * dialog or log it.<br>
 * Additionally it listens for exceptions thrown within {@code Job}s and handles
 * them.
 */
public class SimpleExceptionHandlerManager implements IExceptionHandlerManager {

	private List<IExceptionHandler> handlers;

	private final IJobChangeListener jobChangeListener = new JobChangeListener();

	private final static Logger LOGGER = Log4r.getLogger(Activator.getDefault(), SimpleExceptionHandlerManager.class);

	@InjectExtension
	public void update(final IExceptionHandlerExtension[] exceptionHandlerExtensions) {
		final Orderer<IExceptionHandler> orderer = new Orderer<IExceptionHandler>();
		for (final IExceptionHandlerExtension extension : exceptionHandlerExtensions) {
			orderer.add(extension.createExceptionHandler(), extension.getName(), extension.getPreHandlers(),
					getPostHandler(extension));
		}
		handlers = orderer.getOrderedObjects();
	}

	@InjectService
	public void bind(final IJobManager jobManager) {
		jobManager.addJobChangeListener(jobChangeListener);
	}

	public void unbind(final IJobManager jobManager) {
		jobManager.removeJobChangeListener(jobChangeListener);
	}

	/**
	 * Handle the transition from deprecated 'before' to new 'postHandler'.
	 */
	@SuppressWarnings("deprecation")
	private String getPostHandler(final IExceptionHandlerExtension extension) {
		if (StringUtils.isGiven(extension.getBefore()) && StringUtils.isGiven(extension.getPostHandlers())) {
			throw new InjectionFailure(
					"ExcetionHandler definition " //$NON-NLS-1$
							+ extension.getName()
							+ "uses both the deprecated 'before' and new 'getPostHandler' attributes. Use only 'getPostHandler'"); //$NON-NLS-1$
		}

		if (StringUtils.isGiven(extension.getBefore())) {
			return extension.getBefore();
		}
		if (StringUtils.isGiven(extension.getPostHandlers())) {
			return extension.getPostHandlers();
		}
		return null;
	}

	public IExceptionHandler.Action handleException(final Throwable t) {
		return handleException(t, null, LOGGER);
	}

	public IExceptionHandler.Action handleException(final Throwable t, final Logger logger) {
		return handleException(t, null, logger);
	}

	public IExceptionHandler.Action handleException(final Throwable t, final String msg) {
		return handleException(t, msg, LOGGER);
	}

	public IExceptionHandler.Action handleException(final Throwable t, final String msg, final Logger logger) {
		for (final IExceptionHandler handler : handlers) {
			try {
				final IExceptionHandler.Action action = handler.handleException(t, msg, logger);
				if (action != IExceptionHandler.Action.NOT_HANDLED) {
					return action;
				}
			} catch (final Throwable t2) {
				// if an exception handler throws an exception there is nothing we can do, log problem and try the next exception handler
				LOGGER.log(LogService.LOG_ERROR, "exception handler " + handler.getClass() //$NON-NLS-1$
						+ "#handleException() throws an exception", t2); //$NON-NLS-1$
			}
		}
		return IExceptionHandler.Action.NOT_HANDLED;
	}

	private class JobChangeListener extends JobChangeAdapter {

		@Override
		public void done(final IJobChangeEvent event) {
			final IStatus result = event.getResult();
			if (result != null && result.getSeverity() == IStatus.ERROR && result.getException() != null) {
				handleException(result.getException(), result.getMessage());
			}
		}

	}

}
