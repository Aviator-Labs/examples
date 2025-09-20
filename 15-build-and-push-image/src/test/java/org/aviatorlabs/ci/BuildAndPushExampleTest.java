package org.aviatorlabs.ci;

import com.google.gson.JsonElement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Build and Push Image Module")
class BuildAndPushExampleTest {

    @Test
    @DisplayName("Comparison - Build and Push Image")
    void comparisonTest() {
        BuildAndPushExample.main(null);

        String fileName = "generated.json";

        JsonElement generated = TestUtils.loadFromAssets(fileName);
        JsonElement expected = TestUtils.loadFromTarget(fileName);

        assertEquals(expected, generated);
    }
}