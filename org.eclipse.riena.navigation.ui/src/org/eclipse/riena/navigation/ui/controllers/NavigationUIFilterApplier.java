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
package org.eclipse.riena.navigation.ui.controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.riena.internal.navigation.ui.filter.AbstractUIFilterRuleMenuItemMarker;
import org.eclipse.riena.navigation.IApplicationNode;
import org.eclipse.riena.navigation.INavigationNode;
import org.eclipse.riena.navigation.INavigationNodeController;
import org.eclipse.riena.navigation.ISubApplicationNode;
import org.eclipse.riena.navigation.listener.NavigationNodeListener;
import org.eclipse.riena.ui.filter.IUIFilter;
import org.eclipse.riena.ui.filter.IUIFilterRule;
import org.eclipse.riena.ui.ridgets.IComplexRidget;
import org.eclipse.riena.ui.ridgets.IMenuItemRidget;
import org.eclipse.riena.ui.ridgets.IRidget;
import org.eclipse.riena.ui.ridgets.IRidgetContainer;
import org.eclipse.riena.ui.ridgets.IToolItemRidget;

/**
 * This class applies the UI filters after a filter was added or removed; also
 * this controller was activated.
 * 
 * @param <N>
 *            type of the navigation node
 */
@SuppressWarnings("unchecked")
public class NavigationUIFilterApplier<N> extends NavigationNodeListener {

	private final static IUIFilterRuleClosure APPLY_CLOSURE = new ApplyClosure();
	private final static IUIFilterRuleClosure REMOVE_CLOSURE = new RemoveClosure();

	/**
	 * Applies all the filters of the given node (and all filters of the parent
	 * nodes) to the given node.
	 * 
	 * @param node
	 *            navigation node
	 */
	private void applyFilters(final INavigationNode<?> node) {

		if (node == null) {
			return;
		}

		final Collection<IUIFilter> filters = new ArrayList<IUIFilter>();
		collectFilters(node, filters);

		for (final IUIFilter filter : filters) {
			applyFilter(node, filter, APPLY_CLOSURE);
		}

		final ISubApplicationNode subAppNode = node.getParentOfType(ISubApplicationNode.class);
		if ((subAppNode != null) && (subAppNode != node)) {
			for (final IUIFilter filter : filters) {
				applyFilter(subAppNode, filter, APPLY_CLOSURE);
			}
		}

	}

	/**
	 * Removes all rules for menu and tool items form the given node and also
	 * its child nodes.<br>
	 * This this necessary because every sub-application has its "own" menu and
	 * tool bar: same widget but different ridgets.
	 * 
	 * @param node
	 *            navigation node
	 */
	private void removeAllMenuItemRules(final INavigationNode<?> node) {

		if (node == null) {
			return;
		}

		for (final IUIFilter filter : node.getFilters()) {
			for (final IUIFilterRule rule : filter.getFilterRules()) {
				if (rule instanceof AbstractUIFilterRuleMenuItemMarker) {
					applyFilterRule(node, rule, REMOVE_CLOSURE);
				}
			}
		}

		final List<?> children = node.getChildren();
		for (final Object child : children) {
			if (child instanceof INavigationNode<?>) {
				removeAllMenuItemRules((INavigationNode<?>) child);
			}
		}

	}

	/**
	 * Adds the filters of the given node and of the parents nodes to the given
	 * collection of filters.
	 * 
	 * @param node
	 *            navigation node
	 * @param filters
	 *            collection of UI filters.
	 */
	private void collectFilters(final INavigationNode<?> node, final Collection<IUIFilter> filters) {

		if (node == null) {
			return;
		}

		if (node.getFilters() != null) {
			filters.addAll(node.getFilters());
		}
		collectFilters(node.getParent(), filters);

	}

	/**
	 * Executes the closure for the given filter to the given node and all of
	 * its child nodes.
	 * 
	 * @param node
	 *            navigation node
	 * @param filter
	 *            UI filter
	 * @param closure
	 *            closure to execute
	 */
	private void applyFilter(final INavigationNode<?> node, final IUIFilter filter, final IUIFilterRuleClosure closure) {

		final Collection<? extends IUIFilterRule> rules = filter.getFilterRules();
		for (final IUIFilterRule rule : rules) {
			applyFilterRule(node, rule, closure);
		}

		final List<?> children = node.getChildren();
		for (final Object child : children) {
			if (child instanceof INavigationNode<?>) {
				applyFilter((INavigationNode<?>) child, filter, closure);
			}
		}

	}

	/**
	 * Executes the closure for the given filter rule to the given node and all
	 * the ridgets.
	 * 
	 * @param node
	 *            navigation node
	 * @param filterRule
	 *            filter rule
	 * @param closure
	 *            closure to execute
	 */
	private void applyFilterRule(final INavigationNode<?> node, final IUIFilterRule filterRule,
			final IUIFilterRuleClosure closure) {

		if (filterRule.matches(node)) {
			closure.execute(node, filterRule, node);
		}

		final INavigationNodeController controller = node.getNavigationNodeController();
		if (controller instanceof IRidgetContainer) {
			final IRidgetContainer container = (IRidgetContainer) controller;
			executeFilter(node, filterRule, closure, container.getRidgets());
		}

	}

	private void executeFilter(final INavigationNode<?> node, final IUIFilterRule filterRule,
			final IUIFilterRuleClosure closure, final Collection<? extends IRidget> ridgets) {
		for (final IRidget ridget : ridgets) {
			if (ridget instanceof IComplexRidget) {
				executeFilter(node, filterRule, closure, ((IComplexRidget) ridget).getRidgets());
			}
			if (filterRule.matches(ridget, node)) {
				closure.execute(node, filterRule, ridget);
			}
		}
	}

	@Override
	public void afterActivated(final INavigationNode source) {
		super.afterActivated(source);
		applyFilters(source);
	}

	@Override
	public void beforeDeactivated(final INavigationNode source) {
		super.beforeDeactivated(source);
		final IApplicationNode appNode = (IApplicationNode) source.getParentOfType(IApplicationNode.class);
		removeAllMenuItemRules(appNode);
	}

	@Override
	public void filterAdded(final INavigationNode source, final IUIFilter filter) {
		super.filterAdded(source, filter);
		applyFilter(source, filter, APPLY_CLOSURE);
	}

	@Override
	public void filterRemoved(final INavigationNode source, final IUIFilter filter) {
		super.filterRemoved(source, filter);
		applyFilter(source, filter, REMOVE_CLOSURE);
	}

	/**
	 * Closure to execute the {@code apply} method of {@link IUIFilterRule} .
	 */
	private static class ApplyClosure implements IUIFilterRuleClosure {

		public void execute(final INavigationNode<?> node, final IUIFilterRule attr, final Object obj) {
			if (obj instanceof IRidget) {
				if (isMenuItemOfDeactivatedNode(node, (IRidget) obj)) {
					return;
				}
			}
			attr.apply(obj);
		}

	}

	/**
	 * Closure to execute the {@code remove} method of {@link IUIFilterRule}.
	 */
	private static class RemoveClosure implements IUIFilterRuleClosure {

		public void execute(final INavigationNode<?> node, final IUIFilterRule attr, final Object obj) {
			if (obj instanceof IRidget) {
				if (isMenuItemOfDeactivatedNode(node, (IRidget) obj)) {
					return;
				}
			}
			attr.remove(obj);
		}

	}

	/**
	 * Tests if the given ridget is a menu or tool item and belongs to a
	 * deactivated node.<br>
	 * This this necessary because every sub-application has its "own" menu and
	 * tool bar.
	 * 
	 * @param node
	 *            navigation node to which the ridget belongs.
	 * @param ridget
	 * @return {@code true} if ridget is a menu item and the node is
	 *         deactivated; otherwise {@code false}
	 */
	private static boolean isMenuItemOfDeactivatedNode(final INavigationNode<?> node, final IRidget ridget) {

		if (!(node instanceof IApplicationNode)) {
			if ((ridget instanceof IToolItemRidget) || (ridget instanceof IMenuItemRidget)) {
				return node.isDeactivated();
			}
		}

		return false;

	}

}