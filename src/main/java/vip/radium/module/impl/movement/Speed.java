package vip.radium.module.impl.movement;

import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.util.Timer;
import vip.radium.event.EventBusPriorities;
import vip.radium.event.impl.packet.PacketReceiveEvent;
import vip.radium.event.impl.player.MoveEvent;
import vip.radium.event.impl.player.SprintEvent;
import vip.radium.event.impl.player.UpdatePositionEvent;
import vip.radium.module.Module;
import vip.radium.module.ModuleCategory;
import vip.radium.module.ModuleInfo;
import vip.radium.module.ModuleManager;
import vip.radium.property.impl.EnumProperty;
import vip.radium.utils.*;

@ModuleInfo(label = "Speed", category = ModuleCategory.MOVEMENT)
public final class Speed extends Module {

    private final EnumProperty<SpeedMode> speedModeProperty = new EnumProperty<>("Mode", SpeedMode.WATCHDOG);
    private boolean wasOnGround;
    public TimerUtil timer = new TimerUtil();
    private boolean bobToggle = false;
    private int airTicks;
    public static int stage;


    @EventLink
    public final Listener<UpdatePositionEvent> onUpdatePositionEvent = e -> {
        switch (speedModeProperty.getValue()) {
            case WATCHDOG:
                if (MovementUtils.isOnGround()) {
                    if (MovementUtils.isMoving()) {
                        mc.thePlayer().jump();
                        MovementUtils.strafe();
                    }
                }

                break;
            case VERUS:
                if (MovementUtils.isMoving()) {
                    if (MovementUtils.isOnGround()) {
                        mc.thePlayer().jump();
                        wasOnGround = true;
                    } else if (wasOnGround) {
                        if (!mc.thePlayer().isCollidedHorizontally) {
                            mc.thePlayer().motionY = -0.0784000015258789;
                        }
                        wasOnGround = false;
                    }
                    MovementUtils.setSpeed(0.33);
                } else {
                    mc.thePlayer().motionX = mc.thePlayer().motionZ = 0;
                }
                break;
            case STRAFE:
                if (MovementUtils.isMoving()) {
                    MovementUtils.strafe();
                    if (MovementUtils.isOnGround()) {
                        mc.thePlayer().jump();
                    }
                }
                break;
            case LEGIT:
                if (MovementUtils.isMoving()) {
                    if (MovementUtils.isOnGround()) {
                        mc.thePlayer().jump();
                    }
                }
                break;
            case UNCP:
                if (MovementUtils.isMoving() && !mc.getGameSettings().keyBindJump.isKeyDown()) {
                    MovementUtils.strafe();
                    if (airTicks >= 5.1) {
                        mc.getTimer().timerSpeed = 1.2f;
                    } else mc.getTimer().timerSpeed = 1.0f;
                    if (mc.thePlayer().onGround) {
                        mc.thePlayer().jump();
                    }
                }
                break;
            case MUSH:
            if (MovementUtils.isMoving() && !mc.getGameSettings().keyBindJump.isKeyDown()) {
                MovementUtils.strafe();
                    mc.getTimer().timerSpeed = 1.2f;
                if (mc.thePlayer().onGround) {
                    mc.thePlayer().jump();
                    mc.getTimer().timerSpeed = 1.0f;
                }
            }
            break;
            }

        if(mc.thePlayer().onGround) {
            airTicks = 0;
        } else {
            airTicks++;
        }

        };


    public static double predictedMotion(double motion, int ticks) {
        if (ticks == 0) {
            return motion;
        } else {
            double predicted = motion;

            for (int i = 0; i < ticks; ++i) {
                predicted = (predicted - 0.08) * 0.9800000190734863;
            }

            return predicted;
        }
    }

    public Speed() {
        setSuffixListener(speedModeProperty);
    }

    @Override
    public void onEnable() {
        stage = 2;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        mc.getTimer().timerSpeed = 1f;
        mc.getGameSettings().viewBobbing = true;
        mc.getGameSettings().keyBindJump.pressed = false;
        super.onDisable();
    }

    public static boolean isSpeeding() {
        return ModuleManager.getInstance(Speed.class).isEnabled();
    }


    private enum SpeedMode {
        WATCHDOG, UNCP, MUSH, VERUS, STRAFE, LEGIT
    }
}
