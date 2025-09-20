package org.aviatorlabs.ci;


import org.aviatorlabs.ci.resource.git.GitResource;
import org.aviatorlabs.ci.resource.git.GitResourceConfig;
import org.aviatorlabs.ci.resource.registry.RegistryImageConfig;
import org.aviatorlabs.ci.resource.registry.RegistryImageResource;
import org.aviatorlabs.ci.sdk.Pipeline;
import org.aviatorlabs.ci.sdk.job.BuildLogRetentionPolicy;
import org.aviatorlabs.ci.sdk.job.Job;
import org.aviatorlabs.ci.sdk.resource.Resource;
import org.aviatorlabs.ci.sdk.step.task.Task;
import org.aviatorlabs.ci.sdk.step.task.config.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class NodejsAppExample {
    public static void main(String[] args) {
        Pipeline pipeline = new Pipeline();

        GitResourceConfig repoConfig = GitResourceConfig.create("https://github.com/nodejs/nodejs.org.git");
        Resource repo = GitResource.create("repo", repoConfig).setIcon("github");
        pipeline.addResource(repo);

        RegistryImageConfig registryConfig = RegistryImageConfig.create("node", "22-slim");
        Resource image = RegistryImageResource.create("node-image", registryConfig).setIcon("docker");
        pipeline.addResource(image);

        // Task Config
        Output workspace = Output.create("workspace").setPath("repo");
        Command installCommand = Command.createCommand("npm")
                .addArg("ci")
                .addArg("--no-audit")
                .addArg("--no-fund")
                .setWorkingDirectory("repo");
        TaskConfig installConfig = TaskConfig.create(Platform.LINUX, installCommand)
                .addInput(Input.create(repo.createGetDefinition()))
                .addOutput(workspace);
        Task install = Task.create("install", installConfig);
        install.setImage(image.createGetDefinition());

        Command testCommand = Command.createCommand("npm")
                .addArg("run")
                .addArg("test")
                .setWorkingDirectory("repo");
        TaskConfig testConfig = TaskConfig.create(Platform.LINUX, testCommand)
                .addInput(Input.create(workspace).setPath("repo"));
        Task test = Task.create("test", testConfig);
        test.setImage(image.createGetDefinition());

        // Job Config
        Job job = new Job("test")
                .markPublic()
                .addStep(image.createGetDefinition())
                .addStep(repo.createGetDefinition().enableTrigger())
                .addStep(install)
                .addStep(test)
                .setBuildLogRetention(BuildLogRetentionPolicy.create().setBuilds(50));

        pipeline.addJob(job);

        try {
            pipeline.render(setPipelineOutput("generated"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String setPipelineOutput(String pipelineName) {
        try {
            Path parent = Paths.get(Objects.requireNonNull(NodejsAppExample.class.getResource("/")).toURI()).getParent();

            return "%s/generated-pipelines/%s.json".formatted(parent, pipelineName);
        } catch (Exception e) {
            throw new IllegalArgumentException("");
        }
    }
}