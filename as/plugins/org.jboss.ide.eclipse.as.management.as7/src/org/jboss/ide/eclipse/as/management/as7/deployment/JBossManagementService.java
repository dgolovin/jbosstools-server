/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ide.eclipse.as.management.as7.deployment;

import org.jboss.ide.eclipse.as.core.server.internal.v7.IJBoss7DeploymentManager;
import org.jboss.ide.eclipse.as.core.server.internal.v7.IJBoss7ManagementInterface;
import org.jboss.ide.eclipse.as.core.server.internal.v7.IJBoss7ManagementService;

/**
 * @author Rob Stryker
 */
public class JBossManagementService implements IJBoss7ManagementService {

	private IJBoss7DeploymentManager deploymentManager = null;
	private IJBoss7ManagementInterface manager = null;
	
	public IJBoss7DeploymentManager getDeploymentManager() {
		if( deploymentManager == null )
			deploymentManager = new JBossDeploymentManager();
		return deploymentManager;
	}

	public IJBoss7ManagementInterface getManagementInterface() {
		// TODO Auto-generated method stub
		return null;
	}
}
