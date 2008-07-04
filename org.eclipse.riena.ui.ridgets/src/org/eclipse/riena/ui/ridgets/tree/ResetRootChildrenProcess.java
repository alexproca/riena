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
package org.eclipse.riena.ui.ridgets.tree;

import org.eclipse.riena.ui.core.uiprocess.UIProcess;

/**
 * Resets the children of the root element of a dynamic tree. All children will
 * be replaced with a dynamic tree node with a placeholder user element.
 */
class ResetRootChildrenProcess extends DynamicTreeModificationProcess {

	private final DynamicTreeNode root;

	/**
	 * Constructor requires extension point.
	 * 
	 * @see UIProcess
	 * @param treeModel
	 *            The tree model.
	 */
	ResetRootChildrenProcess(final DynamicLoadTreeModel treeModel) {
		super(treeModel, ResetRootChildrenProcess.class.getSimpleName());
		this.root = (DynamicTreeNode) treeModel.getRoot();
	}

	/**
	 * Removes all children of the root, resets the roots child iterator an adds
	 * a placeholder node that will trigger the new loading of the children when
	 * accessed.
	 * 
	 * @see de.compeople.spirit.core.client.uibinding.adapter.tree.DynamicTreeModificationProcess#modifyTree()
	 */
	@Override
	protected void modifyTree() {
		for (int index = root.getChildCount() - 1; index >= 0; index--) {
			getTreeModel().removeNodeFromParent((DefaultTreeNode) root.getChildAt(index));
		}
		root.resetChildIterator();
		final DynamicTreeNode initialChildWithPlaceholder = root.createChildNode();
		initialChildWithPlaceholder.setUserObject(root.createPlaceholderUserElement());
		getTreeModel().addNode(initialChildWithPlaceholder, root);
	}

	/**
	 * @see de.compeople.spirit.core.client.uibinding.adapter.tree.DynamicTreeModificationProcess#isResettingTree()
	 */
	@Override
	boolean isResettingTree() {
		return true;
	}

	/**
	 * @see de.compeople.spirit.core.client.uibinding.adapter.tree.DynamicTreeModificationProcess#getModifiedNode()
	 */
	@Override
	protected DynamicTreeNode getModifiedNode() {
		return root;
	}

}
