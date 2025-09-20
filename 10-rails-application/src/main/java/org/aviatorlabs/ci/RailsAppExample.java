package org.aviatorlabs.ci;


import org.aviatorlabs.ci.resource.git.GitResource;
import org.aviatorlabs.ci.resource.git.GitResourceConfig;
import org.aviatorlabs.ci.resource.registry.RegistryImageConfig;
import org.aviatorlabs.ci.resource.registry.RegistryImageResourceType;
import org.aviatorlabs.ci.sdk.Pipeline;
import org.aviatorlabs.ci.sdk.job.BuildLogRetentionPolicy;
import org.aviatorlabs.ci.sdk.job.Job;
import org.aviatorlabs.ci.sdk.resource.AnonymousResource;
import org.aviatorlabs.ci.sdk.resource.Resource;
import org.aviatorlabs.ci.sdk.step.task.Task;
import org.aviatorlabs.ci.sdk.step.task.config.Command;
import org.aviatorlabs.ci.sdk.step.task.config.Input;
import org.aviatorlabs.ci.sdk.step.task.config.Platform;
import org.aviatorlabs.ci.sdk.step.task.config.TaskConfig;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class RailsAppExample {
    public static void main(String[] args) {
        Pipeline pipeline = new Pipeline();

        GitResourceConfig repoConfig = GitResourceConfig.create("https://github.com/rails/rails-contributors.git");
        Resource repo = GitResource.create("rails-contributors-git", repoConfig).setIcon("github");
        pipeline.addResource(repo);

        // Task Config
        AnonymousResource<RegistryImageConfig> ruby = AnonymousResource.create(
                RegistryImageResourceType.create(),
                RegistryImageConfig.create("ruby", "2.6.5")
        );
        String railsTesting = """
                echo "=== Setting up Postgres..."
                apt-get update
                apt-get install -y postgresql libpq-dev cmake nodejs
                cat > /etc/postgresql/*/main/pg_hba.conf <<-EOF
                host   all   postgres   localhost   trust
                EOF
                service postgresql restart
                echo "=== Project requires that we clone rails... "
                cd rails-contributors-git
                git clone --mirror https://github.com/rails/rails
                echo "=== Installing Gems..."
                gem install -N bundler
                bundle install
                echo "=== Running Tests..."
                bundle exec rails db:setup
                bundle exec rails test
                """;
        Command taskCommand = Command.createCommand("/bin/bash").addArg("-c").addArg(railsTesting);
        TaskConfig taskConfig = TaskConfig.create(Platform.LINUX, ruby, taskCommand)
                .addInput(Input.create(repo.createGetDefinition()))
                .addParam("RAILS_ENV", "test")
                .addParam("DATABASE_URL", "postgresql://postgres@localhost");

        Task task = Task.create("run-tests", taskConfig);

        // Job
        Job job = new Job("test").markPublic();

        job.addStep(repo.createGetDefinition().enableTrigger())
                .addStep(task)
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
            Path parent = Paths.get(Objects.requireNonNull(RailsAppExample.class.getResource("/")).toURI()).getParent();

            return "%s/generated-pipelines/%s.json".formatted(parent, pipelineName);
        } catch (Exception e) {
            throw new IllegalArgumentException("");
        }
    }
}