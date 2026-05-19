package wtf.dupers.dupersunited.features.ssidLogin;

import wtf.dupers.dupersunited.MainClient;
import wtf.dupers.dupersunited.modules.render.NickModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;

import java.util.Optional;
import java.util.UUID;

public class SessionManager {
    public static Session originalSession;
    public static Session currentSession;
    public static boolean overrideSession;

    public static Boolean isSessionValid = null;
    public static boolean hasValidationStarted;

    public static void restoreSession() {
        NickModule mod = MainClient.MODULE_MANAGER.getModule(NickModule.class);
        if (mod != null && mod.isEnabled()) mod.username = originalSession.getUsername();
        setSession(SessionManager.originalSession);
    }

    public static boolean isSessionValid() {
        return isSessionValid != null && isSessionValid;
    }

    public static Session getSession() {
        return MinecraftClient.getInstance().getSession();
    }

    public static String getUsername() {
        return MinecraftClient.getInstance().getSession().getUsername();
    }

    public static Session createSession(String username, String uuidString, String ssid) {
        if (uuidString.length() == 32) {
            uuidString =
                    uuidString.substring(0, 8) + "-" +
                            uuidString.substring(8, 12) + "-" +
                            uuidString.substring(12, 16) + "-" +
                            uuidString.substring(16, 20) + "-" +
                            uuidString.substring(20, 32);
        }

        //update username in nick idt this is very smart but like wtv bro
        NickModule mod = MainClient.MODULE_MANAGER.getModule(NickModule.class);
        if (mod != null && mod.isEnabled()) mod.username = username;
        Session current = MinecraftClient.getInstance().getSession();
        Optional<String> xuid = current.getXuid();
        Optional<String> clientId = current.getClientId();

        return new Session(
                username,
                UUID.fromString(uuidString),
                ssid,
                xuid,
                clientId
        );
    }

    public static Session createSession(String username, UUID uuid, String ssid) {
        Session current = MinecraftClient.getInstance().getSession();

        return new Session(
                username,
                uuid,
                ssid,
                current.getXuid(),
                current.getClientId()
        );
    }

    public static void setSession(Session session) {
        currentSession = session;
        isSessionValid = null;
        hasValidationStarted = false;
    }
}