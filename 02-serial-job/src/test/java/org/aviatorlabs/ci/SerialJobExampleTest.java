package org.aviatorlabs.ci;

import com.google.gson.JsonElement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Serial Job Module")
class SerialJobExampleTest {

    @Test
    @DisplayName("Comparison - Serial Job")
    void comparisonTest() throws IOException {
        SerialJobExample.main(null);

        String fileName = "generated.json";

        JsonElement generated = TestUtils.loadFromAssets(fileName);
        JsonElement expected = TestUtils.loadFromTarget(fileName);

        assertEquals(expected, generated);
    }
}