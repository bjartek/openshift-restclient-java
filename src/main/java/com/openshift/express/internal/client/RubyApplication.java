/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package com.openshift.express.internal.client;

import java.util.Calendar;

import com.openshift.express.client.ICartridge;
import com.openshift.express.client.IOpenShiftService;
import com.openshift.express.client.IRubyApplication;
import com.openshift.express.client.OpenShiftException;

/**
 * @author William DeCoste
 * @author Andre Dietisheim
 */
public class RubyApplication extends Application implements IRubyApplication {

	public RubyApplication(String name, String uuid, String creationLog, String healthCheckPath, ICartridge cartridge,
			InternalUser user, IOpenShiftService service) {
		super(name, uuid, creationLog, healthCheckPath, cartridge, user, service);
	}

	public RubyApplication(String name, String uuid, ICartridge cartridge, ApplicationInfo applicationInfo, InternalUser user,
			IOpenShiftService service) {
		super(name, uuid, cartridge, applicationInfo, user, service);
	}

	public String threadDump() throws OpenShiftException {
		service.threadDumpApplication(name, cartridge, getInternalUser());
		
		return getRackLogFile();
	}
	
	private String getRackLogFile() {
		Calendar cal = Calendar.getInstance();
		
		String month = null;
		if (cal.get(Calendar.MONTH) > 8)
			month = String.valueOf(cal.get(Calendar.MONTH) + 1);
		else
			month = "0" + String.valueOf(cal.get(Calendar.MONTH) + 1);
		
		String day = null;
		if (cal.get(Calendar.DAY_OF_MONTH) > 9)
			day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		else
			day = "0" + String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
		
		
		String logFile = "logs/error_log-" + cal.get(Calendar.YEAR) + month + day + "-000000-EST";
		
		return logFile;
	}

}