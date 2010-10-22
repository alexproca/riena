/*******************************************************************************
 * Copyright (c) 2007, 2010 compeople AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    compeople AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.riena.navigation.ui.swt.views;

import java.util.Collection;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import org.eclipse.riena.core.marker.AbstractMarker;
import org.eclipse.riena.core.marker.IMarker;
import org.eclipse.riena.core.util.ReflectionUtils;
import org.eclipse.riena.internal.core.test.RienaTestCase;
import org.eclipse.riena.internal.core.test.collect.UITestCase;
import org.eclipse.riena.navigation.NavigationNodeId;
import org.eclipse.riena.navigation.model.ModuleGroupNode;
import org.eclipse.riena.navigation.model.ModuleNode;
import org.eclipse.riena.navigation.model.NavigationProcessor;
import org.eclipse.riena.navigation.model.SubModuleNode;
import org.eclipse.riena.navigation.ui.swt.lnf.renderer.SubModuleTreeItemMarkerRenderer;
import org.eclipse.riena.ui.core.marker.AttentionMarker;
import org.eclipse.riena.ui.core.marker.ErrorMarker;
import org.eclipse.riena.ui.core.marker.MandatoryMarker;
import org.eclipse.riena.ui.ridgets.swt.DefaultRealm;
import org.eclipse.riena.ui.swt.EmbeddedTitleBar;
import org.eclipse.riena.ui.swt.lnf.ILnfRenderer;
import org.eclipse.riena.ui.swt.lnf.LnfKeyConstants;
import org.eclipse.riena.ui.swt.lnf.LnfManager;
import org.eclipse.riena.ui.swt.lnf.rienadefault.RienaDefaultLnf;
import org.eclipse.riena.ui.swt.utils.SwtUtilities;

/**
 * Tests for the ModuleView.
 */
@UITestCase
public class ModuleViewTest extends RienaTestCase {

	private MyModuleView view;
	private ModuleNode node;
	private SubModuleNode subNode;
	private SubModuleNode subSubNode;
	private SubModuleNode subSubSubNode;
	private Shell shell;
	private RienaDefaultLnf currentLnf;
	private DisabledSubModuleTreeBackgroundPainterMock backgroundPainterMock;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		new DefaultRealm();
		backgroundPainterMock = new DisabledSubModuleTreeBackgroundPainterMock();
		currentLnf = new RienaDefaultLnf();
		LnfManager.setLnf(currentLnf);

		shell = new Shell();
		final NavigationProcessor navigationProcessor = new NavigationProcessor();
		node = new ModuleNode();
		node.setNavigationNodeController(new SWTModuleController(node));
		view = new MyModuleView(shell);
		final ModuleGroupNode moduleGroupNode = new ModuleGroupNode();
		moduleGroupNode.addChild(node);
		view.setModuleGroupNode(moduleGroupNode);
		node.setNavigationProcessor(navigationProcessor);
		subNode = new SubModuleNode();
		subNode.setNavigationProcessor(navigationProcessor);
		node.addChild(subNode);
		subSubNode = new SubModuleNode();
		subSubNode.setNavigationProcessor(navigationProcessor);
		subNode.addChild(subSubNode);
		subSubSubNode = new SubModuleNode();
		subSubSubNode.setNavigationProcessor(navigationProcessor);
		subSubNode.addChild(subSubSubNode);
		view.bind(node);
	}

	@Override
	protected void tearDown() throws Exception {
		view.dispose();
		SwtUtilities.dispose(shell);
		node = null;

		super.tearDown();
	}

	/**
	 * Test for bug 269221
	 */
	public void testSetActivatedSubModuleExpanded() throws Exception {
		subNode.activate();

		assertTrue(node.isActivated());
		assertTrue(subNode.isActivated());
		assertFalse(subSubNode.isActivated());
		assertFalse(subSubSubNode.isActivated());
		assertFalse(subNode.isExpanded());
		assertFalse(subSubNode.isExpanded());

		subSubSubNode.activate();

		assertTrue(node.isActivated());
		assertFalse(subNode.isActivated());
		assertFalse(subSubNode.isActivated());
		assertTrue(subSubSubNode.isActivated());
		assertTrue(subNode.isExpanded());
		assertTrue(subSubNode.isExpanded());
	}

	public void testBlocking() {
		final EmbeddedTitleBar title = ReflectionUtils.invokeHidden(view, "getTitle");
		final Composite body = ReflectionUtils.invokeHidden(view, "getBody");
		final Tree tree = ReflectionUtils.invokeHidden(view, "getTree");
		final Cursor waitCursor = title.getDisplay().getSystemCursor(SWT.CURSOR_WAIT);

		node.setBlocked(true);

		final Event event = new Event();
		event.type = SWT.EraseItem;
		event.widget = view.getTree();
		view.getTree().notifyListeners(SWT.EraseItem, event);
		assertTrue(backgroundPainterMock.handleEraseCalled);

		assertSame(waitCursor, title.getCursor());
		assertSame(waitCursor, body.getCursor());
		assertFalse(title.isCloseable());
		assertFalse(tree.getEnabled());
		
		node.setBlocked(false);

		assertNotSame(waitCursor, title.getCursor());
		assertTrue(title.isCloseable());
		assertNotSame(waitCursor, body.getCursor());
		assertTrue(tree.getEnabled());
	}

	/**
	 * Tests the <i>private</i> method {@code getAllMarkers(ISubModuleNode,
	 * boolean)}.
	 */
	public void testGetAllMarkers() {

		final IMarker em1 = new ErrorMarker();
		subNode.addMarker(em1);
		final IMarker mm2 = new MandatoryMarker();
		subSubNode.addMarker(mm2);
		final IMarker am3 = new AttentionMarker();
		subSubSubNode.addMarker(am3);
		// This marker will be never returned, because it does not implement IIconizableMarker
		final IMarker mym4 = new MyMarker();
		subSubSubNode.addMarker(mym4);

		Collection<? extends IMarker> markers = ReflectionUtils.invokeHidden(view, "getAllMarkers", subNode, false);
		assertEquals(1, markers.size());
		assertTrue(markers.contains(em1));

		markers = ReflectionUtils.invokeHidden(view, "getAllMarkers", subNode, true);
		assertEquals(3, markers.size());
		assertTrue(markers.contains(em1));
		assertTrue(markers.contains(mm2));
		assertTrue(markers.contains(am3));
		assertFalse(markers.contains(mym4));

		markers = ReflectionUtils.invokeHidden(view, "getAllMarkers", subSubNode, true);
		assertEquals(2, markers.size());
		assertFalse(markers.contains(em1));
		assertTrue(markers.contains(mm2));
		assertTrue(markers.contains(am3));
		assertFalse(markers.contains(mym4));

		// Markers of hidden nodes (and their child nodes) will not be returned
		subSubSubNode.setVisible(false);
		markers = ReflectionUtils.invokeHidden(view, "getAllMarkers", subNode, true);
		assertEquals(2, markers.size());
		assertTrue(markers.contains(em1));
		assertTrue(markers.contains(mm2));
		assertFalse(markers.contains(am3));
		assertFalse(markers.contains(mym4));

		subSubNode.setVisible(false);
		subSubSubNode.setVisible(true);
		markers = ReflectionUtils.invokeHidden(view, "getAllMarkers", subNode, true);
		assertEquals(1, markers.size());
		assertTrue(markers.contains(em1));
		assertFalse(markers.contains(mm2));
		assertFalse(markers.contains(am3));
		assertFalse(markers.contains(mym4));

		// Markers of disabled will not be returned
		subSubNode.setVisible(true);
		subSubSubNode.setEnabled(false);
		markers = ReflectionUtils.invokeHidden(view, "getAllMarkers", subNode, true);
		assertEquals(3, markers.size());
		assertTrue(markers.contains(em1));
		assertTrue(markers.contains(mm2));
		assertTrue(markers.contains(am3));
		assertFalse(markers.contains(mym4));

	}

	/**
	 * Tests the <i>private</i> method {@code paintTreeItem(Event)}.
	 */
	public void testPaintTreeItem() {

		final SubModuleTreeItemMarkerRenderer renderer = new SubModuleTreeItemMarkerRenderer();
		final Map<String, ILnfRenderer> rendererTable = ReflectionUtils.getHidden(currentLnf, "rendererTable");
		rendererTable.put(LnfKeyConstants.SUB_MODULE_TREE_ITEM_MARKER_RENDERER, renderer);

		final IMarker em1 = new ErrorMarker();
		subNode.addMarker(em1);
		final IMarker mm2 = new MandatoryMarker();
		subSubNode.addMarker(mm2);
		final IMarker am3 = new AttentionMarker();
		subSubSubNode.addMarker(am3);

		final Tree tree = view.getTree();
		final TreeItem item = new TreeItem(tree, SWT.NONE);
		item.setData(subNode);
		final TreeItem item2 = new TreeItem(item, SWT.NONE);
		item2.setData(subSubNode);
		final TreeItem item3 = new TreeItem(item2, SWT.NONE);
		item3.setData(subSubSubNode);

		final Event event = new Event();
		event.item = item;
		event.x = 11;
		event.y = 22;
		event.width = 123;
		event.height = 456;
		event.gc = new GC(tree);

		ReflectionUtils.invokeHidden(view, "paintTreeItem", event);
		Collection<? extends IMarker> markers = renderer.getMarkers();
		assertEquals(3, markers.size());
		assertTrue(markers.contains(em1));
		assertTrue(markers.contains(mm2));
		assertTrue(markers.contains(am3));
		assertEquals(event.x, renderer.getBounds().x);
		assertEquals(event.y, renderer.getBounds().y);
		assertEquals(event.width, renderer.getBounds().width);
		assertEquals(event.height, renderer.getBounds().height);

		item.setExpanded(true);
		ReflectionUtils.invokeHidden(view, "paintTreeItem", event);
		markers = renderer.getMarkers();
		assertEquals(1, markers.size());
		assertTrue(markers.contains(em1));
		assertFalse(markers.contains(mm2));
		assertFalse(markers.contains(am3));

		event.gc.dispose();

	}

	private class DisabledSubModuleTreeBackgroundPainterMock implements Listener {

		boolean handleEraseCalled = false;

		public void handleEvent(final Event event) {
			if (SWT.EraseItem == event.type) {
				handleEraseCalled = true;
			}
		}
	}

	/**
	 * This ModuleView makes the visibility of the method {@code getTree()}
	 * public for testing.
	 */
	private class MyModuleView extends ModuleView {

		public MyModuleView(final Composite parent) {
			super(parent);
		}

		@Override
		public Tree getTree() {
			return super.getTree();
		}

		@Override
		protected Listener createDisabledSubModuleTreeBackgroundPainter(final Color disabledBackgroundColor) {
			return backgroundPainterMock;
		}

	}

	/**
	 * Marker that not implements {@code IIconizableMarker}.
	 */
	private class MyMarker extends AbstractMarker {
	}

}
