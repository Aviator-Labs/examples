package org.aviatorlabs.ci;

import org.aviatorlabs.ci.resource.git.GitResource;
import org.aviatorlabs.ci.resource.git.GitResourceConfig;
import org.aviatorlabs.ci.sdk.Pipeline;
import org.aviatorlabs.ci.sdk.job.Job;
import org.aviatorlabs.ci.sdk.resource.Resource;
import org.aviatorlabs.ci.sdk.step.task.Task;

public class Main {
    public static void main(String[] args) {
        Pipeline pipeline = new Pipeline();

        GitResourceConfig repoConfig = GitResourceConfig.create("https://github.com/concourse/examples");
        Resource repo = GitResource.create("concourse-examples", repoConfig).setIcon("github");
        pipeline.addResource(repo);

        Job job = new Job("job")
                .markPublic()
                .addStep(repo.createGetDefinition())
                .addStep(Task.create("simple-task", repo.createGetDefinition(), "tasks/hello-world.yml"));

        pipeline.addJob(job);

        System.out.println(pipeline.render());
    }
}