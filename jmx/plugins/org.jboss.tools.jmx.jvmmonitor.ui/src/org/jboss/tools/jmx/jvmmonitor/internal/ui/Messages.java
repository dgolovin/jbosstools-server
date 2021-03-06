/*******************************************************************************
 * Copyright (c) 2010 JVM Monitor project. All rights reserved. 
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.jboss.tools.jmx.jvmmonitor.internal.ui;

import org.eclipse.osgi.util.NLS;

/**
 * The messages.
 */
public final class Messages extends NLS {

    /** The bundle name. */
    private static final String BUNDLE_NAME = "org.jboss.tools.jmx.jvmmonitor.internal.ui.messages";//$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    /**
     * The constructor.
     */
    private Messages() {
        // do not instantiate
    }

    // preference page

    /** */
    public static String updatePeriodLabel;

    /** */
    public static String showLegendLabel;

    /** */
    public static String timelineGroupLabel;

    /** */
    public static String updatePeriodNotEnteredMsg;

    /** */
    public static String illegalUpdatePeriodMsg;

    /** */
    public static String updatePeriodOutOfRangeMsg;
    
    /** */
    public static String threadsGroupLabel;

    /** */
    public static String memoryGroupLabel;
    
    /** */
    public static String wideScopeThreadFilterLabel;
    
    /** */
    public static String wideScopeSWTResourceFilterLabel;
    
    
    /** */
    public static String toolsPreferencePageLabel;

    /** */
    public static String jdkRootDirectoryLabel;

    /** */
    public static String browseButton;

    /** */
    public static String autoDetectGroupLabel;

    /** */
    public static String enterMaxNumberOfClassesMsg;

    /** */
    public static String maxNumberOfClassesInvalidMsg;

    /** */
    public static String maxNumberOfClassesOutOfRangeMsg;

    /** */
    public static String maxNumberOfClassesLabel;
    
    public static String prefPageUpdatePeriodLabel;
    
    public static String jdkRootDirectoryNotEnteredMsg;
}
