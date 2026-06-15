package wtf.dupers.dupersunited.modules.render;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import net.minecraft.registry.Registries;
import wtf.dupers.dupersunited.api.module.Category;
import wtf.dupers.dupersunited.api.module.Module;
import wtf.dupers.dupersunited.features.screens.BlockEspScreen;
import wtf.dupers.dupersunited.api.module.settings.BindSetting;
import wtf.dupers.dupersunited.api.module.settings.ButtonSetting;
import wtf.dupers.dupersunited.api.module.settings.IntSetting;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlockEspModule extends Module {

    private static BlockEspModule instance;
    public static final Set<Block> selectedBlocks = new ReferenceOpenHashSet<>();

    private final CopyOnWriteArrayList<RenderShape> renderShapes = new CopyOnWriteArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "BlockESP-Scanner");
        t.setDaemon(true);
        return t;
    });

    private volatile boolean scanning = false;
    private BlockPos lastScanCenter = null;
    private int ticksSinceScan = SCAN_INTERVAL;
    private int lastRange = -1;

    private static final int SCAN_INTERVAL = 60;
    private static final int MAX_RENDER = 50000;

    private final IntSetting range = register(new IntSetting("Range", 64, 16, 512));
    private final IntSetting red = register(new IntSetting("Red", 0, 0, 255));
    private final IntSetting green = register(new IntSetting("Green", 255, 0, 255));
    private final IntSetting blue = register(new IntSetting("Blue", 255, 0, 255));

    private final ButtonSetting selectBlocks = register(
            new ButtonSetting("SelectBlocks", this::openScreen)
    );

    private static final RenderPipeline ESP_LINES_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.RENDERTYPE_LINES_SNIPPET)
                    .withLocation(Identifier.of("dupersunited", "pipeline/esp_lines"))
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withDepthWrite(false)
                    .build()
    );

    public static final RenderLayer ESP_LINES = RenderLayer.of(
        "esp_lines",
        1536,
        ESP_LINES_PIPELINE,
        RenderLayer.MultiPhaseParameters.builder()
            .lineWidth(new RenderPhase.LineWidth(OptionalDouble.of(1.5)))
            .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
            .target(RenderPhase.ITEM_ENTITY_TARGET)
            .build(false)
    );

    public BlockEspModule() {
        super("BlockESP", "Outlines blocks through blocks.", Category.render);
        this.register(new BindSetting("Keybind", GLFW.GLFW_KEY_UNKNOWN).linkedTo(this));
        instance = this;
    }

    public static BlockEspModule getInstance() { return instance; }

    public void openScreen() {
        MinecraftClient.getInstance().setScreen(new BlockEspScreen());
    }

    @Override
    public JsonElement writeJson() {
        JsonObject object = (JsonObject) super.writeJson();
        JsonArray espBlocks = new JsonArray();
        for (Block block : selectedBlocks) espBlocks.add(Registries.BLOCK.getId(block).toString());
        object.add("selected-blocks", espBlocks);
        return object;
    }

    @Override
    public void readJson(JsonElement element) {
        super.readJson(element);
        if (element instanceof JsonObject object && object.has("selected-blocks")) {
            selectedBlocks.clear();
            for (JsonElement el : object.getAsJsonArray("selected-blocks")) {
                Registries.BLOCK.getEntry(Identifier.tryParse(el.getAsString())).ifPresent(entry -> selectedBlocks.add(entry.value()));
            }
        }
    }

    @Override
    public void onEnable() { invalidateCache(); }

    @Override
    public void onDisable() { renderShapes.clear(); }

    @Override
    public void onTick() {
        if (selectedBlocks.isEmpty()) { renderShapes.clear(); return; }

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) return;

        BlockPos playerPos = mc.player.getBlockPos();
        int currentRange = range.getValue();
        ticksSinceScan++;

        if (currentRange != lastRange) {
            lastRange = currentRange;
            invalidateCache();
        }

        boolean movedFar = lastScanCenter == null
                || Math.abs(playerPos.getX() - lastScanCenter.getX()) > 16
                || Math.abs(playerPos.getY() - lastScanCenter.getY()) > 16
                || Math.abs(playerPos.getZ() - lastScanCenter.getZ()) > 16;

        if ((ticksSinceScan >= SCAN_INTERVAL || movedFar) && !scanning) {
            ticksSinceScan = 0;
            lastScanCenter = playerPos.toImmutable();
            scheduleRescan(mc.world, playerPos.toImmutable(), currentRange);
        }
    }

    public void invalidateCache() {
        lastScanCenter = null;
        ticksSinceScan = SCAN_INTERVAL;
    }

    private void scheduleRescan(World world, BlockPos center, int r) {
        Set<Block> snapshot = new ReferenceOpenHashSet<>(selectedBlocks);
        scanning = true;

        executor.submit(() -> {
            try {
                List<RenderShape> found = new ArrayList<>();
                rescan(world, center, r, snapshot, found);

                renderShapes.clear();
                renderShapes.addAll(found);
            } finally {
                scanning = false;
            }
        });
    }

    /**
     * You don't have to know how this works, you just have to know that it works
     * @author Crosby
     */
    private void rescan(World world, BlockPos center, int r, Set<Block> snapshot, List<RenderShape> found) {
        int cr = Math.ceilDiv(r, 16);
        int ox = ChunkSectionPos.getSectionCoord(center.getX());
        int oz = ChunkSectionPos.getSectionCoord(center.getZ());

        int wMinY = Math.max(world.getBottomY(), center.getY() - r);
        int wMaxY = Math.min(world.getTopYInclusive(), center.getY() + r);
        int cMinY = ChunkSectionPos.getSectionCoord(wMinY);
        int cMaxY = ChunkSectionPos.getSectionCoord(wMaxY);

        for (int cx = ox - cr; cx <= ox + cr; cx++) {
            for (int cz = oz - cr; cz <= oz + cr; cz++) {
                WorldChunk chunk = world.getChunk(cx, cz);

                int minX = Math.max(ChunkSectionPos.getOffsetPos(cx, 0), center.getX() - r);
                int maxX = Math.min(ChunkSectionPos.getOffsetPos(cx, 15), center.getX() + r);
                int minZ = Math.max(ChunkSectionPos.getOffsetPos(cz, 0), center.getZ() - r);
                int maxZ = Math.min(ChunkSectionPos.getOffsetPos(cz, 15), center.getZ() + r);

                for (int cy = cMinY; cy <= cMaxY; cy++) {
                    ChunkSection section = chunk.getSection(chunk.sectionCoordToIndex(cy));

                    if (section.hasAny(state -> snapshot.contains(state.getBlock()))) {
                        int minY = Math.max(ChunkSectionPos.getOffsetPos(cy, 0), wMinY);
                        int maxY = Math.min(ChunkSectionPos.getOffsetPos(cy, 15), wMaxY);

                        for (int y = minY; y <= maxY; y++) {
                            for (int z = minZ; z <= maxZ; z++) {
                                for (int x = minX; x <= maxX; x++) {
                                    BlockState state = section.getBlockState(x & 15, y & 15, z & 15);
                                    if (snapshot.contains(state.getBlock())) {
                                        BlockPos pos = new BlockPos(x, y, z);
                                        VoxelShape shape = state.getOutlineShape(world, pos);
                                        found.add(new RenderShape(
                                            shape != VoxelShapes.fullCube() ? shape.asCuboid() : shape,
                                            pos
                                        ));
                                        if (found.size() >= MAX_RENDER) return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void onRender(MatrixStack matrices, VertexConsumerProvider consumers, Vec3d cameraPos) {
        if (!isEnabled() || renderShapes.isEmpty()) return;

        VertexConsumer lines = consumers.getBuffer(ESP_LINES);
        int count = 0;

        float r = red.getValue() / 255f;
        float g = green.getValue() / 255f;
        float b = blue.getValue() / 255f;

        for (RenderShape renderShape : renderShapes) {
            if (count++ >= MAX_RENDER) break;
            double x = renderShape.pos().getX() - cameraPos.x;
            double y = renderShape.pos().getY() - cameraPos.y;
            double z = renderShape.pos().getZ() - cameraPos.z;
            VoxelShape shape = renderShape.shape();
            VertexRendering.drawBox(
                matrices.peek(), lines,
                x + shape.getMin(Direction.Axis.X), y + shape.getMin(Direction.Axis.Y), z + shape.getMin(Direction.Axis.Z),
                x + shape.getMax(Direction.Axis.X), y + shape.getMax(Direction.Axis.Y), z + shape.getMax(Direction.Axis.Z),
                r, g, b, 1f
            );
        }
    }

    private record RenderShape(VoxelShape shape, BlockPos pos) {}
}