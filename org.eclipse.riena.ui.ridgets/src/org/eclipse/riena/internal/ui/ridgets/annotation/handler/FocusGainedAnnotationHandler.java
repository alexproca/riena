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
package org.eclipse.riena.internal.ui.ridgets.annotation.handler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.eclipse.riena.ui.ridgets.IRidget;
import org.eclipse.riena.ui.ridgets.IRidgetContainer;
import org.eclipse.riena.ui.ridgets.annotation.OnFocusGained;
import org.eclipse.riena.ui.ridgets.annotation.handler.AbstractRidgetContainerAnnotationHandler;
import org.eclipse.riena.ui.ridgets.annotation.processor.AnnotatedOverriddenMethodsGuard;
import org.eclipse.riena.ui.ridgets.listener.IFocusListener;

/**
 * Annotation handler for {@code @OnFocusGained}
 * 
 * @since 3.0
 */
public class FocusGainedAnnotationHandler extends AbstractRidgetContainerAnnotationHandler {

	public void handleAnnotation(final Annotation annotation, final IRidgetContainer ridgetContainer,
			final Object target, final Method targetMethod, final AnnotatedOverriddenMethodsGuard guard) {

		if (annotation instanceof OnFocusGained) {
			final IRidget ridget = getRidget(annotation, targetMethod, ridgetContainer,
					((OnFocusGained) annotation).ridgetId());
			ridget.addFocusListener(createListener(IFocusListener.class, "focusGained", target, targetMethod)); //$NON-NLS-1$
		}
	}
}
