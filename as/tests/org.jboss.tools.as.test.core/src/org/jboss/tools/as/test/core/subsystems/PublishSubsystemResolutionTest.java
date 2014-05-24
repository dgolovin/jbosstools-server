/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.as.test.core.subsystems;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collection;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.jboss.ide.eclipse.as.core.util.JBossServerBehaviorUtils;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IControllableServerBehavior;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.IPublishController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ISubsystemController;
import org.jboss.ide.eclipse.as.wtp.core.server.behavior.ServerProfileModel;
import org.jboss.tools.as.test.core.internal.utils.MatrixUtils;
import org.jboss.tools.as.test.core.internal.utils.ServerCreationTestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.experimental.theories.ParametersSuppliedBy;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Theories.class)
public class PublishSubsystemResolutionTest extends TestCase {
	
	@Retention(RetentionPolicy.RUNTIME)
	@ParametersSuppliedBy(ServerSupplier.class)
	public @interface AllServers {}

	@Retention(RetentionPolicy.RUNTIME)
	@ParametersSuppliedBy(ModeSupplier.class)
	public @interface AllModes {}

	@Theory
	public void testResolution(@AllServers String server, @AllModes String mode) throws Exception {
		System.out.println(server + " " + mode);
		
		IServer instance = ServerCreationTestUtils.createMockServerWithRuntime(server, getClass().getName() + server);
		IServerWorkingCopy wc = instance.createWorkingCopy();
		ServerProfileModel.setProfile(wc, mode);
		try {
			instance = wc.save(false, new NullProgressMonitor());
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}

		IControllableServerBehavior beh = JBossServerBehaviorUtils.getControllableBehavior(instance);
		assertNotNull(beh);
		ISubsystemController controller = beh.getController(IPublishController.SYSTEM_ID);
		assertNotNull(controller);
		assertEquals(controller.getSubsystemMappedId(), "publish.filesystem.default");
		
		ServerCreationTestUtils.deleteAllServersAndRuntimes();
	}
	
	@After
	public void afterTest() {
		System.out.println("After call");
	}
}