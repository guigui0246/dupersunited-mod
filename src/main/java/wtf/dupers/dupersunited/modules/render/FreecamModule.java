package wtf.dupers.dupersunited.modules.render;

import wtf.dupers.dupersunited.modules.Category;
import wtf.dupers.dupersunited.modules.Module;
import wtf.dupers.dupersunited.modules.settings.BindSetting;
import wtf.dupers.dupersunited.modules.settings.FloatSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class FreecamModule extends Module {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public final FloatSetting speed = register(new FloatSetting("Speed", 1f, 0f, 10f));

    public double posX, posY, posZ;
    public double prevPosX, prevPosY, prevPosZ;
    public float yaw, pitch;
    public float lastYaw, lastPitch;

    private Perspective prePerspective;
    private boolean forward, backward, left, right, up, down;

    public FreecamModule() {
        super("Freecam", "Detaches your camera from your player.", Category.render);
        this.register(new BindSetting("Keybind", GLFW.GLFW_KEY_UNKNOWN).linkedTo(this));
    }

    @Override
    protected void onEnable() {
        if (mc.player == null) return;

        yaw       = mc.player.getYaw();
        pitch     = mc.player.getPitch();
        lastYaw   = yaw;
        lastPitch = pitch;

        Vec3d camPos = mc.gameRenderer.getCamera().getCameraPos();
        posX = prevPosX = camPos.x;
        posY = prevPosY = camPos.y;
        posZ = prevPosZ = camPos.z;

        prePerspective = mc.options.getPerspective();
        mc.options.setPerspective(Perspective.THIRD_PERSON_BACK);

        unpress();
    }

    @Override
    protected void onDisable() {
        mc.options.setPerspective(prePerspective);
        forward = backward = left = right = up = down = false;
        unpress();
        if (mc.player != null) mc.player.noClip = false;
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        if (mc.options.getPerspective() != Perspective.THIRD_PERSON_BACK)
            mc.options.setPerspective(Perspective.THIRD_PERSON_BACK);

        forward  = isKeyDown(mc.options.forwardKey.getDefaultKey().getCode());
        backward = isKeyDown(mc.options.backKey.getDefaultKey().getCode());
        left     = isKeyDown(mc.options.leftKey.getDefaultKey().getCode());
        right    = isKeyDown(mc.options.rightKey.getDefaultKey().getCode());
        up       = isKeyDown(mc.options.jumpKey.getDefaultKey().getCode());
        down     = isKeyDown(mc.options.sneakKey.getDefaultKey().getCode());

        Vec3d fwd  = Vec3d.fromPolar(0, yaw);
        Vec3d side = Vec3d.fromPolar(0, yaw + 90);

        double velX = 0, velY = 0, velZ = 0;
        float s = speed.getValue() * 0.5f;

        boolean movingForward = false;
        boolean movingLateral = false;

        if (forward)  { velX += fwd.x  * s; velZ += fwd.z  * s; movingForward = true; }
        if (backward) { velX -= fwd.x  * s; velZ -= fwd.z  * s; movingForward = true; }
        if (right)    { velX += side.x * s; velZ += side.z * s; movingLateral = true; }
        if (left)     { velX -= side.x * s; velZ -= side.z * s; movingLateral = true; }

        if (movingForward && movingLateral) {
            double diag = 1.0 / Math.sqrt(2);
            velX *= diag;
            velZ *= diag;
        }

        if (up)   velY += s;
        if (down) velY -= s;

        prevPosX = posX; prevPosY = posY; prevPosZ = posZ;
        posX += velX; posY += velY; posZ += velZ;
    }

    public void changeLookDirection(double deltaX, double deltaY) {
        lastYaw   = yaw;
        lastPitch = pitch;

        yaw   += (float) deltaX;
        pitch -= (float) deltaY;
        pitch  = MathHelper.clamp(pitch, -90f, 90f);
    }

    public double getLerpedX(float tickDelta)     { return MathHelper.lerp(tickDelta, prevPosX, posX); }
    public double getLerpedY(float tickDelta)     { return MathHelper.lerp(tickDelta, prevPosY, posY); }
    public double getLerpedZ(float tickDelta)     { return MathHelper.lerp(tickDelta, prevPosZ, posZ); }
    public double getLerpedYaw(float tickDelta)   { return MathHelper.lerp(tickDelta, lastYaw,   yaw);   }
    public double getLerpedPitch(float tickDelta) { return MathHelper.lerp(tickDelta, lastPitch, pitch); }

    private void unpress() {
        mc.options.forwardKey.setPressed(false);
        mc.options.backKey.setPressed(false);
        mc.options.leftKey.setPressed(false);
        mc.options.rightKey.setPressed(false);
        mc.options.jumpKey.setPressed(false);
        mc.options.sneakKey.setPressed(false);
    }

    private boolean isKeyDown(int key) {
        return GLFW.glfwGetKey(mc.getWindow().getHandle(), key) == GLFW.GLFW_PRESS;
    }
}