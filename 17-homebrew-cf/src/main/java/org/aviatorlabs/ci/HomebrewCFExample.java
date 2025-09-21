package org.aviatorlabs.ci;

import org.aviatorlabs.ci.config.LoadConfig;
import org.aviatorlabs.ci.config.GitHubReleaseDTO;
import org.aviatorlabs.ci.job.debian.CreateDebianPkgJob;
import org.aviatorlabs.ci.job.homebrew.CreateHomebrewReleaseJob;
import org.aviatorlabs.ci.resource.git.GitResource;
import org.aviatorlabs.ci.resource.git.GitResourceConfig;
import org.aviatorlabs.ci.resource.githubrelease.GitHubReleaseConfig;
import org.aviatorlabs.ci.resource.githubrelease.GitHubReleaseResource;
import org.aviatorlabs.ci.sdk.Group;
import org.aviatorlabs.ci.sdk.Pipeline;
import org.aviatorlabs.ci.sdk.variable.Variable;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class HomebrewCFExample {
    public static void main(String[] args) {
        Pipeline pipeline = new Pipeline();

        GitResource homebrew = GitResource.create(
                "homebrew",
                GitResourceConfig.create("github.com/aviatorlabs/examples", "main")
        );

        homebrew.setCheckEvery("6h");

        pipeline.addResource(homebrew);

        Group allGroup = new Group("all");
        Group homebrewGroup = new Group("homebrew");
        Group debianGroup = new Group("debian");

        for (GitHubReleaseDTO releaseEntry : LoadConfig.load("releases.yml").getGithubReleases()) {
            GitHubReleaseConfig gitHubReleaseConfig = GitHubReleaseConfig.create(releaseEntry.getUser(), releaseEntry.getRepository())
                    .setAccessToken(Variable.referenceVariable("github_access_token"));

            if (releaseEntry.getPreRelease()) {
                gitHubReleaseConfig.enablePreReleases();
            }

            GitHubReleaseResource release = GitHubReleaseResource.create(
                    releaseEntry.getName(),
                    gitHubReleaseConfig
            );
            release.setCheckEvery("6h");

            pipeline.addResource(release);

            if (releaseEntry.getJobs().getDebian()) {
                CreateDebianPkgJob job = CreateDebianPkgJob.create(homebrew, release);
                pipeline.addJob(job);

                allGroup.addJob(job);
                debianGroup.addJob(job);
            }

            if (releaseEntry.getJobs().getHomebrew()) {
                CreateHomebrewReleaseJob job = CreateHomebrewReleaseJob.create(homebrew, release);
                pipeline.addJob(job);

                allGroup.addJob(job);
                homebrewGroup.addJob(job);
            }
        }

        pipeline.addGroup(allGroup).addGroup(homebrewGroup).addGroup(debianGroup);

        try {
            pipeline.render(setPipelineOutput("generated"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String setPipelineOutput(String pipelineName) {
        try {
            Path parent = Paths.get(Objects.requireNonNull(HomebrewCFExample.class.getResource("/")).toURI()).getParent();

            return "%s/generated-pipelines/%s.json".formatted(parent, pipelineName);
        } catch (Exception e) {
            throw new IllegalArgumentException("");
        }
    }
}