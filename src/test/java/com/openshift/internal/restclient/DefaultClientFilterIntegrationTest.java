package com.openshift.internal.restclient;

import com.openshift.internal.restclient.model.build.BuildConfigBuilder;
import com.openshift.internal.restclient.model.project.OpenshiftProjectRequest;
import com.openshift.restclient.IClient;
import com.openshift.restclient.IResourceFactory;
import com.openshift.restclient.ResourceKind;
import com.openshift.restclient.model.IBuildConfig;
import com.openshift.restclient.model.IProject;
import com.openshift.restclient.model.IResource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static com.openshift.internal.restclient.IntegrationTestHelper.*;
import static com.openshift.restclient.ResourceKind.BUILD_CONFIG;
import static org.junit.Assert.*;

public class DefaultClientFilterIntegrationTest {

	private static final String VERSION = "v1";

	private static final Logger LOG = LoggerFactory.getLogger(DefaultClientFilterIntegrationTest.class);

	private static IClient client;

	private static IResourceFactory factory;

	private static List<IBuildConfig> buildConfigs = new ArrayList();
	private static IProject project;

	private static IntegrationTestHelper helper = new IntegrationTestHelper();



	@BeforeClass
	public static void  setup() {

		client = helper.createClientForBasicAuth();
		factory = new ResourceFactory(client);
		OpenshiftProjectRequest projectRequest = factory.create(VERSION, ResourceKind.PROJECT_REQUEST);
		projectRequest.setName(helper.generateNamespace());
		project = (IProject) client.create(projectRequest);

		buildConfigs.add(createBuildConfigWithLabels(project, "build1", new HashMap<String, String>() {{
			put("foo", "yes");
			put("bar", "no");
			put("baz", "no");
		}}));

		buildConfigs.add(createBuildConfigWithLabels(project, "build2", new HashMap<String, String>() {{
			put("foo", "no");
			put("bar", "yes");

		}}));

		buildConfigs.add(createBuildConfigWithLabels(project, "build3", new HashMap<String, String>() {{
			put("foo", "yes");
			put("bar", "yes");
		}}));

		buildConfigs.add(createBuildConfigWithLabels(project, "build4", new HashMap<>()));

	}

	@AfterClass
	public static void cleanup() {
		buildConfigs.forEach(bc -> cleanUpResource(client, bc));
		cleanUpResource(client, project);
	}

	@Test
	public void testFilteringWithOneLabel() {
		List<IBuildConfig> list = client.list(BUILD_CONFIG, project.getNamespace(), new HashMap<String, String>() {{
					put("foo", "yes");
				}});

		assertEquals(2, list.size());
		Set<String> names = list.stream().map(IResource::getName).collect(Collectors.toSet());
		assertTrue("Should contain build1", names.contains("build1"));
		assertTrue("Should contain build3", names.contains("build3"));

	}

	@Test
	public void testFilteringWithTwoLabel() {
		List<IBuildConfig> list = client.list(BUILD_CONFIG, project.getNamespace(), new HashMap<String, String>() {{
					put("foo", "yes");
					put("bar", "no");
				}});

		assertEquals(1, list.size());
		IBuildConfig bc = list.get(0);
		assertEquals("build1", bc.getName());
	}

	@Test
	public void testFilteringWithLabelExist() {
		List<IBuildConfig> list = client.list(BUILD_CONFIG, project.getNamespace(), "baz");

		assertEquals(1, list.size());
		IBuildConfig bc = list.get(0);
		assertEquals("build1", bc.getName());
	}

	@Test
	public void testFilteringWithLabelNotExist() {
		List<IBuildConfig> list =
				client.list(BUILD_CONFIG, project.getNamespace(), "!baz");

		assertEquals(3, list.size());
		Set<String> names = list.stream().map(IResource::getName).collect(Collectors.toSet());
		assertTrue("Should contain build2", names.contains("build2"));
		assertTrue("Should contain build3", names.contains("build3"));
		assertTrue("Should contain build4", names.contains("build4"));


	}

	@Test
	public void testFilteringWithLabelNotEqualTo() {
		List<IBuildConfig> list = client.list(BUILD_CONFIG, project.getNamespace(), "foo!=yes");

		assertEquals(2, list.size());
		Set<String> names = list.stream().map(IResource::getName).collect(Collectors.toSet());
		assertTrue("Should contain build2", names.contains("build2"));
		assertTrue("Should contain build4", names.contains("build4"));
	}

	@Test
	public void testFilteringWithLabelCombinedLabelQuery() {
		List<IBuildConfig> list = client.list(BUILD_CONFIG, project.getNamespace(), "foo,bar=no");

		assertEquals(1, list.size());
		IBuildConfig bc = list.get(0);
		assertEquals("build1", bc.getName());
	}



	private static IBuildConfig createBuildConfigWithLabels(IProject project, String name, HashMap<String, String> labelFilter) {

		IBuildConfig bc = new BuildConfigBuilder(client)
				.named(name)
				.inNamespace(project.getNamespace())
				.buildOnConfigChange(true)
				.buildOnImageChange(true)
				.buildOnSourceChange(true)
				.fromGitSource()
				.fromGitUrl("https://foo/bar/repo.git")
				.usingGitReference("branch")
				.inContextDir("root/directory")
				.end()
				.usingSourceStrategy()
				.fromImageStreamTag("builder:latest")
				.inNamespace("other")
				.end()
				.toImageStreamTag("foo/target:latest")
				.withLabels(labelFilter)
				.build();


		bc = client.create(bc);
		assertNotNull("Exp. the bc to be found but was not",
				waitForResource(client, BUILD_CONFIG, project.getName(), bc.getName(),
						5 * MILLISECONDS_PER_SECOND));
		return bc;
	}

}
