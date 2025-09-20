package org.aviatorlabs.ci;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class TestUtils {
    public static JsonElement loadFromAssets(String filename) {
        InputStream is = TestUtils.class.getResourceAsStream(String.format("/assets/examples/%s", filename));

        JsonElement expected;

        try {
            assert is != null;
            try (InputStreamReader reader = new InputStreamReader(is)) {
                expected = new Gson().fromJson(reader, JsonElement.class);

                return expected;
            }
        } catch (IOException ignored) {
            throw new RuntimeException("File not found");
        }
    }

    public static JsonElement loadFromTarget(String filename) {
        Path parent;

        try {
            parent = Paths.get(Objects.requireNonNull(GitHubReleaseExample.class.getResource("/")).toURI()).getParent();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        File file = new File(parent.toFile(), "generated-pipelines/%s".formatted(filename));

        InputStream is;

        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        JsonElement expected;

        try (InputStreamReader reader = new InputStreamReader(is)) {
            expected = new Gson().fromJson(reader, JsonElement.class);

            return expected;
        } catch (IOException ignored) {
            throw new RuntimeException("File not found");
        }
    }
}
