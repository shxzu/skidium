package vip.radium.module.impl.misc.hackerdetect.check.impl;

import net.minecraft.entity.player.EntityPlayer;
import vip.radium.module.impl.misc.hackerdetect.check.Check;
import vip.radium.utils.MovementUtils;
import vip.radium.utils.mc;

import java.util.HashMap;
import java.util.Map;

public final class OmniSprintCheck implements Check {

    private static final Map<Integer, Integer> VL_MAP = new HashMap<>();

    @Override
    public boolean flag(EntityPlayer player) {
       final int id = player.getEntityId();
       if (player.moveForward < 0 && player.isSprinting() && MovementUtils.isMoving(player)) {
           if (VL_MAP.containsKey(id)) {
               VL_MAP.put(id, VL_MAP.get(id) + 1);
           } else {
               VL_MAP.put(id, 1);
           }
       } else {
           VL_MAP.put(id, 0);
       }
       return VL_MAP.get(id) >= 10;
    }
}
