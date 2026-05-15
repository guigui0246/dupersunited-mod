package wtf.dupers.dupersunited.features.auth;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.exceptions.AuthenticationException;
import wtf.dupers.dupersunited.MainClient;
import wtf.dupers.dupersunited.features.CapeManager;
import wtf.dupers.dupersunited.features.ServerInviteManager;
import wtf.dupers.dupersunited.features.ssidLogin.SessionAPI;
import wtf.dupers.dupersunited.features.ssidLogin.SessionManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;
import net.minecraft.text.Text;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class AuthManager {
    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final String PUBLIC_WS_URL = "wss://dupersunited-server.dupers.wtf/ws/public";
    private static final String PUBLIC_API_BASE_URL = "https://dupersunited-server.dupers.wtf";
    private static final long RECONNECT_DELAY_MS = 3_000L;

    private static volatile AuthPhase phase = AuthPhase.CONNECTING_SOCKET;
    private static volatile String statusLine = UiText.CONNECTING_SOCKET.status;
    private static volatile String detailLine = UiText.CONNECTING_SOCKET.detail;

    private static volatile boolean initialized;
    private static volatile boolean socketConnected;
    private static volatile boolean accountVerified;
    private static volatile boolean socketConnecting;

    private static volatile int generation;
    private static volatile long reconnectAtMs = -1L;

    private static volatile WebSocket socket;
    private static volatile MinecraftAccount linkedAccount;
    private static final CapeCatalog EMPTY_CAPE_CATALOG = new CapeCatalog(Map.of(), null);
    private static volatile CapeCatalog capeCatalog = EMPTY_CAPE_CATALOG;
    private static volatile String verifiedSessionFingerprint = "";
    private static volatile String requestedSessionFingerprint = "";

    private AuthManager() {}

    public static void init() {
        if (initialized) {
            return;
        }

        initialized = true;
        resetAccountState();
        connectSocket();
    }

    public static void onTick() {
        if (!initialized) {
            return;
        }

        long reconnectAt = reconnectAtMs;
        if (reconnectAt > 0L && System.currentTimeMillis() >= reconnectAt) {
            reconnectAtMs = -1L;
            connectSocket();
        }

        detectSessionSwitch();
    }

    public static void onMinecraftAccountChanged() {
        if (!initialized) {
            return;
        }

        resetAccountState();
        syncCapeModuleState();

        if (socketConnected) {
            ensureSessionValidatedAndRetry(true);
        }
    }

    public static boolean isSocketConnected() {
        return socketConnected;
    }

    public static boolean canUseCapes() {
        return socketConnected && accountVerified && linkedAccount != null;
    }

    public static String getStatusLine() {
        return statusLine;
    }

    public static String getDetailLine() {
        return detailLine;
    }

    public static boolean isLoginBusy() {
        return phase == AuthPhase.CONNECTING_SOCKET || phase == AuthPhase.WAITING_FOR_ACCOUNT;
    }

    public static MinecraftAccount getLinkedAccount() {
        return linkedAccount;
    }

    public static CapeCatalog getCapeCatalog() {
        return capeCatalog == null ? EMPTY_CAPE_CATALOG : capeCatalog;
    }

    public static String getApiBaseUrl() {
        return PUBLIC_API_BASE_URL;
    }

    public static String getWsBaseUrl() {
        return PUBLIC_WS_URL;
    }

    public static void retry() {
        init();

        if (!socketConnected && !socketConnecting) {
            connectSocket();
            return;
        }

        if (!accountVerified && ensureSessionValidatedAndRetry(false)) {
            retryAccountVerification();
            return;
        }

        if (canUseCapes()) {
            requestCapeList();
        }
    }

    public static void retryAccountVerification() {
        if (!socketConnected) {
            connectSocket();
            return;
        }

        if (!ensureSessionValidatedAndRetry(false)) {
            setPhase(UiText.READY_UNSUPPORTED_SESSION);
            syncProtectedState();
            return;
        }

        requestedSessionFingerprint = getSessionFingerprint();
        linkedAccount = null;
        accountVerified = false;
        capeCatalog = EMPTY_CAPE_CATALOG;

        setPhase(UiText.WAITING_FOR_ACCOUNT_REQUEST);
        syncProtectedState();
        syncCapeModuleState();
        sendSocketMessage(buildMessage("switch_account"));
    }

    public static void requestCapeList() {
        if (!canUseCapes()) {
            return;
        }

        sendSocketMessage(buildMessage("list_capes"));
    }

    public static void pickCape(String capeKey) {
        if (!canUseCapes()) {
            return;
        }

        JsonObject payload = buildMessage("pick_cape");
        if (capeKey == null || capeKey.isBlank() || "disabled".equalsIgnoreCase(capeKey)) {
            payload.add("capeKey", JsonNull.INSTANCE);
        } else {
            payload.addProperty("capeKey", capeKey);
        }
        sendSocketMessage(payload);
    }

    private static void connectSocket() {
        if (socketConnected || socketConnecting) {
            return;
        }

        int gen = ++generation;

        closeSocket();
        socketConnected = false;
        socketConnecting = true;
        reconnectAtMs = -1L;
        resetAccountState();

        setPhase(UiText.CONNECTING_SOCKET);
        syncProtectedState();

        HTTP_CLIENT.newWebSocketBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .buildAsync(URI.create(PUBLIC_WS_URL), new AddonSocketListener(gen))
                .exceptionally(throwable -> {
                    handleSocketDisconnect(gen, null, "WebSocket connection failed: " + simplifyError(throwable));
                    return null;
                });
    }

    private static void handleSocketOpen(int gen, WebSocket webSocket) {
        if (gen != generation) {
            webSocket.abort();
            return;
        }

        socket = webSocket;
        socketConnected = true;
        socketConnecting = false;
        reconnectAtMs = -1L;

        setPhase(UiText.READY_NO_ACCOUNT);
        syncProtectedState();
        ensureSessionValidatedAndRetry(true);
    }

    private static void handleSocketMessage(int gen, WebSocket webSocket, String rawMessage) {
        if (gen != generation || socket != webSocket) {
            return;
        }

        JsonObject root;
        try {
            root = JsonParser.parseString(rawMessage).getAsJsonObject();
        } catch (Exception ignored) {
            return;
        }

        String type = getString(root, "type");
        if (type == null) {
            return;
        }

        switch (type) {
            case "server_id" -> startMinecraftVerification(root);
            case "account_ok" -> {
                linkedAccount = parseAccount(getObject(root, "account"));
                accountVerified = linkedAccount != null;
                verifiedSessionFingerprint = getSessionFingerprint();
                requestedSessionFingerprint = verifiedSessionFingerprint;
                setPhase(linkedAccount == null ? UiText.READY_NO_ACCOUNT : UiText.ADDON_READY,
                        linkedAccount == null
                                ? UiText.READY_NO_ACCOUNT.detail
                                : "Linked Minecraft account: " + linkedAccount.username());
                requestCapeList();
                syncProtectedState();
                syncCapeModuleState();
            }
            case "capes" -> {
                linkedAccount = parseAccount(getObject(root, "account"));
                accountVerified = linkedAccount != null;
                capeCatalog = parseCapeCatalog(getArray(root, "capes"));
                syncCapeModuleState();
            }
            case "broadcast" -> handleBroadcast(root);
            case "server_invite" -> handleServerInvite(root);
            case "error" -> handleSocketError(root);
            default -> {
            }
        }
    }

    private static void startMinecraftVerification(JsonObject root) {
        String serverId = getString(root, "serverId");
        if (serverId == null || serverId.isBlank()) {
            setPhase(UiText.MINECRAFT_VERIFY_FAILED, "Backend sent an empty serverId.");
            syncProtectedState();
            return;
        }

        requestedSessionFingerprint = getSessionFingerprint();
        linkedAccount = null;
        accountVerified = false;
        capeCatalog = EMPTY_CAPE_CATALOG;

        setPhase(UiText.WAITING_FOR_ACCOUNT_REQUEST, "Joining Mojang session server with challenge " + serverId + "...");
        syncProtectedState();
        syncCapeModuleState();

        CompletableFuture.runAsync(() -> joinServerSession(serverId))
                .exceptionally(throwable -> {
                    setPhase(UiText.MINECRAFT_VERIFY_FAILED, simplifyError(throwable));
                    syncProtectedState();
                    return null;
                });
    }

    private static void joinServerSession(String serverId) {
        Session session = MinecraftClient.getInstance().getSession();
        UUID uuid = session.getUuidOrNull();
        if (uuid == null) {
            setPhase(UiText.READY_UNSUPPORTED_SESSION);
            syncProtectedState();
            return;
        }

        String accessToken = session.getAccessToken();
        if (accessToken == null || accessToken.isBlank()) {
            setPhase(UiText.READY_UNSUPPORTED_SESSION);
            syncProtectedState();
            return;
        }

        try {
            MinecraftClient.getInstance().getApiServices().sessionService().joinServer(uuid, accessToken, serverId);
        } catch (AuthenticationException exception) {
            setPhase(UiText.READY_LINK_FAILED);
            syncProtectedState();
            return;
        }

        setPhase(UiText.WAITING_FOR_ACCOUNT_CONFIRMATION);
        syncProtectedState();

        JsonObject joined = buildMessage("joined");
        joined.addProperty("username", session.getUsername());
        sendSocketMessage(joined);
    }

    private static void handleBroadcast(JsonObject root) {
        String message = getNullableString(root, "msg");
        if (message == null || message.isBlank()) {
            return;
        }

        MinecraftClient.getInstance().execute(() ->
                MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(
                        Text.literal("\n\n§b§lDU Broadcast:§r\n§f" + message + "\n\n")
                )
        );
    }

    private static void handleServerInvite(JsonObject root) {
        String ip = getNullableString(root, "ip");
        String inviter = getNullableString(root, "from");
        String sentAt = getNullableString(root, "sentAt");
        if (ip == null || inviter == null) {
            return;
        }

        MinecraftClient.getInstance().execute(() -> ServerInviteManager.receiveInvite(ip, inviter, sentAt));
    }

    private static void handleSocketError(JsonObject root) {
        String code = getNullableString(root, "code");
        String message = Objects.requireNonNullElse(getString(root, "message"), "Unknown backend error.");

        if ("ACCOUNT_REQUIRED".equals(code)
                || "ACCOUNT_JOIN_MISSING".equals(code)
                || "ACCOUNT_VERIFY_FAILED".equals(code)
                || "ACCOUNT_LINK_PENDING".equals(code)) {
            resetAccountState();
            setPhase(UiText.READY_ACCOUNT_REQUIRED, message);
            syncProtectedState();
            syncCapeModuleState();
            return;
        }

        if ("CAPE_SWITCH_FAILED".equals(code)) {
            setPhase(UiText.BACKEND_REJECTED, message);
            syncProtectedState();
            return;
        }

        if ("AUTH_UNSUPPORTED".equals(code) || "PRIVATE_SOCKET_REQUIRED".equals(code)) {
            setPhase(UiText.BACKEND_REJECTED, message);
            syncProtectedState();
            return;
        }

        setPhase(UiText.BACKEND_REJECTED, message);
        syncProtectedState();
    }

    private static void handleSocketDisconnect(int gen, WebSocket webSocket, String reason) {
        if (gen != generation) {
            return;
        }
        if (webSocket != null && socket != webSocket) {
            return;
        }

        socket = null;
        socketConnected = false;
        socketConnecting = false;
        resetAccountState();
        syncCapeModuleState();

        reconnectAtMs = System.currentTimeMillis() + RECONNECT_DELAY_MS;
        setPhase(UiText.DISCONNECTED, reason + " Retrying shortly...");
        syncProtectedState();
    }

    private static void detectSessionSwitch() {
        if (!socketConnected || !accountVerified) {
            return;
        }

        if (!ensureSessionValidatedAndRetry(false)) {
            return;
        }

        String currentFingerprint = getSessionFingerprint();
        if (currentFingerprint.isBlank()) {
            return;
        }
        if (currentFingerprint.equals(verifiedSessionFingerprint) || currentFingerprint.equals(requestedSessionFingerprint)) {
            return;
        }

        requestedSessionFingerprint = currentFingerprint;
        linkedAccount = null;
        accountVerified = false;
        capeCatalog = EMPTY_CAPE_CATALOG;

        setPhase(UiText.WAITING_FOR_ACCOUNT_SWITCH);
        syncProtectedState();
        syncCapeModuleState();
        sendSocketMessage(buildMessage("switch_account"));
    }

    private static boolean ensureSessionValidatedAndRetry(boolean retryAfterValidation) {
        if (SessionManager.isSessionValid != null) {
            if (SessionManager.isSessionValid && retryAfterValidation) {
                retryAccountVerificationIfReady();
            }
            return SessionManager.isSessionValid;
        }

        if (!SessionManager.hasValidationStarted) {
            SessionManager.hasValidationStarted = true;

            String sessionFingerprint = getSessionFingerprint();
            String accessToken = MinecraftClient.getInstance().getSession().getAccessToken();

            Thread.ofVirtual().start(() -> {
                boolean valid = accessToken != null && !accessToken.isBlank() && SessionAPI.validateSession(accessToken);

                MinecraftClient.getInstance().execute(() -> {
                    if (!Objects.equals(sessionFingerprint, getSessionFingerprint())) {
                        SessionManager.hasValidationStarted = false;
                        ensureSessionValidatedAndRetry(retryAfterValidation);
                        return;
                    }

                    SessionManager.isSessionValid = valid;
                    SessionManager.hasValidationStarted = false;

                    if (valid && retryAfterValidation) {
                        retryAccountVerificationIfReady();
                    }
                });
            });
        }

        return false;
    }

    private static void retryAccountVerificationIfReady() {
        if (socketConnected && !accountVerified) {
            retryAccountVerification();
        }
    }

    private static void resetAccountState() {
        accountVerified = false;
        linkedAccount = null;
        capeCatalog = EMPTY_CAPE_CATALOG;
        verifiedSessionFingerprint = "";
        requestedSessionFingerprint = "";
    }

    private static void closeSocket() {
        WebSocket previous = socket;
        socket = null;
        if (previous != null) {
            try {
                previous.sendClose(WebSocket.NORMAL_CLOSURE, "reset");
            } catch (Exception ignored) {
                previous.abort();
            }
        }
    }

    private static void syncProtectedState() {
        if (!canUseCapes()) {
            CapeManager.disableCape();
        }
    }

    private static void syncCapeModuleState() {
        if (!canUseCapes()) {
            CapeManager.disableCape();
        }
        if (MainClient.MODULE_MANAGER == null) {
            return;
        }
    }

    private static void setPhase(UiText text) {
        setPhase(text, text.detail);
    }

    private static void setPhase(UiText text, String detail) {
        phase = text.phase;
        statusLine = text.status;
        detailLine = detail;
    }

    private static JsonObject buildMessage(String type) {
        JsonObject object = new JsonObject();
        object.addProperty("type", type);
        return object;
    }

    private static void sendSocketMessage(JsonObject payload) {
        WebSocket activeSocket = socket;
        if (activeSocket == null) {
            return;
        }

        activeSocket.sendText(GSON.toJson(payload), true)
                .exceptionally(throwable -> {
                    handleSocketDisconnect(generation, activeSocket, "Failed to send a WebSocket message.");
                    return null;
                });
    }

    private static MinecraftAccount parseAccount(JsonObject object) {
        if (object == null) {
            return null;
        }

        String uuid = firstNonBlank(getNullableString(object, "uuid"), getNullableString(object, "id"));
        String username = firstNonBlank(getNullableString(object, "username"), getNullableString(object, "name"));
        if ((uuid == null || uuid.isBlank()) && (username == null || username.isBlank())) {
            return null;
        }
        return new MinecraftAccount(uuid, username);
    }

    private static CapeCatalog parseCapeCatalog(JsonArray capes) {
        if (capes == null) {
            return EMPTY_CAPE_CATALOG;
        }

        Map<String, String> textureUrls = new LinkedHashMap<>();
        String selectedKey = null;
        for (JsonElement element : capes) {
            if (element == null || element.isJsonNull() || !element.isJsonObject()) {
                continue;
            }

            JsonObject object = element.getAsJsonObject();
            String key = getNullableString(object, "capeKey");
            String textureUrl = getNullableString(object, "texture");
            String state = getNullableString(object, "state");
            if (key == null || key.isBlank()) {
                continue;
            }

            if ("active".equalsIgnoreCase(state)) {
                selectedKey = key;
            }

            if (textureUrl != null && !textureUrl.isBlank()) {
                textureUrls.putIfAbsent(key, textureUrl);
            }
        }
        return new CapeCatalog(Map.copyOf(textureUrls), selectedKey);
    }

    private static String getSessionFingerprint() {
        Session session = MinecraftClient.getInstance().getSession();
        UUID uuid = session.getUuidOrNull();
        if (uuid == null) {
            return session.getUsername();
        }
        return session.getUsername() + "|" + uuid;
    }

    private static String simplifyError(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getMessage() == null || cause.getMessage().isBlank()
                ? cause.getClass().getSimpleName()
                : cause.getMessage();
    }

    private static String getString(JsonObject object, String key) {
        if (object == null || !object.has(key) || object.get(key).isJsonNull()) {
            return null;
        }
        try {
            return object.get(key).getAsString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static JsonObject getObject(JsonObject object, String key) {
        if (object == null || !object.has(key)) {
            return null;
        }
        JsonElement element = object.get(key);
        if (element == null || element.isJsonNull() || !element.isJsonObject()) {
            return null;
        }
        return element.getAsJsonObject();
    }

    private static JsonArray getArray(JsonObject object, String key) {
        if (object == null || !object.has(key)) {
            return null;
        }
        JsonElement element = object.get(key);
        if (element == null || element.isJsonNull() || !element.isJsonArray()) {
            return null;
        }
        return element.getAsJsonArray();
    }

    private static String getNullableString(JsonObject object, String key) {
        String value = getString(object, key);
        return value == null || value.isBlank() ? null : value;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    public record MinecraftAccount(String uuid, String username) {}

    public record CapeCatalog(Map<String, String> textureUrls, String selected) {}

    public enum AuthPhase {
        CONNECTING_SOCKET,
        WAITING_FOR_ACCOUNT,
        READY,
        DISCONNECTED,
        ERROR
    }

    private enum UiText {
        CONNECTING_SOCKET(AuthPhase.CONNECTING_SOCKET, "Connecting to backend", "Opening the WebSocket session..."),
        WAITING_FOR_ACCOUNT_REQUEST(AuthPhase.WAITING_FOR_ACCOUNT, "Waiting for Minecraft account verification", "Requesting a new verification challenge..."),
        WAITING_FOR_ACCOUNT_CONFIRMATION(AuthPhase.WAITING_FOR_ACCOUNT, "Waiting for Minecraft account verification", "Mojang session join succeeded. Waiting for backend confirmation..."),
        WAITING_FOR_ACCOUNT_SWITCH(AuthPhase.WAITING_FOR_ACCOUNT, "Waiting for Minecraft account verification", "Detected a Minecraft account switch. Reverifying..."),
        ADDON_READY(AuthPhase.READY, "Addon ready", ""),
        READY_NO_ACCOUNT(AuthPhase.READY, "Addon ready", "Verify a Minecraft account to access cape management."),
        READY_UNSUPPORTED_SESSION(AuthPhase.READY, "Addon ready", "This Minecraft session cannot be linked. Please use a valid Minecraft account."),
        READY_LINK_FAILED(AuthPhase.READY, "Addon ready", "Mojang session join failed for this account. Please try again later."),
        READY_ACCOUNT_REQUIRED(AuthPhase.READY, "Addon ready", "Verify a Minecraft account before using cape management."),
        DISCONNECTED(AuthPhase.DISCONNECTED, "Backend disconnected", "Retrying shortly..."),
        MINECRAFT_VERIFY_FAILED(AuthPhase.ERROR, "Minecraft verification failed", "Minecraft account verification failed."),
        BACKEND_REJECTED(AuthPhase.ERROR, "Backend rejected the current step", "Unknown backend error.");

        private final AuthPhase phase;
        private final String status;
        private final String detail;

        UiText(AuthPhase phase, String status, String detail) {
            this.phase = phase;
            this.status = status;
            this.detail = detail;
        }
    }

    private static final class AddonSocketListener implements WebSocket.Listener {
        private final int gen;
        private final StringBuilder textBuffer = new StringBuilder();

        private AddonSocketListener(int gen) {
            this.gen = gen;
        }

        @Override
        public void onOpen(WebSocket webSocket) {
            webSocket.request(1);
            handleSocketOpen(gen, webSocket);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            textBuffer.append(data);
            if (last) {
                String message = textBuffer.toString();
                textBuffer.setLength(0);
                handleSocketMessage(gen, webSocket, message);
            }
            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            handleSocketDisconnect(gen, webSocket, reason == null || reason.isBlank() ? "WebSocket closed." : reason);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            handleSocketDisconnect(gen, webSocket, "WebSocket error: " + simplifyError(error));
        }
    }
}