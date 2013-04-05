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
package org.eclipse.riena.core.injector.extension;

import org.osgi.framework.Bundle;

import org.eclipse.core.runtime.IConfigurationElement;

@ExtensionInterface
public interface IData {

	public enum Color {
		RED, GREEN
	};

	@MapContent
	String getValue();

	String getText();

	@MapName("required")
	boolean getRequired();

	boolean isRequired();

	short getShortNumber();

	int getIntegerNumber();

	long getLongNumber();

	float getFloatNumber();

	double getDoubleNumber();

	char getDelimCharacter();

	byte getJustAByte();

	Object createObjectType();

	@MapName("data")
	IData2 getNews();

	IData3[] getMoreData();

	Bundle getContributingBundle();

	IConfigurationElement getConfigurationElement();

	@MapName("objectType")
	Class<?> getLazyThingType();

	@CreateLazy()
	@MapName("objectType")
	ILazyThing createLazyThing();

	Color getColor();
}
