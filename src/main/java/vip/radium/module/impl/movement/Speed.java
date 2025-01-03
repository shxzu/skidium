package vip.radium.module.impl.movement;

import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.util.Timer;
import vip.radium.event.EventBusPriorities;
import vip.radium.event.impl.player.MoveEvent;
import vip.radium.module.Module;
import vip.radium.module.ModuleCategory;
import vip.radium.module.ModuleInfo;
import vip.radium.module.ModuleManager;
import vip.radium.property.impl.EnumProperty;
import vip.radium.utils.*;

@ModuleInfo(label = "Speed", category = ModuleCategory.MOVEMENT)
public final class Speed extends Module {

    private final EnumProperty<SpeedMode> speedModeProperty = new EnumProperty<>("Mode", SpeedMode.WATCHDOG);
/*I FUCKING HATE THAT MC.THEPLAYER().JUMP() DOESNT WORK RIGHT!*/
    private boolean wasOnGround;
    public TimerUtil timer = new TimerUtil();
    private int airTicks;

    @EventLink(EventBusPriorities.LOW)
    public final Listener<MoveEvent> moveEventListener = e -> {
        switch (speedModeProperty.getValue()) {
            case WATCHDOG:
                if(MovementUtils.isOnGround()) {
                    if(MovementUtils.isMoving()) {
                        mc.getGameSettings().keyBindJump.pressed = true;
                        MovementUtils.strafe();
                    }
                }

                break;
            case MUSHMC:
                if (MovementUtils.isMoving() && !mc.getGameSettings().keyBindJump.isKeyDown()) {
                    MovementUtils.setMotion(mc.thePlayer().movementInput.moveForward, mc.thePlayer().movementInput.moveStrafe, mc.thePlayer().rotationYaw, MovementUtils.getHorizontalSpeed());
                    if(MovementUtils.isOnGround()) {
                        if (MovementUtils.isMoving()) {
                            e.setY(mc.thePlayer().motionY = 0.01F);
                            MovementUtils.setSpeed(e, MovementUtils.getBaseMoveSpeed() * 1.69);
                            mc.getTimer().timerSpeed = 1.3f;
                        }
                        this.airTicks = 0;
                    } else {
                        ++this.airTicks;
                    }
                }
                break;
            case VULCAN1:
                if (MovementUtils.isMoving()) {
                    mc.getTimer().timerSpeed = 1.0F;
                    if (!MovementUtils.isOnGround() && mc.thePlayer().ticksExisted % 10 == 0) {
                        mc.thePlayer().motionY = -0.2;
                    }

                    if (mc.thePlayer().ticksExisted % 15 == 0) {
                        mc.getTimer().timerSpeed = 1.1F;
                    }

                    if (mc.thePlayer().ticksExisted % 25 == 0) {
                        mc.getTimer().timerSpeed = 1.7F;
                    }

                    if (mc.thePlayer().ticksExisted % 35 == 0) {
                        mc.getTimer().timerSpeed = 1.7F;
                    }

                    if (MovementUtils.isMoving() && MovementUtils.isOnGround()) {
                        MovementUtils.strafe();
                        mc.getTimer().timerSpeed = 1.0599F;
                        mc.getGameSettings().keyBindJump.pressed = true;
                        MovementUtils.setSpeed(e,0.48);
                    }
                }

                break;
            case VULCAN2:
                if(MovementUtils.isMoving()) {
                    if(mc.thePlayer().isAirBorne && mc.thePlayer().fallDistance > 2) {
                        mc.getTimer().timerSpeed = 1f;
                        return;
                    }

                    if(MovementUtils.isOnGround()) {
                        mc.getGameSettings().keyBindJump.pressed = true;
                        if(mc.thePlayer().motionY > 0) {
                            mc.getTimer().timerSpeed = 1.1453f;
                        }
                        MovementUtils.setSpeed(e, 0.4815f);
                    } else {
                        if (mc.thePlayer().motionY < 0) {
                            mc.getTimer().timerSpeed = 0.9185f;
                        }
                    }
                } else {
                    mc.getTimer().timerSpeed = 1f;
                }

                break;
            case VERUS:
                if (MovementUtils.isMoving()) {
                    if (MovementUtils.isOnGround()) {
                        mc.getGameSettings().keyBindJump.pressed = true;
                        wasOnGround = true;
                    } else if (wasOnGround) {
                        if (!mc.thePlayer().isCollidedHorizontally) {
                            mc.thePlayer().motionY = -0.0784000015258789;
                        }
                        wasOnGround = false;
                    }
                    if(!MovementUtils.isOnGround()) {
                        mc.getGameSettings().keyBindJump.pressed = false;
                    }
                    MovementUtils.setSpeed(e, 0.33);
                } else {
                    mc.thePlayer().motionX = mc.thePlayer().motionZ = 0;
                }
                break;
            case STRAFE:
                MovementUtils.strafe();
                if(MovementUtils.isOnGround()) {
                    mc.getGameSettings().keyBindJump.pressed = true;
                }
                break;
            case LEGIT:
                if(MovementUtils.isOnGround()) {
                mc.getGameSettings().keyBindJump.pressed = true;
                }
                break;
            }
    };

    public static double predictedMotion(double motion, int ticks) {
        if (ticks == 0) {
            return motion;
        } else {
            double predicted = motion;

            for(int i = 0; i < ticks; ++i) {
                predicted = (predicted - 0.08) * 0.9800000190734863;
            }

            return predicted;
        }
    }

    public Speed() {
        setSuffixListener(speedModeProperty);
    }

    @Override
    public void onDisable() {
        mc.getTimer().timerSpeed = 1f;
        mc.getGameSettings().keyBindJump.pressed = false;
    }

    public static boolean isSpeeding() {
        return ModuleManager.getInstance(Speed.class).isEnabled();
    }


    private enum SpeedMode {
        WATCHDOG, MUSHMC, VERUS, VULCAN1, VULCAN2, STRAFE, LEGIT
    }
}
