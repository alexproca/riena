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
package org.eclipse.riena.navigation.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.riena.core.util.VariableManagerUtil;
import org.eclipse.riena.navigation.IGenericNavigationAssembler;
import org.eclipse.riena.navigation.IModuleGroupNode;
import org.eclipse.riena.navigation.IModuleGroupNodeExtension;
import org.eclipse.riena.navigation.IModuleNode;
import org.eclipse.riena.navigation.IModuleNodeExtension;
import org.eclipse.riena.navigation.INavigationAssemblyExtension;
import org.eclipse.riena.navigation.INavigationNode;
import org.eclipse.riena.navigation.ISubApplicationNode;
import org.eclipse.riena.navigation.ISubApplicationNodeExtension;
import org.eclipse.riena.navigation.ISubModuleNode;
import org.eclipse.riena.navigation.ISubModuleNodeExtension;
import org.eclipse.riena.navigation.NavigationArgument;
import org.eclipse.riena.navigation.NavigationNodeId;

/**
 * The GenericNavigationAssembler can handle the hierarchical definition of
 * navigation assemblies as defined by the extension point
 * 'org.eclipse.riena.navigation.assemblies'. Variables used for labels of
 * modules and submodules would be substituted. There are some predefined
 * variables:
 * <ul>
 * <li>riena.navigation.nodeid
 * <li>riena.navigation.paramenter
 * </ul>
 * These variables reference the NavigationNodeId and
 * NavigationArgument.getInputParameter() objects, reps. The user is required to
 * provide a parameter pointing to the desired property. For example to access
 * the instanceId property of the current NavigationNodeId one would write:
 * 
 * <pre>
 * ${riena.navigation.nodeid:instanceId}
 * </pre>
 */
public class GenericNavigationAssembler implements IGenericNavigationAssembler {

	/** dynamic variable referencing navigation node id */
	static public final String VAR_NAVIGATION_NODEID = "riena.navigation.nodeid"; //$NON-NLS-1$

	/** dynamic variable referencing navigation parameter */
	static public final String VAR_NAVIGATION_PARAMETER = "riena.navigation.paramenter"; //$NON-NLS-1$

	// the node definition as read from extension point
	private INavigationAssemblyExtension assembly;

	/**
	 * @see org.eclipse.riena.navigation.IGenericNavigationAssembler#getAssembly()
	 */
	public INavigationAssemblyExtension getAssembly() {
		return assembly;
	}

	/**
	 * @see org.eclipse.riena.navigation.IGenericNavigationAssembler#setAssembly(org.eclipse.riena.navigation.INavigationAssemblyExtension)
	 */
	public void setAssembly(INavigationAssemblyExtension nodeDefinition) {
		this.assembly = nodeDefinition;
	}

	/**
	 * @see org.eclipse.riena.navigation.INavigationAssembler#buildNode(org.eclipse.riena.navigation.NavigationNodeId,
	 *      org.eclipse.riena.navigation.NavigationArgument)
	 */
	public INavigationNode<?> buildNode(NavigationNodeId targetId, NavigationArgument navigationArgument) {

		if (assembly != null) {
			// build module group if it exists
			ISubApplicationNodeExtension subapplicationDefinition = assembly.getSubApplicationNode();
			if (subapplicationDefinition != null) {
				return build(subapplicationDefinition, targetId, navigationArgument);
			}
			// build module group if it exists
			IModuleGroupNodeExtension groupDefinition = assembly.getModuleGroupNode();
			if (groupDefinition != null) {
				return build(groupDefinition, targetId, navigationArgument);
			}
			// otherwise try module
			IModuleNodeExtension moduleDefinition = assembly.getModuleNode();
			if (moduleDefinition != null) {
				return build(moduleDefinition, targetId, navigationArgument);
			}
			// last resort is submodule
			ISubModuleNodeExtension submoduleDefinition = assembly.getSubModuleNode();
			if (submoduleDefinition != null) {
				return build(submoduleDefinition, targetId, navigationArgument);
			}
		}

		throw new ExtensionPointFailure(
				"'subapplication', 'modulegroup', 'module' or 'submodule' element expected. ID=" + targetId.getTypeId()); //$NON-NLS-1$
	}

	protected ISubApplicationNode build(ISubApplicationNodeExtension subapplicationDefinition,
			NavigationNodeId targetId, NavigationArgument navigationArgument) {

		// a module group can only contain modules
		ISubApplicationNode subapplication = new SubApplicationNode(createNavigationNodeIdFromTemplate(targetId,
				subapplicationDefinition.getTypeId(), navigationArgument));
		for (IModuleGroupNodeExtension modulegroupDefinition : subapplicationDefinition.getModuleGroupNodes()) {
			subapplication.addChild(build(modulegroupDefinition, targetId, navigationArgument));
		}

		return subapplication;
	}

	protected IModuleGroupNode build(IModuleGroupNodeExtension groupDefinition, NavigationNodeId targetId,
			NavigationArgument navigationArgument) {

		// a module group can only contain modules
		IModuleGroupNode moduleGroup = new ModuleGroupNode(createNavigationNodeIdFromTemplate(targetId, groupDefinition
				.getTypeId(), navigationArgument));
		for (IModuleNodeExtension moduleDefinition : groupDefinition.getModuleNodes()) {
			moduleGroup.addChild(build(moduleDefinition, targetId, navigationArgument));
		}

		return moduleGroup;
	}

	protected IModuleNode build(IModuleNodeExtension moduleDefinition, NavigationNodeId targetId,
			NavigationArgument navigationArgument) {

		IModuleNode module = null;
		Map<String, Object> mapping = createMapping(targetId, navigationArgument);
		try {
			startVariableResolver(mapping);
			// create module node with label (and icon)
			module = new ModuleNode(createNavigationNodeIdFromTemplate(targetId, moduleDefinition.getTypeId(),
					navigationArgument), resolveVariables(moduleDefinition.getLabel()));
			module.setIcon(moduleDefinition.getIcon());
			// TODO we cannot set visibility state now
			// TODO node MUST be registered first
			//module.setVisible(!moduleDefinition.isHidden());
			module.setCloseable(!moduleDefinition.isUncloseable());
			// ...and may contain submodules
			for (ISubModuleNodeExtension submoduleDefinition : moduleDefinition.getSubModuleNodes()) {
				module.addChild(build(submoduleDefinition, targetId, navigationArgument));
			}
		} finally {
			cleanupVariableResolver();
		}

		return module;
	}

	protected ISubModuleNode build(ISubModuleNodeExtension submoduleDefinition, NavigationNodeId targetId,
			NavigationArgument navigationArgument) {

		ISubModuleNode submodule = null;
		Map<String, Object> mapping = createMapping(targetId, navigationArgument);
		try {
			startVariableResolver(mapping);
			// create submodule node with label (and icon)
			submodule = new SubModuleNode(createNavigationNodeIdFromTemplate(targetId, submoduleDefinition.getTypeId(),
					navigationArgument), resolveVariables(submoduleDefinition.getLabel()));
			submodule.setIcon(submoduleDefinition.getIcon());
			// TODO we cannot set visibility state now
			// TODO node MUST be registered first
			//submodule.setVisible(!submoduleDefinition.isHidden());

			// process nested submodules
			for (ISubModuleNodeExtension nestedSubmoduleDefinition : submoduleDefinition.getSubModuleNodes()) {
				submodule.addChild(build(nestedSubmoduleDefinition, targetId, navigationArgument));
			}
		} finally {
			cleanupVariableResolver();
		}

		return submodule;
	}

	protected NavigationNodeId createNavigationNodeIdFromTemplate(NavigationNodeId template, String typeId,
			NavigationArgument navigationArgument) {

		return new NavigationNodeId(typeId, template.getInstanceId());
	}

	protected String resolveVariables(String string) {

		try {
			return VariableManagerUtil.substitute(string);
		} catch (CoreException ex) {
			ex.printStackTrace();
			return string;
		}
	}

	protected Map<String, Object> createMapping(NavigationNodeId targetId, NavigationArgument navigationArgument) {

		Map<String, Object> mapping = new HashMap<String, Object>();
		mapping.put(VAR_NAVIGATION_NODEID, targetId);
		if (navigationArgument != null) {
			mapping.put(VAR_NAVIGATION_PARAMETER, navigationArgument.getInputParameter());
		}

		return mapping;
	}

	protected void startVariableResolver(Map<String, Object> mapping) {
		ThreadLocalMapResolver.configure(mapping);
	}

	protected void cleanupVariableResolver() {
		ThreadLocalMapResolver.cleanup();
	}
}
