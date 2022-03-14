package io.github.gastaldi;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.DefaultProjectBuilderConfiguration;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.interpolation.ModelInterpolationException;
import org.apache.maven.project.interpolation.ModelInterpolator;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * List the dependency management as a tree
 */
@Mojo(name = "tree", requiresProject = true)
public class DependencyManagementTreeMojo
        extends AbstractMojo {

    private static final String MAVEN_CENTRAL = "https://repo1.maven.org/maven2/";

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject mavenProject;

    @Component
    ModelInterpolator modelInterpolator;

    @Parameter(defaultValue = "${project.basedir}", readonly = true)
    File projectLocation;

    static final MavenXpp3Reader MAVEN_READER = new MavenXpp3Reader();

    @Override
    public void execute()
            throws MojoExecutionException {
        Model originalModel;
        try {
            originalModel = interpolateModel(mavenProject.getOriginalModel());
        } catch (ModelInterpolationException e) {
            throw new MojoExecutionException(e);
        }
        DependencyManagement dependencyManagement = originalModel.getDependencyManagement();
        List<TreeNode> children = null;
        if (dependencyManagement != null) {
            children = children(dependencyManagement.getDependencies());
        }
        System.out.println(new TreeNode(mavenProject.getArtifact().toString(), children));
    }

    private List<TreeNode> children(List<Dependency> dependencies) throws MojoExecutionException {
        List<TreeNode> children = new ArrayList<>();
        for (Dependency dependency : dependencies) {
            TreeNode node = new TreeNode(toArtifactDescription(dependency), null);
            if ("import".equalsIgnoreCase(dependency.getScope())) {
                // Recursive
                Model model = resolveModel(dependency);
                DependencyManagement dependencyManagement = model.getDependencyManagement();
                children.add(new TreeNode(toArtifactDescription(dependency), children(
                        dependencyManagement != null ? dependencyManagement.getDependencies() : null)));
            } else {
                children.add(new TreeNode(toArtifactDescription(dependency), null));
            }
        }
        return children;
    }

    private Model resolveModel(Dependency dependency) throws MojoExecutionException {
        //TODO: Use insanely complex maven resolver API for this
        URL url = null;
        try {
            url = new URL(MessageFormat.format("{0}{1}/{2}/{3}/{2}-{3}.{4}",
                    MAVEN_CENTRAL,
                    dependency.getGroupId().replace('.', '/'),
                    dependency.getArtifactId(),
                    dependency.getVersion(),
                    dependency.getType()));
        } catch (MalformedURLException e) {
            throw new MojoExecutionException(e);
        }

        try (InputStream is = url.openStream()) {
            Model model = MAVEN_READER.read(is);
            return interpolateModel(model);
        } catch (Exception e) {
            throw new MojoExecutionException(e);
        }
    }

    private Model interpolateModel(Model model) throws ModelInterpolationException {
        return modelInterpolator.interpolate(model, projectLocation,
                    new DefaultProjectBuilderConfiguration(), false);
    }

    private String toArtifactDescription(Dependency dependency) {
        //io.quarkus:quarkus-resteasy-server-common:jar:2.7.4.Final:compile
        return String.format("%s:%s:%s:%s", dependency.getGroupId(), dependency.getArtifactId(), dependency.getType(), dependency.getVersion());
    }
}
