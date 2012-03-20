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
package org.eclipse.riena.internal.communication.factory.hessian;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import com.caucho.hessian.client.HessianProxyFactory;
import com.caucho.hessian.io.AbstractHessianInput;
import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.Hessian2Output;

import org.eclipse.riena.communication.core.hooks.ICallMessageContext;
import org.eclipse.riena.communication.core.hooks.ICallMessageContextAccessor;
import org.eclipse.riena.internal.communication.factory.hessian.serializer.RienaSerializerFactory;

public class RienaHessianProxyFactory extends HessianProxyFactory {

	private ICallMessageContextAccessor mca;
	private static boolean transferDataChunked = false; // set chunking to FALSE by default overwriting the hessian default
	private int connectTimeout = -1;
	private boolean isZipClientRequest = false;

	private final static ThreadLocal<HttpURLConnection> CONNECTIONS = new ThreadLocal<HttpURLConnection>();

	public RienaHessianProxyFactory() {
		super();
		getSerializerFactory().setAllowNonSerializable(true);
		getSerializerFactory().addFactory(new RienaSerializerFactory());
		setHessian2Request(true);
		setHessian2Reply(true);
		setChunkedPost(transferDataChunked);
	}

	/**
	 * By default data is transfered NOT chunked. This method tests the current
	 * setting.
	 * 
	 * @return true if chunking for transfer is enabled, (false=DEFAULT)
	 */
	public static boolean isTransferDataChunked() {
		return transferDataChunked;
	}

	/**
	 * By default data is transfered NOT chunked, this method can be used to
	 * enable it.
	 * 
	 * @param transferDataChunked
	 *            true if data just be transfered in chunks (false=DEFAULT)
	 */
	public static void setTransferDataChunked(final boolean transferDataChunked) {
		RienaHessianProxyFactory.transferDataChunked = transferDataChunked;
	}

	public void setConnectTimeout(final int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	/**
	 * @param zipClientRequest
	 *            the zipClientRequest to set
	 */
	public void setZipClientRequest(final boolean isZipClientRequest) {
		this.isZipClientRequest = isZipClientRequest;
	}

	@Override
	protected URLConnection openConnection(final URL url) throws IOException {
		URLConnection connection;
		final ICallMessageContext mc = mca.getMessageContext();
		String methodName = mc.getMethodName();
		final String requestId = mc.getRequestId();
		if (methodName != null || requestId != null) {
			if (requestId != null) {
				methodName = methodName + "&" + requestId; //$NON-NLS-1$
			}
			connection = super.openConnection(new URL(url.toString() + "?" + methodName)); //$NON-NLS-1$
		} else {
			connection = super.openConnection(url);
		}
		if (connectTimeout > 0) {
			connection.setConnectTimeout(connectTimeout);
		}
		final Map<String, List<String>> headers = mc.listRequestHeaders();
		if (headers != null) {
			for (final Map.Entry<String, List<String>> entry : headers.entrySet()) {
				// System.out.println("size:" + headers.get(hName).size());
				for (final String hValue : entry.getValue()) {
					connection.addRequestProperty(entry.getKey(), hValue);
					// System.out.println(">>>" + hName + ":" + hValue);
				}
			}
		}
		if (isZipClientRequest) {
			connection.addRequestProperty("Content-Encoding", "x-hessian-gzip"); //$NON-NLS-1$//$NON-NLS-2$
			connection = new GZippingHttpURLConnectionWrapper((HttpURLConnection) connection);
		}
		CONNECTIONS.set((HttpURLConnection) connection);
		return connection;
	}

	@Override
	public AbstractHessianInput getHessianInput(final InputStream is) {
		final ICallMessageContext messageContext = mca.getMessageContext();

		if (messageContext.getProgressMonitorList() == null) {
			return super.getHessianInput(is);
		} else {
			return super.getHessianInput(new InputStream() {

				@Override
				public int read() throws IOException {
					final int b = is.read();
					if (b != -1) {
						messageContext.fireReadEvent(1);
					}
					return b;
				}

			});
		}
	}

	@Override
	public AbstractHessianOutput getHessianOutput(final OutputStream os) {
		final ICallMessageContext messageContext = mca.getMessageContext();

		if (messageContext.getProgressMonitorList() == null) {
			return getHessianOutputImpl(os, os);
		} else {
			return getHessianOutputImpl(os, new OutputStream() {

				@Override
				public void write(final int b) throws IOException {
					os.write(b);
					messageContext.fireWriteEvent(1);
				}
			});
		}
	}

	public void setCallMessageContextAccessor(final ICallMessageContextAccessor mca) {
		this.mca = mca;
	}

	protected static HttpURLConnection getHttpURLConnection() {
		return CONNECTIONS.get();
	}

	/**
	 * @param originalOutputStream
	 *            stream where the acual data is written into (needed for finish
	 *            call on GZIP)
	 * @param outputStreamData
	 *            stream into which all data is written into....
	 * @return
	 */
	private AbstractHessianOutput getHessianOutputImpl(final OutputStream originalOutputStream,
			final OutputStream outputStreamData) {
		AbstractHessianOutput out;

		if (/* _isHessian2Request */true) {
			out = new Hessian2Output(outputStreamData) {

				@Override
				public void completeCall() throws IOException {
					super.completeCall();
					this.flush();
					final HttpURLConnection urlConnection = CONNECTIONS.get();
					if (urlConnection instanceof GZippingHttpURLConnectionWrapper) {
						final GZIPOutputStream gzipOut = ((GZippingHttpURLConnectionWrapper) urlConnection)
								.getUsedGZIPOutputStream();
						gzipOut.finish();
					}
					outputStreamData.flush();
				}
			};
			//		else {
			//			HessianOutput out1 = new HessianOutput(outputStreamParameter);
			//			out = out1;
			//
			//			if (/* _isHessian2Reply */true)
			//				out1.setVersion(2);
			//		}
		}

		out.setSerializerFactory(getSerializerFactory());

		return out;
	}

}
