package net.agilhard.maven.plugins.jpacktool.base.handler;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.util.Map.Entry;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.apache.maven.shared.dependency.graph.traversal.DependencyNodeVisitor;
import org.codehaus.plexus.languages.java.jpms.JavaModuleDescriptor;

import net.agilhard.maven.plugins.jpacktool.base.mojo.AbstractToolMojo;

public abstract class AbstractEndVisitDependencyHandler extends AbstractDependencyHandler {

	public class HandleDependencyRootEndVisitor implements DependencyNodeVisitor {

		public MojoExecutionException mojoExecutionException;
		public MojoFailureException mojoFailureException;

		/**
		 * Starts the visit to the specified dependency node.
		 *
		 * @param node the dependency node to visit
		 * 
		 * @return <code>true</code> 
		 */
		public boolean visit(final DependencyNode node) {
			boolean b = !node.toNodeString().endsWith(":test");
						
			if (excludedArtifacts != null) {
				b = b && (!excludedArtifacts.contains(node.getArtifact()));
			}
			
			String classifier=node.getArtifact().getClassifier();

			if ( "jpacktool_jdeps".equals(classifier) ) {
				b=false;
			}

			return b;
		}

		/**
		 * Ends the visit to to the specified dependency
		 * node.AbstractEndVIsitDependencyHandler
		 *
		 * @param node the dependency node to visit
		 * @return <code>true</code> to visit the specified dependency node's next
		 *         sibling, <code>false</code> to skip the specified dependency node's
		 *         next siblings and proceed to its parent
		 */
		@Override
		public boolean endVisit(final DependencyNode node) {

			boolean b = !node.toNodeString().endsWith(":test");
			if (excludedArtifacts != null) {
				b = b && (!excludedArtifacts.contains(node.getArtifact()));
			}
			if (b) {
				try {
					handleDependencyNode(node);
				} catch (final MojoExecutionException e) {
					getLog().error("endVisit -> MojoExecutionException", e);
					mojoExecutionException = e;
				} catch (final MojoFailureException e) {
					getLog().error("endVisit -> MojoFailureException", e);
					mojoFailureException = e;
				}

			}
			return b;
		}
	}

	public AbstractEndVisitDependencyHandler(AbstractToolMojo mojo, DependencyGraphBuilder dependencyGraphBuilder) {
		super(mojo, dependencyGraphBuilder);
	}

	@Override
	protected abstract void handleNonModJar(DependencyNode dependencyNode, Artifact artifact,
			Entry<File, JavaModuleDescriptor> entry) throws MojoExecutionException, MojoFailureException;

	@Override
	protected abstract void handleModJar(DependencyNode dependencyNode, Artifact artifact,
			Entry<File, JavaModuleDescriptor> entry) throws MojoExecutionException, MojoFailureException;

	protected void handleDependencyRoot(final DependencyNode dependencyNode)
			throws MojoExecutionException, MojoFailureException {
		HandleDependencyRootEndVisitor visitor = new HandleDependencyRootEndVisitor();
		dependencyNode.accept(visitor);
		if (visitor.mojoExecutionException != null) {
			throw visitor.mojoExecutionException;
		}
		if (visitor.mojoFailureException != null) {
			throw visitor.mojoFailureException;
		}
	}

}
