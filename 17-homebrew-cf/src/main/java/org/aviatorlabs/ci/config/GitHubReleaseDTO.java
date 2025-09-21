package org.aviatorlabs.ci.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GitHubReleaseDTO {
    private String name;
    private String user;
    private String repository;
    private Boolean preRelease = Boolean.FALSE;

    private Jobs jobs;

    public String getName() {
        if (name == null) {
            return repository;
        }

        return name;
    }

    @Getter
    @Setter
    public static class Jobs {
        private Boolean homebrew = Boolean.FALSE;
        private Boolean debian = Boolean.FALSE;
    }
}
