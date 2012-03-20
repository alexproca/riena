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
package org.eclipse.riena.ui.ridgets.swt;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.TypedListener;

/**
 * This helper class supports the {@link MenuManger}.
 * 
 * 
 */

// FIXME MenuManagerHelper is a utility-class and should be final with private constructor and static methods
public class MenuManagerHelper {

	/**
	 * Creates with the help of the given menu manager a menu. If the given tool
	 * item is selected, the menu is shown.
	 * 
	 * @param parent
	 * @param toolItem
	 *            tool item with menu
	 * @param topMenuManager
	 *            menu manager
	 * @return menu
	 */
	public Menu createMenu(final Composite parent, final ToolItem toolItem, final MenuManager topMenuManager) {

		final Menu menu = topMenuManager.createContextMenu(parent);
		toolItem.setData(topMenuManager);
		topMenuManager.updateAll(true);
		addListeners(toolItem, menu);

		return menu;

	}

	public void addListeners(final ToolItem toolItem, final Menu menu) {

		menu.addMenuListener(new TopMenuListener(menu, toolItem));
		toolItem.addSelectionListener(new TopItemListener(menu, toolItem));
	}

	public void removeListeners(final ToolItem toolItem, final Menu menu) {

		Listener[] listeners = menu.getListeners(SWT.Hide);
		for (final Listener listener : listeners) {
			if (listener instanceof TypedListener) {
				final TypedListener typedListener = (TypedListener) listener;
				if (typedListener.getEventListener() instanceof TopMenuListener) {
					menu.removeMenuListener((TopMenuListener) typedListener.getEventListener());
				}
			}
		}
		listeners = toolItem.getListeners(SWT.Selection);
		for (final Listener listener : listeners) {
			if (listener instanceof TypedListener) {
				final TypedListener typedListener = (TypedListener) listener;
				if (typedListener.getEventListener() instanceof TopItemListener) {
					toolItem.removeSelectionListener((TopItemListener) typedListener.getEventListener());
				}
			}
		}

	}

	private static class TopMenuListener implements MenuListener {

		private final ToolItem toolItem;
		private final Menu menu;

		public TopMenuListener(final Menu menu, final ToolItem toolItem) {
			this.menu = menu;
			this.toolItem = toolItem;
		}

		public void menuHidden(final MenuEvent e) {
			if (e.getSource() == menu && !toolItem.isDisposed()) {
				toolItem.setSelection(false);
			}
		}

		public void menuShown(final MenuEvent e) {
		}

	}

	private static class TopItemListener implements SelectionListener {

		private final ToolItem toolItem;
		private final Menu menu;

		public TopItemListener(final Menu menu, final ToolItem toolItem) {
			this.menu = menu;
			this.toolItem = toolItem;
		}

		/**
		 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		public void widgetDefaultSelected(final SelectionEvent e) {
		}

		/**
		 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		public void widgetSelected(final SelectionEvent e) {
			if (e.getSource() == toolItem) {
				final Rectangle itemBounds = toolItem.getBounds();
				final Point loc = toolItem.getParent().toDisplay(itemBounds.x, itemBounds.height + itemBounds.y);
				menu.setLocation(loc);
				menu.setVisible(true);
			}
		}

	}

}
