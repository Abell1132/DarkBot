package com.github.manolo8.darkbot.gui.titlebar;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class BackpageTaskTest {
    @TempDir Path directory;

    @Test
    @EnabledOnOs(OS.LINUX)
    void linuxLaunchPassesSandboxFlagAndPreservesArgumentsAndCaptchaOutput() throws Exception {
        Path executable = directory.resolve("browser with spaces");
        Files.writeString(executable, "#!/bin/sh\nprintf '%s\\n' \"$@\"\n"
                + "printf '[captchaResult]local-test\\n'\n");
        assertTrue(executable.toFile().setExecutable(true));
        ProcessBuilder builder = BackpageTask.browserBuilder(executable, true,
                "--sid", "session with spaces", "--url", "http://127.0.0.1/test?a=1&b=2");
        assertEquals(ProcessBuilder.Redirect.INHERIT, builder.redirectError());
        assertFalse(builder.redirectErrorStream());
        Process process = builder.start();
        try {
            assertTrue(process.waitFor(5, TimeUnit.SECONDS));
            assertEquals(0, process.exitValue());
            assertEquals("--no-sandbox\n--sid\nsession with spaces\n--url\n"
                            + "http://127.0.0.1/test?a=1&b=2\n[captchaResult]local-test\n",
                    new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
        } finally {
            process.destroyForcibly();
        }
    }

    @Test
    void otherPlatformsKeepTheirExistingLaunchArguments() {
        Path executable = directory.resolve("dark_backpage");
        assertEquals(List.of(executable.toAbsolutePath().toString(), "--captcha", "site-key"),
                BackpageTask.browserBuilder(executable, false, "--captcha", "site-key").command());
    }
}
