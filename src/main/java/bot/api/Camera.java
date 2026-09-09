package bot.api;

/** Camera control. Yaw/pitch targets are the client's own units. */
public final class Camera {
    private Camera() {
    }

    public static int yaw() {
        return Game.client().getCameraYaw();
    }

    public static int pitch() {
        return Game.client().getCameraPitch();
    }

    public static void yawTo(int yaw) {
        Game.client().setCameraYawTarget(yaw);
    }

    public static void pitchTo(int pitch) {
        Game.client().setCameraPitchTarget(pitch);
    }

    /** Top-down view, useful before tile scanning. */
    public static void topDown() {
        Game.client().setCameraPitchTarget(512);
    }
}
