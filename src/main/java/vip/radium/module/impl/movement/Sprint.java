package vip.radium.module.impl.movement;

import io.github.nevalackin.homoBus.annotations.EventLink;
import io.github.nevalackin.homoBus.Listener;
import vip.radium.event.impl.player.MoveEvent;
import vip.radium.module.Module;
import vip.radium.module.ModuleCategory;
import vip.radium.module.ModuleInfo;
import vip.radium.property.Property;
import vip.radium.utils.MovementUtils;
import vip.radium.utils.mc;

@ModuleInfo(label = "Sprint", category = ModuleCategory.MOVEMENT)
public final class Sprint extends Module {

    private final Property<Boolean> omniProperty = new Property<>("Omni", true);

    private int groundTicks;

    public Sprint() {
        toggle();
    }

    @EventLink
    public final Listener<MoveEvent> moveEventListener = event -> {
        final boolean canSprint = MovementUtils.canSprint(omniProperty.getValue());
        if (MovementUtils.isMoving()) {
            if (omniProperty.getValue()) {
                mc.thePlayer().setSprinting(true);
            } else {
                mc.getGameSettings().keyBindSprint.pressed = true;
            }
        }
    };
}
