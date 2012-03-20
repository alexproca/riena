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
package org.eclipse.riena.security.authorizationservice;

import java.io.InputStream;

import javax.security.auth.Subject;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import org.eclipse.riena.core.service.Service;
import org.eclipse.riena.internal.core.test.RienaTestCase;
import org.eclipse.riena.internal.core.test.collect.ManualTestCase;
import org.eclipse.riena.internal.security.authorizationservice.AuthorizationService;
import org.eclipse.riena.internal.tests.Activator;
import org.eclipse.riena.security.common.ISubjectHolder;
import org.eclipse.riena.security.common.authentication.SimplePrincipal;
import org.eclipse.riena.security.common.authorization.IAuthorizationService;
import org.eclipse.riena.security.simpleservices.authorizationservice.store.FilePermissionStore;

@ManualTestCase
public class AuthorizationTest extends RienaTestCase {

	//	private ServiceRegistration fileStoreReg;
	private ServiceRegistration authorizationServiceReg;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// create FilePermissionStore which we inject into a local AuthorizationService
		final InputStream inputStream = this.getClass().getResourceAsStream("policy-def-test.xml"); //$NON-NLS-1$
		final FilePermissionStore store = new FilePermissionStore(inputStream);
		final ServiceReference ref = getContext().getServiceReference(IAuthorizationService.class.getName());
		if (ref != null && ref.getBundle().getState() == Bundle.ACTIVE
				&& ref.getBundle() != Activator.getDefault().getBundle()) {
			ref.getBundle().stop();
		}
		// create and register a local AuthorizationService with a dummy permission store
		final AuthorizationService authorizationService = new AuthorizationService();
		authorizationServiceReg = getContext().registerService(IAuthorizationService.class.getName(),
				authorizationService, null);
		// inject my test filestore
		authorizationService.bind(store);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		authorizationServiceReg.unregister();
	}

	public void testWithoutUser() {
		final boolean result = new BusinessTestCase().hasPermission();
		assertFalse("BusinessTestCase must fail without user", result);
	}

	public void testWithValidUser() {
		final Subject subject = new Subject();
		subject.getPrincipals().add(new SimplePrincipal("testuser"));
		Service.get(ISubjectHolder.class).setSubject(subject);

		final boolean result = new BusinessTestCase().hasPermission();

		assertTrue("BusinessTestCase must work with valid user", result);
	}

	public void testWithInvalidUser() {
		final Subject subject = new Subject();
		subject.getPrincipals().add(new SimplePrincipal("anotheruser"));
		Service.get(ISubjectHolder.class).setSubject(subject);

		final boolean result = new BusinessTestCase().hasPermission();

		assertFalse("BusinessTestCase must fail with invalid user", result);
	}
}
