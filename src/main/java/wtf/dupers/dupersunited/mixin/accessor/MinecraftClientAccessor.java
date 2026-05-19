package wtf.dupers.dupersunited.mixin.accessor;

import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.resource.SplashTextResourceSupplier;
import net.minecraft.client.session.ProfileKeys;
import net.minecraft.client.session.Session;
import net.minecraft.client.session.report.AbuseReportContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {
    @Mutable @Accessor("session")
    void dupersunited$setSession(Session session);

    @Mutable @Accessor("gameProfileFuture")
    void dupersunited$setGameProfileFuture(CompletableFuture<ProfileResult> future);

    @Mutable @Accessor("splashTextLoader")
    void dupersunited$setSplashTextLoader(SplashTextResourceSupplier splashTextLoader);

    @Mutable @Accessor("userApiService")
    void dupersunited$setUserApiService(UserApiService userApiService);

    @Mutable @Accessor("socialInteractionsManager")
    void dupersunited$setSocialInteractionsManager(SocialInteractionsManager socialInteractionsManager);

    @Mutable @Accessor("profileKeys")
    void dupersunited$setProfileKeys(ProfileKeys profileKeys);

    @Accessor("abuseReportContext")
    void dupersunited$setAbuseReportContext(AbuseReportContext abuseReportContext);
}
