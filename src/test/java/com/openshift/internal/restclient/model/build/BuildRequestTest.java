/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package com.openshift.internal.restclient.model.build;

import com.openshift.internal.restclient.model.BuildConfig;
import com.openshift.restclient.IClient;
import com.openshift.restclient.images.DockerImageURI;
import com.openshift.restclient.model.build.BuildStrategyType;
import com.openshift.restclient.model.build.IBuildStrategy;
import com.openshift.restclient.model.build.ICustomBuildStrategy;
import com.openshift.restclient.model.build.IDockerBuildStrategy;
import org.jboss.dmr.ModelNode;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.openshift.internal.util.JBossDmrExtentions.getPath;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class BuildRequestTest {
	@Mock private IClient client;
	private BuildRequest config;
	private ModelNode node = new ModelNode();
	
	@Before
	public void setup(){
		config = new BuildRequest(node, client, null);
	}
	
	@Test
	public void testBuildRequestEnvVars(){

		config.setEnvironmentVariable( "env1", "value1" );
		assertEquals( 1, node.get( "env" ).asList().size() );

		config.setEnvironmentVariable( "env2", "value2" );
		assertEquals( 2, node.get( "env" ).asList().size() );

		for ( ModelNode mn :  node.get( "env" ).asList() ) {
			if ( mn.get("name").asString().equals( "env1" ) ) {
				assertEquals(mn.get("value").asString(), "value1");
			} else if (mn.get("name").asString().equals("env2")) {
				assertEquals(mn.get( "value").asString(), "value2");
			} else {
				fail( "Unexpected environment variable: " + mn.toJSONString(false) );
			}
		}

	}

}
