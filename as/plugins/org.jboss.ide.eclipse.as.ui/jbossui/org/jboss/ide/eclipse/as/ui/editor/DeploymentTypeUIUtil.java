/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.ide.eclipse.as.ui.editor;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.eclipse.wst.server.core.TaskModel;
import org.eclipse.wst.server.ui.editor.ServerEditorPart;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;
import org.eclipse.wst.server.ui.internal.editor.ServerEditorPartInput;
import org.eclipse.wst.server.ui.internal.editor.ServerResourceCommandManager;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.jboss.ide.eclipse.as.ui.editor.IDeploymentTypeUI.IServerModeUICallback;

/**
 * Provide utility methods to acquire a valid callback for
 * use in either a new server wizard or inside a server editor
 * 
 * @author rob.stryker
 *
 */
public class DeploymentTypeUIUtil {

	public interface ICompletable {
		public void setComplete(boolean complete);
	}
	
	public static IServerModeUICallback getCallback(TaskModel tm, IWizardHandle handle, ICompletable completable) {
		return new NewServerWizardBehaviourCallback(tm, handle, completable);
	}

	public static IServerModeUICallback getCallback(final IServerWorkingCopy server, IEditorInput input, ServerEditorPart part, ServerEditorSection section) {
		return new ServerEditorUICallback(input, part, section);
	}
	
	/**
	 * For use inside a wizard fragment
	 */
	public static class NewServerWizardBehaviourCallback implements IServerModeUICallback {
		protected TaskModel tm;
		protected IWizardHandle handle;
		protected ICompletable completable;
		public NewServerWizardBehaviourCallback(TaskModel tm, 
				IWizardHandle handle, ICompletable completable) {
			this.tm = tm;
			this.handle = handle;
			this.completable = completable;
		}
		public IRuntime getRuntime() {
			return (IRuntime) tm.getObject(TaskModel.TASK_RUNTIME);
		}
		public IServerWorkingCopy getServer() {
			return (IServerWorkingCopy) tm.getObject(TaskModel.TASK_SERVER);
		}
		public IWizardHandle getHandle() {
			return handle;
		}
		public void execute(IUndoableOperation operation) {
			try {
				operation.execute(new NullProgressMonitor(), null);
			} catch(ExecutionException  ee) {
				// TODO
			}
		}
		public void executeLongRunning(Job j) {
			// depends on COMMON, DAMN
//			IWizardContainer container = ((WizardPage)handle).getWizard().getContainer();
//			try {
//				WizardUtils.runInWizard(j, null, container);
//			} catch(Exception e) {
//				// TODO clean
//			}
			j.schedule();
		}
		public void setErrorMessage(String msg) {
			if( completable != null )
				completable.setComplete(msg == null);
			handle.setMessage(msg, IMessageProvider.ERROR);
			handle.update();
		}
		public Object getAttribute(String key) {
			return tm.getObject(key);
		}
		public int getCallbackType() {
			return WIZARD;
		}
		@Override
		public void setComplete(boolean complete) {
			if( completable != null )
				completable.setComplete(complete);
		}
	}

	
	/**
	 * For use inside a server editor
	 */
	public static class ServerEditorUICallback implements IServerModeUICallback {
		private ServerResourceCommandManager commandManager;
		private ServerEditorPart part;
		private ServerEditorSection section;
		public ServerEditorUICallback(IEditorInput input, ServerEditorPart part, ServerEditorSection section ) {
			this.part = part;
			this.section = section;
			commandManager = ((ServerEditorPartInput) input).getServerCommandManager();
		}
		public IServerWorkingCopy getServer() {
			return part.getServer();
		}
		public void execute(IUndoableOperation operation) {
			commandManager.execute(operation);
		}
		public IRuntime getRuntime() {
			return part.getServer().getRuntime();
		}
		public void executeLongRunning(Job j) {
			j.schedule();
		}
		public void setErrorMessage(String msg) {
			if( section != null )
				section.setErrorMessage(msg);
			else
				part.setErrorMessage(msg);
		}
		public Object getAttribute(String key) {
			return null;
		}
		public int getCallbackType() {
			return EDITOR;
		}
		@Override
		public void setComplete(boolean complete) {
			// Do nothing
		}
	}
	

}
