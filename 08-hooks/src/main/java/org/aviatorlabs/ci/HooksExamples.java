package org.aviatorlabs.ci;


import org.aviatorlabs.ci.resource.registry.RegistryImageConfig;
import org.aviatorlabs.ci.resource.registry.RegistryImageResourceType;
import org.aviatorlabs.ci.sdk.Pipeline;
import org.aviatorlabs.ci.sdk.job.Job;
import org.aviatorlabs.ci.sdk.resource.AnonymousResource;
import org.aviatorlabs.ci.sdk.step.task.Task;
import org.aviatorlabs.ci.sdk.step.task.config.Command;
import org.aviatorlabs.ci.sdk.step.task.config.Platform;
import org.aviatorlabs.ci.sdk.step.task.config.TaskConfig;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class HooksExamples {
    public static void main(String[] args) {
        Pipeline pipeline = new Pipeline();

        AnonymousResource<RegistryImageConfig> busyBox = AnonymousResource.create(
                RegistryImageResourceType.create(),
                RegistryImageConfig.create("busybox")
        );

        // Successful Task Configuration
        Task onSuccessTask = generateTask(busyBox, "task-success", "echo", "This task succeeded!");
        Task onAbortTask = generateTask(busyBox, "task-aborted", "echo", "This task was aborted!");
        Task successfulTask = generateTask(busyBox, "successful-task", "sh", "-lc", "exit 0")
                .setOnSuccess(onSuccessTask)
                .setOnAbort(onAbortTask);

        // Failing Task Configuration
        Task onFailureTask = generateTask(busyBox, "task-failure", "echo", "This task failed!");
        Task failingTask = generateTask(busyBox, "failing-task", "sh", "-lc", "exit 1")
                .setOnFailure(onFailureTask);

        // Job Configuration
        Task onSuccessJob = generateTask(busyBox, "job-success", "echo", "This job succeeded!");
        Task onFailureJob = generateTask(busyBox, "job-failure", "echo", "This job failed!");
        Task onAbortJob = generateTask(busyBox, "job-aborted", "echo", "This job was aborted!");

        Job job = new Job("job")
                .markPublic()
                .addStep(successfulTask)
                .addStep(failingTask)
                .setOnSuccess(onSuccessJob)
                .setOnFailure(onFailureJob)
                .setOnAbort(onAbortJob);

        pipeline.addJob(job);

        try {
            pipeline.render(setPipelineOutput("generated"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String setPipelineOutput(String pipelineName) {
        try {
            Path parent = Paths.get(Objects.requireNonNull(HooksExamples.class.getResource("/")).toURI()).getParent();

            return "%s/generated-pipelines/%s.json".formatted(parent, pipelineName);
        } catch (Exception e) {
            throw new IllegalArgumentException("");
        }
    }

    private static Task generateTask(AnonymousResource<RegistryImageConfig> resource, String taskName, String simpleCommand, String... commandArgs) {
        Command command = Command.createCommand(simpleCommand);

        for (String arg : commandArgs) command.addArg(arg);

        TaskConfig config = TaskConfig.create(Platform.LINUX, resource, command);

        return Task.create(taskName, config);
    }
}