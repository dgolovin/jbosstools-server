/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.tools.jmx.ui.internal.actions;


import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.jboss.tools.jmx.core.ExtensionManager;
import org.jboss.tools.jmx.core.IAsyncRefreshable;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.core.JMXActivator;
import org.jboss.tools.jmx.core.JMXCoreMessages;
import org.jboss.tools.jmx.core.tree.Node;
import org.jboss.tools.jmx.core.tree.Root;
import org.jboss.tools.jmx.ui.Messages;
import org.jboss.tools.jmx.ui.Refreshable;
import org.jboss.tools.jmx.ui.internal.JMXImages;
import org.jboss.tools.jmx.ui.internal.views.navigator.MBeanExplorerContentProvider.ProviderCategory;


/**
 * Refresh a given node. If the node is an IConnectionWrapper, disconnect and reconnect.
 * Otherwise, adapt the node to a Refreshable and invoke its refresh() method. 
 * 
 * @author lhein
 */
public class RefreshAction extends Action implements IWorkbenchWindowActionDelegate {
	@SuppressWarnings("unused")
	private IWorkbenchWindow window;
	private StructuredViewer viewer;
	private String viewId;

	public RefreshAction() {
		super();
	}

	/**
	 * creates the refresh action
	 */
	public RefreshAction(String viewId) {
		super();
		this.viewId = viewId;
		setText(Messages.RefreshAction_text);
		setDescription(Messages.RefreshAction_description);
		setToolTipText(Messages.RefreshAction_tooltip);
		JMXImages.setLocalImageDescriptors(this, "refresh.gif"); //$NON-NLS-1$
	}

	/**
	 * Refresh the specified node in the tree viewer/ structured selection.
	 *
	 * @param onode - node to refresh
	 */
	private void refreshObjectNode(Object onode)
	{
		if (onode == null)
			return;

		if (onode instanceof Refreshable) {
			Refreshable refreshable = (Refreshable) onode;
			refreshable.refresh();
			refreshViewer(onode);
		} else if( onode instanceof IAsyncRefreshable ) {
			 ((IAsyncRefreshable)onode).refresh(() -> fireRefreshAsync(onode));
		} else if( onode instanceof ProviderCategory) {
			viewer.refresh(onode, true);
		} else {
			
			IConnectionWrapper wrapper2 = identifyConnectionWrapper(onode);

			if (wrapper2 != null && wrapper2.isConnected()) {
				ISelection sel = viewer.getSelection();
				TreePath[] paths = ((TreeViewer)viewer).getExpandedTreePaths();
				RefreshActionState.getDefault().setSelection(wrapper2, sel);
				RefreshActionState.getDefault().setExpansion(wrapper2, paths);
				final IConnectionWrapper wrapper = wrapper2;
				new Job(Messages.RefreshActionJobTitle) {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						try {
							wrapper.disconnect();
							wrapper.connect();
							fireRefreshAsync(wrapper);
						} catch (Exception ex) {
						    Status status =
							new Status(IStatus.ERROR, JMXActivator.PLUGIN_ID, JMXCoreMessages.RefreshJobFailed,	ex);
							ErrorDialog.openError(Display.getCurrent().getActiveShell(), JMXCoreMessages.RefreshJob,
									null, status);
							return status;
						}
						return Status.OK_STATUS;
					}
				}.schedule();
			}
		}
	}  // refreshObjectNode

	private IConnectionWrapper identifyConnectionWrapper(Object onode) {
		IConnectionWrapper wrapper2 = null;

		// Identify the connection wrapper.
		if (onode instanceof IConnectionWrapper) {
			wrapper2 = (IConnectionWrapper) onode;
		} else if (onode instanceof Node) {
			Root r = ((Node) onode).getRoot();
			wrapper2 = r == null ? null : r.getConnection();
		}
		return wrapper2;
	}

	private void fireRefreshAsync(final Object wrapper) {
		Display.getDefault().asyncExec(() -> {
			refreshViewer(wrapper);
			if (viewer instanceof TreeViewer && !((TreeViewer) viewer).getTree().isDisposed()) {
				TreeViewer treeViewer = (TreeViewer) viewer;
				treeViewer.expandToLevel(wrapper, 1);
			}
		});
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		IWorkbench wb = PlatformUI.getWorkbench();
		if (wb == null) {
			return;
		}

		IWorkbenchWindow aww = wb.getActiveWorkbenchWindow();
		if (aww == null) {
			return;
		}

		IWorkbenchPage ap = aww.getActivePage();
		if (ap == null) {
			return;
		}

		ISelection sel = getSelection(ap);
		if (sel == null) {
			IConnectionWrapper[] connections = ExtensionManager.getAllConnections();
			if (connections.length > 0)
				refreshObjectNode(connections[0]);
		}
		else if (sel instanceof TreeSelection) {
			TreeSelection treeSelection = (TreeSelection)sel;
			refreshObjectNode(treeSelection.getFirstElement());
		}
		else if (sel instanceof StructuredSelection) {
			StructuredSelection ss = (StructuredSelection) sel;
			refreshObjectNode(ss.getFirstElement());
		}
	}  // run

	protected ISelection getSelection(IWorkbenchPage ap) {
		return ap.getSelection(viewId);
	}

	private void refreshViewer(Object node) {
		if (viewer != null) {
			viewer.refresh(node);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		run();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		//No actiobn taken on selection change
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	@Override
	public void dispose() {
		// Nothing to dispose
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void setViewer(StructuredViewer viewer) {
		this.viewer = viewer;
	}
}
