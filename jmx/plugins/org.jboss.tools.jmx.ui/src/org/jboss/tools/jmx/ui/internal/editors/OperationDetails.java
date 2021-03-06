/*******************************************************************************
 * Copyright (c) 2007 Jeff Mesnil
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.jboss.tools.jmx.ui.internal.editors;

import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.jboss.tools.jmx.core.IConnectionWrapper;
import org.jboss.tools.jmx.core.IJMXRunnable;
import org.jboss.tools.jmx.core.JMXException;
import org.jboss.tools.jmx.core.MBeanOperationInfoWrapper;
import org.jboss.tools.jmx.ui.JMXUIActivator;
import org.jboss.tools.jmx.ui.Messages;
import org.jboss.tools.jmx.ui.internal.MBeanUtils;
import org.jboss.tools.jmx.core.util.StringUtils;
import org.jboss.tools.jmx.ui.internal.dialogs.OperationInvocationResultDialog;

public class OperationDetails extends AbstractFormPart implements IDetailsPage {

    private FormToolkit toolkit;

    private Composite container;

    private MBeanOperationInfoWrapper opInfoWrapper;

    private Section section;

    public OperationDetails(IFormPart masterSection) {
    }

    public void createContents(Composite parent) {
        TableWrapLayout layout = new TableWrapLayout();
        parent.setLayout(layout);

        toolkit = getManagedForm().getToolkit();

        section = toolkit.createSection(parent, Section.TITLE_BAR | SWT.WRAP
                | Section.DESCRIPTION);
        section.marginWidth = 10;
        section.setText(Messages.OperationDetails_title);
        section.setDescription(""); //$NON-NLS-1$
        section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

        container = toolkit.createComposite(section);
        section.setClient(container);
        GridLayout glayout = new GridLayout();
        glayout.marginWidth = glayout.marginHeight = 0;
        glayout.numColumns = 2;
        glayout.makeColumnsEqualWidth = false;
        container.setLayout(glayout);
    }

    public void selectionChanged(IFormPart part, ISelection selection) {
        if (!(selection instanceof IStructuredSelection))
            return;

        Object obj = ((IStructuredSelection) selection).getFirstElement();
        if (obj instanceof MBeanOperationInfoWrapper) {
            MBeanOperationInfoWrapper wrapper = (MBeanOperationInfoWrapper) obj;
            if (wrapper == opInfoWrapper) {
                return;
            }
            // update the currently selected contribution to the one to be
            // displayed, if null
            // the controls displayed are still disposed, this is to reflect
            // a removed contribution
            opInfoWrapper = wrapper;
            drawInvocationDetails(wrapper);
        } else {
            clear();
        }
    }

    public void clear() {
        drawInvocationDetails(null);
    }

    protected void drawInvocationDetails(MBeanOperationInfoWrapper wrapper) {
        if (container != null && !container.isDisposed()) {
            // remove any controls created from prior selections
            Control[] childs = container.getChildren();
            if (childs.length > 0) {
                for (int i = 0; i < childs.length; i++) {
                    childs[i].dispose();
                }
            }
        }
        if (wrapper == null) {
            return;
        }
        MBeanOperationInfo opInfo = wrapper.getMBeanOperationInfo();
        String desc = opInfo.getDescription();
        // FIX issue #27: the MBean operation description can be null
        if (desc != null) {
            section.setDescription(desc);
        }
        // composite for method signature [ return type | method button | ( |
        // Composite(1..n parameters) | ) ]
        Composite operationComposite = toolkit.createComposite(container, SWT.BORDER);
        GridLayout gl = new GridLayout(5, false);
        gl.verticalSpacing = 10;
        operationComposite.setLayout(gl);
        
        // return type
        String returnString = opInfo.getReturnType() != null ? StringUtils.toString(opInfo.getReturnType()) : "void"; //$NON-NLS-1$
        Label returnTypeLabel = toolkit.createLabel(operationComposite,returnString);
        returnTypeLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER,
                false, false));
        // method name
        InvokeOperationButton invocationButton = new InvokeOperationButton(operationComposite,
                SWT.PUSH);
        Label leftParenthesis = toolkit.createLabel(operationComposite, "("); //$NON-NLS-1$
        GridData leftParenData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
        leftParenData.horizontalSpan = 3;
        leftParenthesis.setLayoutData(leftParenData);

        
        
        
        
        /*
         * Parameters
         *    Each paramter gets its own line, 5 columns
         *    Column 1 is buffer (indentation)
         *    Column 2 is Text for inputting value
         *    Column 3 is param name
         *    Column 4 and Column 5 are description
         */
        final MBeanParameterInfo[] params = opInfo.getSignature();
        Text[] textParams = null;
        if (params.length > 0) {
            textParams = new Text[params.length];
            for (int j = 0; j < params.length; j++) {
            	new Label(operationComposite, SWT.NONE);
                MBeanParameterInfo param = params[j];
                textParams[j] = new Text(operationComposite, SWT.SINGLE | SWT.BORDER);
                textParams[j].setText(StringUtils.toString(param.getType()));
                GridData textData = new GridData(SWT.LEFT, SWT.TOP,true, true);
                textData.minimumWidth = 100;
                textParams[j].setLayoutData(textData);
                
                Label name = new Label(operationComposite,SWT.NONE);
                name.setText(params[j].getName() == null ? "" : params[j].getName()); //$NON-NLS-1$
                name.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
                
                Label paramDesc = new Label(operationComposite, SWT.WRAP);
                paramDesc.setText(params[j].getDescription() == null ? "" : params[j].getDescription()); //$NON-NLS-1$
                GridData gd = new GridData(SWT.FILL, SWT.BOTTOM, false, true);
                gd.horizontalSpan = 2;
                gd.widthHint = 250;
                paramDesc.setLayoutData(gd);
            }
        }
        Label rightParenthesis = toolkit.createLabel(operationComposite, ")"); //$NON-NLS-1$
        rightParenthesis.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER,
                false, false));

        invocationButton.setTextParams(textParams);
        container.pack();
        container.layout();
    }

    private class InvokeOperationButton extends SelectionAdapter {

        private Text[] textParams;

        private Button button;

        public InvokeOperationButton(Composite parent, int style) {
            button = toolkit.createButton(parent, opInfoWrapper
                    .getMBeanOperationInfo().getName(), style);
            button.addSelectionListener(this);
            button.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
                    false));
        }

        void setTextParams(Text[] textParams) {
            this.textParams = textParams;
        }

        @Override
        public void widgetSelected(final SelectionEvent event) {
            final String[] strs = textParams == null ? null : new String[textParams.length];
        	if (textParams != null) {
                for (int i = 0; i < strs.length; i++) {
                    strs[i] = textParams[i].getText();
                }
        	}
        	new Thread() {
        		public void run() {
        			IConnectionWrapper connection = opInfoWrapper.getMBeanInfoWrapper().getParent().getConnection();
        			try {
	        			connection.run(new IJMXRunnable() {
							public void run(MBeanServerConnection connection)
									throws Exception {
			        			widgetSelected2(connection, strs);
							} });
        			} catch( JMXException jmxe) {
        			}
        		}
        	}.start();
        }
        
        protected void widgetSelected2(MBeanServerConnection connection, String[] strs) {
            try {
                MBeanParameterInfo[] paramInfos = opInfoWrapper
                        .getMBeanOperationInfo().getSignature();
                Object[] paramList = null;
                if (textParams != null) {
                    paramList = MBeanUtils.getParameters(strs, paramInfos);
                }
                ObjectName objectName = opInfoWrapper.getObjectName();
                String methodName = opInfoWrapper.getMBeanOperationInfo()
                        .getName();
                Object result;
                if (paramList != null) {
                    String[] paramSig = new String[paramInfos.length];
                    for (int i = 0; i < paramSig.length; i++) {
                        paramSig[i] = paramInfos[i].getType();
                    }
                    result = connection.invoke(objectName, methodName, paramList,
                            paramSig);
                } else {
                    result = connection.invoke(objectName, methodName, new Object[0],
                            new String[0]);
                }
                if ("void".equals(opInfoWrapper.getMBeanOperationInfo() //$NON-NLS-1$
                        .getReturnType())) {
                	Display.getDefault().asyncExec(new Runnable() { public void run() { 
	                    MessageDialog.openInformation(container.getShell(),
	                            Messages.OperationDetails_invocationResult,
	                            Messages.OperationDetails_invocationSuccess);
                	}});
                    return;
                } else {
                	final Object result2 = result;
                	Display.getDefault().asyncExec(new Runnable() { public void run() { 
                		OperationInvocationResultDialog.open(container.getShell(), result2);
                	}});
                }
            } catch (Exception e) {
                String message = e.getClass().getName() + ": " + e.getLocalizedMessage(); //$NON-NLS-1$
                JMXUIActivator.log(IStatus.ERROR, e.getClass().getName(), e);
                // if the exception has a cause, it is likely more interesting
                // since it may be the exception thrown by the mbean
                // implementation
                // rather than the exception thrown by the mbean server
                // connection
                if (e.getCause() != null) {
                    message = e.getCause().getClass().getName() + ": " + e.getCause().getLocalizedMessage(); //$NON-NLS-1$
                }
                final String message2 = message;
            	Display.getDefault().asyncExec(new Runnable() { public void run() { 
	                MessageDialog.openError(container.getShell(),
	                        Messages.OperationDetails_invocationError, message2);
            	}});
            }
        }
    }
}