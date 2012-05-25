/*******************************************************************************
 * Copyright (c) 2007, 2011 compeople AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    compeople AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.riena.navigation.ui.swt.e4.application;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.osgi.service.log.LogService;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.core.commands.contexts.ContextManagerEvent;
import org.eclipse.core.commands.contexts.IContextManagerListener;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.PerspectiveStackImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.EContextService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.equinox.log.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.internal.StartupThreading;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.commands.CommandService;
import org.eclipse.ui.internal.contexts.ContextService;
import org.eclipse.ui.internal.misc.Policy;
import org.eclipse.ui.internal.progress.ProgressManager;
import org.eclipse.ui.internal.services.IServiceLocatorCreator;
import org.eclipse.ui.internal.services.ServiceLocator;
import org.eclipse.ui.internal.services.ServiceLocatorCreator;
import org.eclipse.ui.services.IDisposable;

import org.eclipse.riena.core.Log4r;
import org.eclipse.riena.core.service.Service;
import org.eclipse.riena.core.util.RAPDetector;
import org.eclipse.riena.internal.navigation.ui.filter.IUIFilterApplier;
import org.eclipse.riena.internal.navigation.ui.swt.Activator;
import org.eclipse.riena.navigation.ApplicationNodeManager;
import org.eclipse.riena.navigation.IApplicationNode;
import org.eclipse.riena.navigation.IModuleGroupNode;
import org.eclipse.riena.navigation.IModuleNode;
import org.eclipse.riena.navigation.INavigationNode;
import org.eclipse.riena.navigation.INavigationNodeProvider;
import org.eclipse.riena.navigation.ISubApplicationNode;
import org.eclipse.riena.navigation.NavigationNodeId;
import org.eclipse.riena.navigation.StartupNodeInfo;
import org.eclipse.riena.navigation.listener.NavigationTreeObserver;
import org.eclipse.riena.navigation.model.ApplicationNode;
import org.eclipse.riena.navigation.model.NavigationNodeProvider;
import org.eclipse.riena.navigation.ui.application.ProgressVisualizerLocator;
import org.eclipse.riena.navigation.ui.controllers.ApplicationController;
import org.eclipse.riena.ui.core.uiprocess.ProgressProviderBridge;
import org.eclipse.riena.ui.swt.uiprocess.SwtUISynchronizer;

@SuppressWarnings("restriction")
public class RienaE4Application extends E4Application implements EventHandler {
	private final static Logger LOGGER = Log4r.getLogger(Activator.getDefault(), RienaE4Application.class);
	private ApplicationController controller;
	private IApplicationNode applicationNode;
	private MApplication application;
	private ServiceLocator serviceLocator;
	private CommandManager commandManager;
	private ContextManager contextManager;
	private final Map<String, MBindingContext> bindingContexts = new HashMap<String, MBindingContext>();

	@Inject
	private EPartService ePartService;

	@Inject
	private IEventBroker eventBroker;
	private IApplicationNode applicationModel;

	@Override
	public Object start(final IApplicationContext applicationContext) throws Exception {

		// set the display name before the Display is
		// created to ensure the app name is used in any
		// platform menus, etc. See
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=329456#c14
		final IProduct product = Platform.getProduct();
		if (product.getName() != null) {
			Display.setAppName(product.getName());
		}

		final Display display = getApplicationDisplay();

		preStart();

		final E4Workbench workbench = createE4Workbench(applicationContext, display);
		application = workbench.getApplication();

		applyToWorkbenchModel(applicationNode, workbench.getContext().get(MApplication.class), workbench.getContext());

		final Location instanceLocation = (Location) workbench.getContext().get(E4Workbench.INSTANCE_LOCATION);
		try {
			final IEclipseContext e4Context = workbench.getContext();
			e4Context.set(Display.class, display);

			e4Context.set(IWorkbench.class, new DummyWorkbench());
			e4Context.set(IApplicationNode.class, applicationNode);
			e4Context
					.set(E4Workbench.PRESENTATION_URI_ARG,
							"bundleclass://org.eclipse.riena.navigation.ui.swt.e4/org.eclipse.riena.navigation.ui.swt.e4.RienaPartRenderingEngine"); //$NON-NLS-1$
			createServiceLocator(display, e4Context);
			create3xCompatServices(e4Context, display);

			installDefaultBinding(e4Context);
			// Create and run the UI (if any)
			workbench.createAndRunUI(application);
			saveModel();
			workbench.close();
			return EXIT_OK;
		} finally {
			if (display != null) {
				display.dispose();
			}
			if (instanceLocation != null) {
				instanceLocation.release();
			}
		}
	}

	private void applyToWorkbenchModel(final IApplicationNode applicationNode, final MApplication wbApp,
			final IEclipseContext appContext) {

		final MWindow window = wbApp.getChildren().get(0);
		final PerspectiveStackImpl stack = (PerspectiveStackImpl) AdvancedFactoryImpl.INSTANCE.createPerspectiveStack();
		stack.setParent((MElementContainer<MUIElement>) (EObject) window);
		stack.setVisible(true);
		stack.setToBeRendered(true);

		final NavigationTreeObserver navigationTreeObserver = new NavigationTreeObserver();
		navigationTreeObserver.addListener(new SubModuleActivationListener(wbApp));

		navigationTreeObserver.addListenerTo(applicationNode);
		final E4ViewHelper viewHelper = new E4ViewHelper();
		int i = 0;
		for (final ISubApplicationNode subApp : applicationNode.getChildren()) {
			viewHelper.createE4SubApplication(wbApp, stack, i, subApp);
			i++;
		}

	}

	private void createServiceLocator(final Display display, final IEclipseContext workbenchContext) {
		final IServiceLocatorCreator slc = new ServiceLocatorCreator();
		serviceLocator = (ServiceLocator) slc.createServiceLocator(null, null, new IDisposable() {
			public void dispose() {
				if (display != null && !display.isDisposed()) {
					MessageDialog.openInformation(null, WorkbenchMessages.Workbench_NeedsClose_Title,
							WorkbenchMessages.Workbench_NeedsClose_Message);
				}
			}
		});
		serviceLocator.setContext(workbenchContext);
		serviceLocator.registerService(IServiceLocatorCreator.class, slc);

	}

	private void preStart() {
		applicationNode = createModel();
		if (applicationNode == null) {
			throw new RuntimeException(
					"Application did not return an ApplicationModel in method 'createModel' but returned NULL. Cannot continue"); //$NON-NLS-1$
		}
		applyUserInterfaceFilters(applicationNode);
		ApplicationNodeManager.registerApplicationNode(applicationNode);
		createStartupNodes(applicationNode);

		initializeNode(applicationNode);
		installProgressProviderBridge();

	}

	private void applyUserInterfaceFilters(final IApplicationNode applicationNode) {
		final IUIFilterApplier filter = Service.get(IUIFilterApplier.class);
		if (filter != null) {
			filter.applyFilter(applicationNode);
		}
	}

	protected void createStartupNodes(final IApplicationNode applicationNode) {
		final INavigationNodeProvider navigationNodeProvider = NavigationNodeProvider.getInstance();
		final List<StartupNodeInfo> startups = navigationNodeProvider.getSortedStartupNodeInfos();
		for (final StartupNodeInfo startup : startups) {
			LOGGER.log(LogService.LOG_INFO, "creating " + startup.toString()); //$NON-NLS-1$
			applicationNode.create(new NavigationNodeId(startup.getId()));
		}
	}

	protected void initializeNode(final IApplicationNode model) {
		initializeModelDefaults(model);
	}

	protected void initializeModelDefaults(final IApplicationNode model) {
		initializeNodeDefaults(model);
	}

	protected void initializeNodeDefaults(final IApplicationNode node) {
		for (final ISubApplicationNode child : node.getChildren()) {
			initializeNodeDefaults(child);
		}
	}

	protected void initializeNodeDefaults(final ISubApplicationNode node) {
		for (final IModuleGroupNode child : node.getChildren()) {
			initializeNodeDefaults(child);
		}
	}

	protected void initializeNodeDefaults(final IModuleGroupNode node) {
		for (final IModuleNode child : node.getChildren()) {
			initializeNodeDefaults(child);
		}
	}

	protected void initializeNodeDefaults(final IModuleNode node) {
		initializeNodeDefaultIcon(node);
	}

	protected void initializeNodeDefaultIcon(final INavigationNode<?> node) {
	}

	private void installProgressProviderBridge() {
		disableEclipseProgressManager();
		/**
		 * install the riena ProgressProvider which handles creation of
		 * IProgressMontitor instances for scheduled jobs. riena provides a
		 * special monitor for background processing.
		 */
		final ProgressProviderBridge instance = ProgressProviderBridge.instance();
		Job.getJobManager().setProgressProvider(instance);
		instance.setVisualizerFactory(new ProgressVisualizerLocator());
	}

	protected void disableEclipseProgressManager() {
		if (RAPDetector.isRAPavailable()) {
			return;
		}
		// //we need to get the instance first as this is the only way to
		// lock/disable the singleton
		ProgressManager.getInstance();
		// //shutting down means uninstalling ProgressProvider and removing all
		// Job-Listeners
		ProgressManager.shutdownProgressManager();
	}

	@Override
	public void stop() {
		if (controller != null) {
			controller.getNavigationNode().dispose();
			if (controller.getNavigationNode().isDisposed()) {
				SwtUISynchronizer.setWorkbenchShutdown(true);
			}
		}
		super.stop();
	}

	// helping methods
	// ////////////////

	protected void create3xCompatServices(final IEclipseContext e4Context, final Display display) {
		final CommandService commandService = createCommandService(e4Context, display);

		createContextManager(e4Context, display);

		createBindingService(e4Context, commandService, display);

	}

	protected CommandService createCommandService(final IEclipseContext e4Context, final Display display) {
		runWithoutExceptions(display, new StartupThreading.StartupRunnable() {

			@Override
			public void runWithException() {
				Command.DEBUG_COMMAND_EXECUTION = Policy.DEBUG_COMMANDS;
				commandManager = e4Context.get(CommandManager.class);
			}
		});

		final CommandService[] commandService = new CommandService[1];
		runWithoutExceptions(display, new StartupThreading.StartupRunnable() {

			@Override
			public void runWithException() {
				commandService[0] = initializeCommandService(e4Context);

			}
		});
		return commandService[0];
	}

	protected void createContextManager(final IEclipseContext e4Context, final Display display) {
		runWithoutExceptions(display, new StartupThreading.StartupRunnable() {

			@Override
			public void runWithException() {
				ContextManager.DEBUG = Policy.DEBUG_CONTEXTS;
				contextManager = e4Context.get(ContextManager.class);
			}
		});

		final IContextService contextService = ContextInjectionFactory.make(ContextService.class, e4Context);

		runWithoutExceptions(display, new StartupThreading.StartupRunnable() {

			@Override
			public void runWithException() {
				contextManager.addContextManagerListener(new IContextManagerListener() {
					public void contextManagerChanged(final ContextManagerEvent contextManagerEvent) {
						if (contextManagerEvent.isContextChanged()) {
							final String id = contextManagerEvent.getContextId();
							if (id != null) {
								defineBindingTable(id);
							}
						}
					}
				});
				final EContextService ecs = e4Context.get(EContextService.class);
				ecs.activateContext(IContextService.CONTEXT_ID_DIALOG_AND_WINDOW);
			}
		});

		serviceLocator.registerService(IContextService.class, contextService);
	}

	protected void createBindingService(final IEclipseContext e4Context, final CommandService commandService,
			final Display display) {
	}

	public static void runWithoutExceptions(final Display display, final StartupThreading.StartupRunnable r)
			throws RuntimeException {
		display.syncExec(r);
		final Throwable throwable = r.getThrowable();
		if (throwable != null) {
			if (throwable instanceof Error) {
				throw (Error) throwable;
			} else if (throwable instanceof RuntimeException) {
				throw (RuntimeException) throwable;
			} else {
				throw new RuntimeException(throwable);
			}
		}
	}

	private CommandService initializeCommandService(final IEclipseContext e4Context) {
		final CommandService service = new CommandService(commandManager, e4Context);
		e4Context.set(ICommandService.class.getName(), service);
		service.readRegistry();
		return service;
	}

	private void defineBindingTable(final String id) {
		final List<MBindingTable> bindingTables = application.getBindingTables();
		if (contains(bindingTables, id)) {
			return;
		}
		if (WorkbenchPlugin.getDefault().isDebugging()) {
			WorkbenchPlugin.log("Defining a binding table: " + id); //$NON-NLS-1$
		}
		final MBindingTable bt = CommandsFactoryImpl.eINSTANCE.createBindingTable();
		bt.setBindingContext(getBindingContext(id));
		bindingTables.add(bt);
	}

	private boolean contains(final List<MBindingTable> bindingTables, final String id) {
		for (final MBindingTable bt : bindingTables) {
			if (id.equals(bt.getBindingContext().getElementId())) {
				return true;
			}
		}
		return false;
	}

	public MBindingContext getBindingContext(final String id) {
		// cache
		MBindingContext result = bindingContexts.get(id);
		if (result == null) {
			// search
			result = searchContexts(id, application.getRootContext());
			if (result == null) {
				// create
				result = MCommandsFactory.INSTANCE.createBindingContext();
				result.setElementId(id);
				result.setName("Auto::" + id); //$NON-NLS-1$
				application.getRootContext().add(result);
			}
			if (result != null) {
				bindingContexts.put(id, result);
			}
		}
		return result;
	}

	private MBindingContext searchContexts(final String id, final List<MBindingContext> rootContext) {
		for (final MBindingContext context : rootContext) {
			if (context.getElementId().equals(id)) {
				return context;
			}
			final MBindingContext result = searchContexts(id, context.getChildren());
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	protected void installDefaultBinding(final IEclipseContext e4Context) {
		//		final BindingServiceFacade facade = BindingServiceFacade.getDefault();
		//
		//		final IBindingService bindingService = e4Context.get(IBindingService.class);
		//		final String schemeId = getKeyScheme();
		//		final Scheme rienaScheme = facade.getScheme(bindingService, schemeId);
		//		try {
		//			// saving will activate (!) the scheme:
		//			final Binding[] bindings = facade.getBindings(bindingService);
		//			facade.savePreferences(bindingService, rienaScheme, bindings);
		//		} catch (final IOException ioe) {
		//			final Logger logger = Log4r.getLogger(Activator.getDefault(), this.getClass());
		//			logger.log(LogService.LOG_ERROR, "Could not activate scheme: " + schemeId, ioe); //$NON-NLS-1$
		//		}
	}

	/**
	 * @see org.eclipse.e4.ui.internal.workbench.swt.E4Application#createE4Workbench
	 *      (org.eclipse.equinox.app.IApplicationContext,
	 *      org.eclipse.swt.widgets.Display)
	 */
	@Override
	public E4Workbench createE4Workbench(final IApplicationContext applicationContext, final Display display) {

		final E4Workbench workbench = super.createE4Workbench(applicationContext, display);
		final IEclipseContext e4Context = workbench.getContext();

		controller = createApplicationController();

		e4Context.set(ApplicationController.class, controller);

		return workbench;
	}

	protected ApplicationController createApplicationController() {
		return new ApplicationController(applicationNode);
	}

	/**
	 * Overwrite to create own application model
	 * 
	 * @return IApplicationModelProvider - root of the configured application
	 *         model
	 */
	protected IApplicationNode createModel() {
		if (null == applicationModel) {
			applicationModel = new ApplicationNode(new NavigationNodeId(ApplicationNode.DEFAULT_APPLICATION_TYPEID));
		}
		return applicationModel;
	}

	protected String getKeyScheme() {
		return "org.eclipse.riena.ui.defaultBindings"; //$NON-NLS-1$
	}

	public void handleEvent(final Event event) {

	}

}
