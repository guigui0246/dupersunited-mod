package wtf.dupers.dupersunited.features;

import wtf.dupers.dupersunited.commands.MainCommand;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.HashMap;
import java.util.Map;

public class GhostBlock {
    private static final Map<BlockPos, BlockState> ghosts = new HashMap<>();
    private static int blockAmount;

    public static void deleteBlock() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }

        Vec3d start = client.player.getCameraPosVec(1.0F);
        Vec3d direction = client.player.getRotationVec(1.0F);
        Vec3d end = start.add(direction.multiply(1000.0));

        BlockHitResult hit = client.world.raycast(new RaycastContext(
                start,
                end,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                client.player
        ));

        if (hit.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockPos pos = hit.getBlockPos();
        BlockState state = client.world.getBlockState(pos);

        ghosts.put(pos, state);
        client.world.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);
        MainCommand.sendMessage("Placed ghost block at X: " + pos.getX() + " Y: " + pos.getY() + " Z: " + pos.getZ() + ".", true);
    }

    public static void replaceBlock(BlockState blockState) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }

        Vec3d start = client.player.getCameraPosVec(1.0F);
        Vec3d direction = client.player.getRotationVec(1.0F);
        Vec3d end = start.add(direction.multiply(1000.0));

        BlockHitResult hit = client.world.raycast(new RaycastContext(
                start,
                end,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                client.player
        ));

        if (hit.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockPos pos = hit.getBlockPos();
        BlockState state = client.world.getBlockState(pos);

        ghosts.put(pos, state);
        client.world.setBlockState(pos, blockState, 11);
        MainCommand.sendMessage("Replaced block at X: " + pos.getX() + " Y: " + pos.getY() + " Z: " + pos.getZ() + "with " + blockState.getBlock().getName() + ".", true);
    }

    public static void restoreGhosts() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            return;
        }

        for (Map.Entry<BlockPos, BlockState> entry : ghosts.entrySet()) {
            client.world.setBlockState(entry.getKey(), entry.getValue(), 11);
            blockAmount++;
        }
        ghosts.clear();
        MainCommand.sendMessage("Restored "  + blockAmount + " ghost blocks.", true);
        blockAmount = 0;
    }

    public static void clearGhosts() {
        ghosts.clear();
    }

}