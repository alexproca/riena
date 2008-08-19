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
package org.eclipse.riena.internal.sample.app.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 */
public class BasicAuthenticationTestServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String auth = req.getHeader("Authorization");
		String correctAuth = "Basic c2NwOnNjcHRlc3RwYXNzd29yZA==";// encoded
		// version
		// of
		// userid=scp,
		// password=scptestpassword
		if (auth != null && auth.equals(correctAuth)) {
			resp.getOutputStream().write("OK".getBytes());
		} else {
			resp.sendError(401);
		}

	}

}
