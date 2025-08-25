package org.aviatorlabs.ci;


import org.aviatorlabs.ci.resource.registry.RegistryImageConfig;
import org.aviatorlabs.ci.resource.registry.RegistryImageResource;
import org.aviatorlabs.ci.sdk.Pipeline;
import org.aviatorlabs.ci.sdk.job.BuildLogRetentionPolicy;
import org.aviatorlabs.ci.sdk.job.Job;
import org.aviatorlabs.ci.sdk.resource.Resource;
import org.aviatorlabs.ci.sdk.step.task.Task;
import org.aviatorlabs.ci.sdk.step.task.config.Command;
import org.aviatorlabs.ci.sdk.step.task.config.Platform;
import org.aviatorlabs.ci.sdk.step.task.config.TaskConfig;

public class Main {
    public static void main(String[] args) {
        Pipeline pipeline = new Pipeline();

        RegistryImageConfig v120Config = RegistryImageConfig.create("golang", "1.20-alpine");
        Resource v120Image = RegistryImageResource.create("golang-1.20.x-image", v120Config).setIcon("docker");

        RegistryImageConfig v121Config = RegistryImageConfig.create("golang", "1.21-alpine");
        Resource v121Image = RegistryImageResource.create("golang-1.21.x-image", v121Config).setIcon("docker");

        RegistryImageConfig v122Config = RegistryImageConfig.create("golang", "1.22-alpine");
        Resource v122Image = RegistryImageResource.create("golang-1.22.x-image", v122Config).setIcon("docker");

        pipeline.addResource(v120Image).addResource(v121Image).addResource(v122Image);

        String goTest = """
                GOPATH=$PWD/go
                
                go version
                """;

        TaskConfig config = TaskConfig.create(Platform.LINUX, Command.createCommand("/bin/sh").addArg("-c").addArg(goTest));
        BuildLogRetentionPolicy policy = BuildLogRetentionPolicy.create().setBuilds(50);

        Job v120 = new Job("golang-1.20").markPublic()
                .addStep(v120Image.createGetDefinition().enableTrigger())
                .addStep(Task.create("run-tests", config).setImage(v120Image.createGetDefinition()))
                .setBuildLogRetention(policy);
        Job v121 = new Job("golang-1.21").markPublic()
                .addStep(v121Image.createGetDefinition().enableTrigger())
                .addStep(Task.create("run-tests", config).setImage(v121Image.createGetDefinition()))
                .setBuildLogRetention(policy);
        Job v122 = new Job("golang-1.22").markPublic()
                .addStep(v122Image.createGetDefinition().enableTrigger())
                .addStep(Task.create("run-tests", config).setImage(v122Image.createGetDefinition()))
                .setBuildLogRetention(policy);

        pipeline.addJob(v120).addJob(v121).addJob(v122);

        System.out.println(pipeline.render());
    }
}