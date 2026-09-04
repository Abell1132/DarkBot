package com.github.manolo8.darkbot.extensions.plugins;

import com.github.manolo8.darkbot.utils.LibSetup;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PluginUpdaterDevModeTest {
    @Test
    void devModeBlocksUpdatesBeforeUiOrPluginAccess() {
        String previous = System.getProperty("darkbot.devAuthEnabled");
        System.setProperty("darkbot.devAuthEnabled", "true");
        try {
            PluginUpdater updater = new PluginUpdater(null);
            assertDoesNotThrow(updater::scheduleUpdateChecker);
            assertDoesNotThrow(updater::checkUpdates);
            assertDoesNotThrow(updater::updateAll);
            assertDoesNotThrow(() -> updater.update(null));

            LibSetup.Lib lib = new LibSetup.Lib();
            lib.path = "plugins/updates/dev-test.jar";
            // No download URL: a guarded call must return before any network/file access.
            assertFalse(LibSetup.downloadLib(lib));
            assertFalse(LibSetup.downloadLib(lib,
                    Paths.get("plugins/old/../dev-test.jar").toAbsolutePath()));
        } finally {
            if (previous == null) System.clearProperty("darkbot.devAuthEnabled");
            else System.setProperty("darkbot.devAuthEnabled", previous);
        }
    }
}
