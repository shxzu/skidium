package vip.radium.module.impl.movement;

import io.github.nevalackin.homoBus.annotations.EventLink;
import io.github.nevalackin.homoBus.Listener;
import net.minecraft.network.play.client.C03PacketPlayer;
import vip.radium.event.impl.packet.PacketSendEvent;
import vip.radium.event.impl.player.StepEvent;
import vip.radium.module.Module;
import vip.radium.module.ModuleCategory;
import vip.radium.module.ModuleInfo;
import vip.radium.property.Property;
import vip.radium.property.impl.DoubleProperty;
import vip.radium.utils.MovementUtils;
import vip.radium.utils.mc;

@ModuleInfo(label = "Step", category = ModuleCategory.MOVEMENT)
public final class Step extends Module {

    private final DoubleProperty heightProperty = new DoubleProperty("Step Height", 1, 1, 5.0, 1);
    private final double[] offsets = {0.42f, 0.7532f};
    private float timerWhenStepping;
    private boolean cancelMorePackets;
    private byte cancelledPackets;

    @Override
    public void onDisable() {
        mc.getTimer().timerSpeed = 1.0f;
    }

    @EventLink
    public final Listener<StepEvent> onStepEvent = e -> {
        if (!MovementUtils.isInLiquid() && MovementUtils.isOnGround() && !MovementUtils.isOnStairs()) {
            if (e.isPre()) {
                e.setStepHeight(heightProperty.getValue().floatValue());
            }
        }
    };
}