/*******************************************************************************
 * Copyright (c) 2007, 2009 compeople AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    compeople AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.riena.internal.communication.hessian;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import junit.framework.TestCase;

import com.caucho.hessian.io.AbstractHessianInput;
import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.AbstractSerializerFactory;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.caucho.hessian.io.SerializerFactory;

import org.eclipse.core.runtime.Assert;

import org.eclipse.riena.core.util.ReflectionUtils;
import org.eclipse.riena.tests.collect.NonUITestCase;

/**
 * Abstract {@code TestCase} supporting tests of Hessian serializer factories.
 */
@NonUITestCase
public abstract class AbstractSerializerFactoryTestCase extends TestCase {

	protected enum HessianSerializerVersion {
		One, Two
	};

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		removeUnwantedSerializerFactories();
	}

	/**
	 * This must be kept in sync with {@code
	 * org.eclipse.riena.internal.communication.factory.hessian.
	 * RienaSerializerFactory.prepareHessianSerializerFactory()}
	 */
	private void removeUnwantedSerializerFactories() {
		HashMap<?, ?> staticDeSerMap = ReflectionUtils.getHidden(SerializerFactory.class, "_staticDeserializerMap"); //$NON-NLS-1$
		staticDeSerMap.remove(java.io.InputStream.class);
		staticDeSerMap.remove(StackTraceElement.class);
		HashMap<?, ?> staticSerMap = ReflectionUtils.getHidden(SerializerFactory.class, "_staticSerializerMap"); //$NON-NLS-1$
		staticSerMap.remove(java.io.InputStream.class);
	}

	/**
	 * Check whether the given object is equal to the object returned after a
	 * round trip of serialization and deserialization.
	 * 
	 * @param object
	 * @param abstractSerializerFactories
	 * @throws IOException
	 */
	protected boolean isBackAndForthOk(Object object, Class<?> asReturnType,
			AbstractSerializerFactory... abstractSerializerFactories) {
		return isBackAndForthOk(object, HessianSerializerVersion.One, asReturnType, abstractSerializerFactories)
				&& isBackAndForthOk(object, HessianSerializerVersion.Two, asReturnType, abstractSerializerFactories);
	}

	/**
	 * Check whether the given object is equal to the object returned after a
	 * round trip of serialization and deserialization.
	 * 
	 * @param object
	 * @param version
	 * @param abstractSerializerFactories
	 * @throws IOException
	 */
	protected boolean isBackAndForthOk(Object expected, HessianSerializerVersion version, Class<?> asReturnType,
			AbstractSerializerFactory... abstractSerializerFactories) {
		Assert.isNotNull(expected, "expected value MUST not be null");
		try {
			return expected.equals(inAndOut(expected, version, asReturnType, abstractSerializerFactories));
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Make a round-trip: first serialize given object and then deserialize it
	 * back.
	 * 
	 * @param object
	 * @param version
	 * @param asReturnType
	 * @param abstractSerializerFactories
	 * @return
	 * @throws IOException
	 */
	protected Object inAndOut(Object object, HessianSerializerVersion version, Class<?> asReturnType,
			AbstractSerializerFactory... abstractSerializerFactories) throws IOException {

		// Serialization
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		AbstractHessianOutput out = version == HessianSerializerVersion.Two ? new Hessian2Output(baos)
				: new HessianOutput(baos);
		SerializerFactory factory = out.findSerializerFactory();
		for (AbstractSerializerFactory serializerFactory : abstractSerializerFactories) {
			factory.addFactory(serializerFactory);
		}
		factory.setAllowNonSerializable(true);
		out.writeObject(object);
		out.close();

		// Deserialization
		InputStream is = new ByteArrayInputStream(baos.toByteArray());
		AbstractHessianInput in = version == HessianSerializerVersion.Two ? new Hessian2Input(is)
				: new HessianInput(is);
		in.setSerializerFactory(factory);
		return in.readObject(asReturnType);
	}

}
