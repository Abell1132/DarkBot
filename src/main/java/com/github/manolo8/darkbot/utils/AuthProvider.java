package com.github.manolo8.darkbot.utils;

import com.github.manolo8.darkbot.extensions.util.VerifierChecker;

public final class AuthProvider {

    private AuthProvider() {}

    public static AuthAPI get() {
        boolean devMode =
                Boolean.getBoolean("darkbot.devAuthEnabled");

        if (devMode) {
            System.out.println(
                    "[DEV-AUTH] Using local development AuthAPI"
            );

            return DevAuthAPI.fromSystemProperties();
        }

        return VerifierChecker.getAuthApi();
    }
}
