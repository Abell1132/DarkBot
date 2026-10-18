package com.github.manolo8.darkbot.core.api;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.backpage.BackpageManager;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.utils.StartupParams;
import eu.darkbot.util.Timer;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

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
        try {
            GameAPI.Handler handler = mock(GameAPI.Handler.class);
            GameAPIImpl<?, ?, ?, ?, ?, ?> api = new GameAPIImpl<>(
                    new StartupParams(new String[]{"--hide"}), mock(GameAPI.Window.class), handler,
                    mock(GameAPI.Memory.class), mock(GameAPI.ExtraMemoryReader.class),
                    mock(GameAPI.Interaction.class), mock(GameAPI.DirectInteraction.class));
            when(handler.isValid()).thenReturn(true);
            Field timerField = GameAPIImpl.class.getDeclaredField("becomeValidTimer");
            timerField.setAccessible(true);
            ((Timer) timerField.get(api)).activate(-1);

            assertTrue(api.isValid(), "Flash may be valid before game/window initialization");
            verify(handler, never()).setMinimized(true);
            api.tick();
            verify(handler).setMinimized(true);
            api.setVisible(true, true);
            api.tick();
            verify(handler, times(1)).setMinimized(true);
            verify(handler).setMinimized(false);
        } finally {
            Main.INSTANCE = previous;
        }
    }
}
