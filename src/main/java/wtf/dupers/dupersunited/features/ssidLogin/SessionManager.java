package wtf.dupers.dupersunited.features.ssidLogin;

import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.resource.SplashTextResourceSupplier;
import net.minecraft.client.session.ProfileKeys;
import net.minecraft.client.session.report.AbuseReportContext;
import net.minecraft.client.session.report.ReporterEnvironment;
import net.minecraft.util.Util;
import wtf.dupers.dupersunited.MainClient;
import wtf.dupers.dupersunited.mixin.accessor.MinecraftClientAccessor;
import wtf.dupers.dupersunited.modules.render.NickModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SessionManager {
    public static Session originalSession;

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
        isSessionValid = null;
        hasValidationStarted = false;

        MinecraftClientAccessor client = (MinecraftClientAccessor) MinecraftClient.getInstance();
        client.dupersunited$setSession(session);
        client.dupersunited$setGameProfileFuture(CompletableFuture.supplyAsync(() ->
            MinecraftClient.getInstance().getApiServices().sessionService().fetchProfile(session.getUuidOrNull(), true),
            Util.getDownloadWorkerExecutor()));
        client.dupersunited$setSplashTextLoader(new SplashTextResourceSupplier(session));
        UserApiService userApiService = new YggdrasilAuthenticationService(MinecraftClient.getInstance().getNetworkProxy()).createUserApiService(session.getAccessToken());
        client.dupersunited$setUserApiService(userApiService);
        client.dupersunited$setSocialInteractionsManager(new SocialInteractionsManager(MinecraftClient.getInstance(), userApiService));
        client.dupersunited$setProfileKeys(ProfileKeys.create(userApiService, session, FabricLoader.getInstance().getGameDir()));
        client.dupersunited$setAbuseReportContext(AbuseReportContext.create(ReporterEnvironment.ofIntegratedServer(), userApiService));
    }
}