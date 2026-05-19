package wtf.dupers.dupersunited.features.ssidLogin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import wtf.dupers.dupersunited.MainClient;
import net.minecraft.client.MinecraftClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

public class SessionAPI {
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    public static String[] getProfileInfo(String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://api.minecraftservices.com/minecraft/profile")).header("Authorization", "Bearer " + token).GET().build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return null;
            }

            JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();

            if (jsonObject == null || !jsonObject.has("name") || !jsonObject.has("id")) {
                return null;
            }

            String IGN = jsonObject.get("name").getAsString();
            String UUID = jsonObject.get("id").getAsString();
            return new String[] { IGN, UUID };
        } catch (Exception e) {
            return null;
        }
    }

    public static Boolean validateSession(String token) {
        try {
            String[] profileInfo = getProfileInfo(token);
            if (profileInfo == null || profileInfo.length < 2) {
                return false;
            }

            String ign = profileInfo[0];
            String uuidString = profileInfo[1];

            if (uuidString.length() == 32) {
                uuidString =
                        uuidString.substring(0, 8) + "-" +
                                uuidString.substring(8, 12) + "-" +
                                uuidString.substring(12, 16) + "-" +
                                uuidString.substring(16, 20) + "-" +
                                uuidString.substring(20, 32);
            }

            UUID uuid = UUID.fromString(uuidString);

            var session = MinecraftClient.getInstance().getSession();

            return ign.equalsIgnoreCase(session.getUsername())
                    && uuid.equals(session.getUuidOrNull());

        } catch (Exception e) {
            MainClient.LOGGER.error("something went wrong with session api", e);
            return false;
        }
    }

    public static int changeSkin(String url, String token) {
        try {
            String jsonString = String.format("{ \"variant\": \"classic\", \"url\": \"%s\"}", new Object[] { url });
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://api.minecraftservices.com/minecraft/profile/skins")).header("Authorization", "Bearer " + token).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(jsonString)).build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode();
        } catch (Exception e) {
            return -1;
        }
    }

    public static int changeName(String newName, String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://api.minecraftservices.com/minecraft/profile/name/" + newName)).header("Authorization", "Bearer " + token).PUT(HttpRequest.BodyPublishers.ofString("")).build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode();
        } catch (Exception e) {
            return -1;
        }
    }
}

