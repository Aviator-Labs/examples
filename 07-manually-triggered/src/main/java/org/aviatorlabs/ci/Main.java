package org.aviatorlabs.ci;


import org.aviatorlabs.ci.resource.registry.RegistryImageConfig;
import org.aviatorlabs.ci.resource.registry.RegistryImageResourceType;
import org.aviatorlabs.ci.resource.time.TimeConfig;
import org.aviatorlabs.ci.resource.time.TimeResource;
import org.aviatorlabs.ci.sdk.Pipeline;
import org.aviatorlabs.ci.sdk.job.BuildLogRetentionPolicy;
import org.aviatorlabs.ci.sdk.job.Job;
import org.aviatorlabs.ci.sdk.resource.AnonymousResource;
import org.aviatorlabs.ci.sdk.resource.Resource;
import org.aviatorlabs.ci.sdk.step.task.Task;
import org.aviatorlabs.ci.sdk.step.task.config.Command;
import org.aviatorlabs.ci.sdk.step.task.config.Platform;
import org.aviatorlabs.ci.sdk.step.task.config.TaskConfig;

public class Main {
    public static void main(String[] args) {
        Pipeline pipeline = new Pipeline();

        Resource every30Seconds = TimeResource.create("every-30s", TimeConfig.create().setInterval("30s")).setIcon("clock-outline");
        pipeline.addResource(every30Seconds);

        AnonymousResource<RegistryImageConfig> busyBox = AnonymousResource.create(
                RegistryImageResourceType.create(),
                RegistryImageConfig.create("busybox")
        );

        Command command = Command.createCommand("echo").addArg("Hello, world!");

        TaskConfig config = TaskConfig.create(Platform.LINUX, busyBox, command);
        Task simpleTask = Task.create("simple-task", config);

        Job triggeredFirst = new Job("triggered-first")
                .markPublic()
                .setBuildLogRetention(BuildLogRetentionPolicy.create().setBuilds(20))
                .addStep(every30Seconds.createGetDefinition().enableTrigger())
                .addStep(simpleTask);

        Job notTriggered = new Job("not-triggered")
                .markPublic()
                .addStep(every30Seconds.createGetDefinition().addPassedRequirement(triggeredFirst))
                .addStep(simpleTask);

        Job triggeredSecond = new Job("triggered-second")
                .markPublic()
                .setBuildLogRetention(BuildLogRetentionPolicy.create().setBuilds(20))
                .addStep(every30Seconds.createGetDefinition().addPassedRequirement(triggeredFirst).enableTrigger())
                .addStep(simpleTask);

        pipeline.addJob(triggeredFirst).addJob(notTriggered).addJob(triggeredSecond);

        System.out.println(pipeline.render());
    }
}