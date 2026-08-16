package com.github.manolo8.darkbot.utils;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.jar.JarFile;

/**
 * Development-only AuthAPI implementation.
 *
 * Used for testing plugins developed locally without depending on
 * external Discord authentication.
 */
public final class DevAuthAPI implements AuthAPI {

    public enum State {
        ANONYMOUS,
        AUTHENTICATED,
        DONOR
    }

    private final State state;
    private final String authId;

    public DevAuthAPI(State state, String authId) {
        this.state = state;
        this.authId = authId;

        System.out.println(
                "[DEV-AUTH] Enabled: state=" + state +
                ", authId=" + authId
        );
    }

    public static DevAuthAPI fromSystemProperties() {
        String value = System.getProperty(
                "darkbot.devAuth",
                "authenticated"
        );

        State state;

        switch (value.toLowerCase()) {
            case "anonymous":
                state = State.ANONYMOUS;
                break;

            case "authenticated":
                state = State.AUTHENTICATED;
                break;

            case "donor":
                state = State.DONOR;
                break;

            default:
                throw new IllegalArgumentException(
                        "Unknown darkbot.devAuth state: " + value
                );
        }

        String authId = System.getProperty(
                "darkbot.devAuthId",
                "local-development"
        );

        return new DevAuthAPI(state, authId);
    }

    @Override
    public void setupAuth() {
        System.out.println("[DEV-AUTH] setupAuth()");
    }

    @Override
    public boolean isAuthenticated() {
        return state != State.ANONYMOUS;
    }

    @Override
    public boolean isDonor() {
        return state == State.DONOR;
    }

    @Override
    public boolean requireDonor() {
        return isDonor();
    }

    @Override
    @Nullable
    public String getAuthId() {
        if (!isAuthenticated()) {
            return null;
        }

        return authId;
    }

    @Override
    public Boolean checkPluginJarSignature(JarFile jarFile)
            throws IOException {

        /*
         * Auth-state simulation is intentionally independent from
         * plugin signature validation.
         */
        return null;
    }
}
