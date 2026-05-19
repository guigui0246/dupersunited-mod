//package wtf.dupers.dupersunited.mixin.entity;
//
//import net.fabricmc.api.EnvType;
//import net.fabricmc.api.Environment;
//import net.minecraft.client.network.AbstractClientPlayerEntity;
//import net.minecraft.client.network.PlayerListEntry;
//import net.minecraft.entity.player.SkinTextures;
//import net.minecraft.util.Identifier;
//import net.minecraft.util.AssetInfo;
//import org.jetbrains.annotations.Nullable;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//
//@Mixin(AbstractClientPlayerEntity.class)
//@Environment(EnvType.CLIENT)
//public abstract class AbstractClientPlayerEntityMixin {
//    @Shadow
//    @Nullable
//    protected abstract PlayerListEntry getPlayerListEntry();
//
//    @Inject(at = @At("RETURN"), method = "getSkin", cancellable = true)
//    private void getSkinTextures(CallbackInfoReturnable<SkinTextures> cir) {
//        PlayerListEntry entry = this.getPlayerListEntry();
//        if (entry == null) return;
//
//        Identifier cape = CapeManager.getProfile(entry.getProfile().id());
//        if (cape != null) {
//            SkinTextures textures = cir.getReturnValue();
//            cir.setReturnValue(new SkinTextures(
//                    textures.body(),
//                    new AssetInfo.TextureAssetInfo(cape, cape),
//                    new AssetInfo.TextureAssetInfo(cape, cape),
//                    textures.model(),
//                    textures.secure()
//            ));
//        }
//    }
//}
