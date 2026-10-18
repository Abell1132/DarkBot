package com.github.manolo8.darkbot.core.api;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.backpage.BackpageManager;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.gui.MainGui;
import com.github.manolo8.darkbot.utils.StartupParams;
import eu.darkbot.util.Timer;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import javax.swing.SwingUtilities;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AutoHideTest {
    @Test
    void hidesAfterGameInitializationAndDoesNotUndoManualShow() throws Exception {
        Main previous = Main.INSTANCE;
        Main main = mock(Main.class);
        main.config = new Config();
        main.config.BOT_SETTINGS.API_CONFIG.FULLY_HIDE_API = true;
        Field backpageField = Main.class.getField("backpage");
        backpageField.setAccessible(true);
        backpageField.set(main, mock(BackpageManager.class));
        Main.INSTANCE = main;
        MainGui gui = mock(MainGui.class);
        when(main.getGui()).thenReturn(gui);
        try {
            GameAPI.Handler handler = mock(GameAPI.Handler.class);
            GameAPIImpl<?, ?, ?, ?, ?, ?> api = new GameAPIImpl<>(
                    new StartupParams(new String[]{"--hide"}), mock(GameAPI.Window.class), handler,
                    mock(GameAPI.Memory.class), mock(GameAPI.ExtraMemoryReader.class),
                    mock(GameAPI.Interaction.class), mock(GameAPI.DirectInteraction.class), Capability.INITIALLY_SHOWN);
            doAnswer(invocation -> {
                api.setVisible(false, true);
                return null;
            }).when(gui).hideViewerAfterStartup();
            assertTrue(api.isInitiallyShown(), "The eye button must start in the visible state");
            when(handler.isValid()).thenReturn(true);
            Field timerField = GameAPIImpl.class.getDeclaredField("becomeValidTimer");
            timerField.setAccessible(true);
            ((Timer) timerField.get(api)).activate(-1);

            assertTrue(api.isValid(), "Flash may be valid before game/window initialization");
            verify(handler, never()).setMinimized(true);
            api.tick();
            assertTrue(api.autoHideTimer.getRemainingFuse() > 10_000, "Give the initialized viewer a grace period");
            api.tick();
            verify(gui, never()).hideViewerAfterStartup();
            when(handler.isValid()).thenReturn(false);
            assertFalse(api.isValid());
            assertFalse(api.autoHideTimer.isArmed(), "A loading interruption restarts the grace period");
            api.tick();
            api.autoHideTimer.activate(-1);
            api.tick();
            SwingUtilities.invokeAndWait(() -> {});
            verify(gui).hideViewerAfterStartup();
            verify(handler).setMinimized(true);
            api.setVisible(true, true);
            api.tick();
            SwingUtilities.invokeAndWait(() -> {});
            verify(gui, times(1)).hideViewerAfterStartup();
            verify(handler, times(1)).setMinimized(true);
            verify(handler).setMinimized(false);
        } finally {
            Main.INSTANCE = previous;
        }
    }
}
