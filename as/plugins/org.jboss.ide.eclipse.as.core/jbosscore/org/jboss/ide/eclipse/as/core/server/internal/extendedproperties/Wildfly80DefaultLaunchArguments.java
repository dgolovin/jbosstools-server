/*******************************************************************************
 * Copyright (c) 2007 - 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.ide.eclipse.as.core.server.internal.extendedproperties;

import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.core.util.ServerAttributeHelper;
import org.jboss.tools.as.core.server.controllable.subsystems.internal.XPathsPortsController;

public class Wildfly80DefaultLaunchArguments extends
		JBoss71DefaultLaunchArguments {
	public Wildfly80DefaultLaunchArguments(IServer s) {
		super(s);
	}
	public Wildfly80DefaultLaunchArguments(IRuntime rt) {
		super(rt);
	}
	public String getStartDefaultVMArgs() {
		String args =  super.getStartDefaultVMArgs() 
				+ "-Dorg.jboss.logmanager.nocolor=true";  //$NON-NLS-1$
			if(server!=null) {
				if (server.getAttribute("offcet", false)) { //$NON-NLS-1$
					
				}
				int web_port = ServerAttributeHelper.createHelper(server).getAttribute(XPathsPortsController.WEB_PORT, XPathsPortsController.WILDFLY8_MANAGEMENT_PORT_DEFAULT_PORT);
				if(web_port!=XPathsPortsController.WILDFLY8_MANAGEMENT_PORT_DEFAULT_PORT) {
					args += " -Djboss.http.port=" + web_port; //$NON-NLS-1$
				}
				if (server.getAttribute("management_http_port", false)) { //$NON-NLS-1$
					
				}
			}
		
		return args;
	}
	protected String getMemoryArgs() {
		return "-Xms64m -Xmx512m -XX:MaxPermSize=256m "; //$NON-NLS-1$
	}
}
