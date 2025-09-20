package org.aviatorlabs.ci;

import com.google.gson.JsonElement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("PHP Application Module")
class PHPAppExampleTest {

    @Test
    @DisplayName("Comparison - PHP Application")
    void comparisonTest() {
        PHPAppExample.main(null);

        String fileName = "generated.json";

        JsonElement generated = TestUtils.loadFromAssets(fileName);
        JsonElement expected = TestUtils.loadFromTarget(fileName);

        assertEquals(expected, generated);
    }
}