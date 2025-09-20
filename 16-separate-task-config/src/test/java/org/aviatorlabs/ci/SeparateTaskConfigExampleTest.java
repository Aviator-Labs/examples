package org.aviatorlabs.ci;

import com.google.gson.JsonElement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Separate Task Config Module")
class SeparateTaskConfigExampleTest {

    @Test
    @DisplayName("Comparison - Separate Task Config")
    void comparisonTest() {
        SeparateTaskConfigExample.main(null);

        String fileName = "generated.json";

        JsonElement generated = TestUtils.loadFromAssets(fileName);
        JsonElement expected = TestUtils.loadFromTarget(fileName);

        assertEquals(expected, generated);
    }
}