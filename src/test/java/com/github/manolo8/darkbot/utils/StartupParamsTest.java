package com.github.manolo8.darkbot.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StartupParamsTest {
    @Test
    void savedLoginDoesNotConsumeStartOrHideOptions() throws Exception {
        StartupParams params = new StartupParams(new String[]{"--login-saved", "--start", "--hide"});
        assertTrue(params.has(StartupParams.LaunchArg.LOGIN_SAVED));
        assertTrue(params.getAutoStart());
        assertTrue(params.getAutoHide());
        assertFalse(params.getAutoLogin());
    }

    @Test
    void directLaunchKeepsManualLoginAndVisibleViewer() throws Exception {
        StartupParams params = new StartupParams(new String[0]);
        assertFalse(params.has(StartupParams.LaunchArg.LOGIN_SAVED));
        assertFalse(params.getAutoHide());
    }
}
