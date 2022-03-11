package io.github.gastaldi;

import com.sun.source.tree.Tree;
import edu.emory.mathcs.backport.java.util.Collections;
import org.apache.maven.artifact.resolver.DefaultArtifactResolver;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.profiles.DefaultProfileManager;
import org.apache.maven.profiles.ProfileManager;
import org.apache.maven.project.DefaultProjectBuilderConfiguration;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ModelUtils;
import org.apache.maven.project.ProjectBuilderConfiguration;
import org.apache.maven.project.interpolation.ModelInterpolationException;
import org.apache.maven.project.interpolation.ModelInterpolator;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.apache.maven.settings.Settings;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.DependencyManager;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.resolution.ArtifactDescriptorResult;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * List the dependency management as a tree
 */
@Mojo(name = "tree", requiresProject = true)
public class DependencyManagementTreeMojo
        extends AbstractMojo {
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject mavenProject;

    @Component
    ModelInterpolator modelInterpolator;

    @Parameter(defaultValue = "${project.basedir}", readonly = true)
    File projectLocation;

    @Override
    public void execute()
            throws MojoExecutionException {
        Model originalModel;
        try {
            originalModel = modelInterpolator.interpolate(mavenProject.getOriginalModel(), projectLocation,
                    new DefaultProjectBuilderConfiguration(), false);
        } catch (ModelInterpolationException e) {
            throw new MojoExecutionException("Error while interpolating values", e);
        }
        List<Dependency> dependencies = originalModel.getDependencyManagement().getDependencies();
        TreeNode root = new TreeNode(mavenProject.getArtifact().toString(), dependencies.stream()
                .map(dep -> dep.getGroupId() + ":" + dep.getArtifactId() + ":" + dep.getVersion())
                .map(name -> new TreeNode(name, List.of()))
                .collect(Collectors.toList()));
        System.out.println(root);
    }
}
