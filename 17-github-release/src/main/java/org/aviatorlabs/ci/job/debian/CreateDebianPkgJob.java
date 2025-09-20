package org.aviatorlabs.ci.job.debian;

import org.aviatorlabs.ci.resource.git.GitResource;
import org.aviatorlabs.ci.resource.githubrelease.GitHubReleaseResource;
import org.aviatorlabs.ci.sdk.job.Job;
import org.aviatorlabs.ci.sdk.resource.Resource;

public class CreateDebianPkgJob extends Job {
    private CreateDebianPkgJob(Resource resource) {
        super("%s-debian".formatted(resource.getName()));
    }

    public static CreateDebianPkgJob create(GitResource homebrewRepo, GitHubReleaseResource release) {
        CreateDebianPkgJob job = new CreateDebianPkgJob(release);
        job.markPublic().addSerialGroup("apt");

        job.addStep(homebrewRepo.createGetDefinition())
                .addStep(release.createGetDefinition().enableTrigger());

        return job;
    }
}
