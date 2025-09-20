package org.aviatorlabs.ci;

import org.aviatorlabs.ci.resource.git.GitResource;
import org.aviatorlabs.ci.resource.git.GitResourceConfig;
import org.aviatorlabs.ci.resource.registry.RegistryImageConfig;
import org.aviatorlabs.ci.resource.registry.RegistryImageResourceType;
import org.aviatorlabs.ci.sdk.Pipeline;
import org.aviatorlabs.ci.sdk.job.BuildLogRetentionPolicy;
import org.aviatorlabs.ci.sdk.job.Job;
import org.aviatorlabs.ci.sdk.resource.AnonymousResource;
import org.aviatorlabs.ci.sdk.resource.get.Get;
import org.aviatorlabs.ci.sdk.step.SetPipeline;
import org.aviatorlabs.ci.sdk.step.task.Task;
import org.aviatorlabs.ci.sdk.step.task.config.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class SetPipelineExample {
    public static void main(String[] args) {

        Pipeline pipeline = new Pipeline();

        GitResource repo = GitResource.create("concourse-examples", GitResourceConfig.create("https://github.com/concourse/examples"));
        repo.setIcon("github").setCheckEvery("30m");

        pipeline.addResource(repo);

        BuildLogRetentionPolicy policy = BuildLogRetentionPolicy.create().setBuilds(50);

        // Set Self
        Job setSelf = new Job("set-self").markPublic();

        Get initialGet = repo.createGetDefinition().enableTrigger();

        setSelf.addStep(initialGet);
        setSelf.addStep(SetPipeline.create("self", initialGet, "pipelines/set-pipelines.yml"));
        setSelf.setBuildLogRetention(policy);

        Get blockedGet = repo.createGetDefinition().enableTrigger().addPassedRequirement(setSelf);

        // Set Example Pipelines
        Job setExamples = new Job("set-example-pipelines").markPublic();

        setExamples.addStep(blockedGet);

        setExamples.addStep(SetPipeline.create("job", blockedGet, "pipelines/hello-world.yml"));
        setExamples.addStep(SetPipeline.create("separate-task-config", blockedGet, "pipelines/separate-task-config.yml"));
        setExamples.addStep(SetPipeline.create("serial-job", blockedGet, "pipelines/serial-job.yml"));

        SetPipeline setPipelineVars = SetPipeline.create("pipeline-vars", blockedGet, "pipelines/pipeline-vars.yml");
        setPipelineVars.addVar("first", "initial").addVar("number", "9000").addVar("hello", "HAL");
        setExamples.addStep(setPipelineVars);

        SetPipeline setPipelineVarsFile = SetPipeline.create("pipeline-vars-file", blockedGet, "pipelines/pipeline-vars.yml");
        setPipelineVarsFile.addVarFile(blockedGet, "pipelines/vars-file.yml");
        setExamples.addStep(setPipelineVarsFile);

        SetPipeline instanceVarsOne = SetPipeline.create("instance-groups", blockedGet, "pipelines/pipeline-vars.yml");
        SetPipeline instanceVarsTwo = SetPipeline.create("instance-groups", blockedGet, "pipelines/pipeline-vars.yml");
        instanceVarsOne.addInstanceVar("first", "initial").addInstanceVar("number", "9000").addInstanceVar("hello", "HAL");
        instanceVarsTwo.addInstanceVar("first", "second").addInstanceVar("number", "3000").addInstanceVar("hello", "WALLY-E");
        setExamples.addStep(instanceVarsOne);
        setExamples.addStep(instanceVarsTwo);

        setExamples.addStep(SetPipeline.create("task-passing-artifact", blockedGet, "pipelines/task-passing-artifact.yml"));
        setExamples.addStep(SetPipeline.create("time-triggered", blockedGet, "pipelines/time-triggered.yml"));
        setExamples.addStep(SetPipeline.create("git-triggered", blockedGet, "pipelines/git-triggered.yml"));
        setExamples.addStep(SetPipeline.create("manual-trigger", blockedGet, "pipelines/manually-triggered.yml"));
        setExamples.addStep(SetPipeline.create("hooks", blockedGet, "pipelines/job-and-task-hooks.yml"));
        setExamples.addStep(SetPipeline.create("golang-lib", blockedGet, "pipelines/golang-lib.yml"));
        setExamples.addStep(SetPipeline.create("rails", blockedGet, "pipelines/rails-app-testing.yml"));
        setExamples.addStep(SetPipeline.create("nodejs", blockedGet, "pipelines/nodejs-app-testing.yml"));
        setExamples.addStep(SetPipeline.create("php", blockedGet, "pipelines/php-larvel-app-testing.yml"));
        setExamples.addStep(SetPipeline.create("java", blockedGet, "pipelines/java.yml"));
        setExamples.setBuildLogRetention(policy);

        // Set Rendered Pipelines
        Job setRendered = new Job("set-rendered-pipelines").markPublic();

        setRendered.addStep(blockedGet);

        AnonymousResource<RegistryImageConfig> carvelytt = AnonymousResource.create(
                RegistryImageResourceType.create(),
                RegistryImageConfig.create("taylorsilva/carvel-ytt")
        );

        String yttGen = """
                ytt -f ./concourse-examples/pipelines/templates/simple > hello-world-rendered.yml
                ytt -f ./concourse-examples/pipelines/templates/multiple-files > multi-files-rendered.yml
                mv *.yml ./pipeline/
                """;

        Command renderCommand = Command.createCommand("sh").addArg("-cx").addArg(yttGen);

        TaskConfig renderConfig = TaskConfig.create(Platform.LINUX, carvelytt, renderCommand)
                .addInput(Input.create(blockedGet))
                .addOutput(Output.create("pipeline"));

        Task renderTask = Task.create("render-pipelines", renderConfig);

        setRendered.addStep(renderTask);

        setRendered.addStep(SetPipeline.create("hello-world-rendered", "pipeline/hello-world-rendered.yml"));
        setRendered.addStep(SetPipeline.create("multi-files-rendered", "pipeline/multi-files-rendered.yml"));
        setRendered.setBuildLogRetention(policy);

        pipeline.addJob(setSelf).addJob(setExamples).addJob(setRendered);

        try {
            pipeline.render(setPipelineOutput("generated"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String setPipelineOutput(String pipelineName) {
        try {
            Path parent = Paths.get(Objects.requireNonNull(SetPipelineExample.class.getResource("/")).toURI()).getParent();

            return "%s/generated-pipelines/%s.json".formatted(parent, pipelineName);
        } catch (Exception e) {
            throw new IllegalArgumentException("");
        }
    }
}
