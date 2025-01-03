package vip.radium.utils.handler;

import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S09PacketHeldItemChange;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;
import vip.radium.event.impl.packet.PacketReceiveEvent;
import vip.radium.event.impl.packet.PacketSendEvent;
import vip.radium.event.impl.player.MoveEvent;

public class BPH { // ensures you don't get banned
    public boolean C08;
    public boolean C07;
    private boolean C02;
    public boolean C09;
    public static boolean delayAttack;
    public boolean delay;
    public static int playerSlot = -1;
    public int serverSlot = -1;

    @EventLink
    public Listener<PacketReceiveEvent> packetReceiveEventListener = e -> {
        if (e.direction != PacketReceiveEvent.Direction.SEND) {
            if (e.isCancelled()) {
                return;
            }
            if (e.getPacket() instanceof C02PacketUseEntity) { // sending a C07 on the same tick as C02 can ban, this usually happens when you unblock and attack on the same tick
                if (C07) {
                    e.setCancelled(true);
                    return;
                }
                C02 = true;
            } else if (e.getPacket() instanceof C08PacketPlayerBlockPlacement) {
                C08 = true;
            } else if (e.getPacket() instanceof C07PacketPlayerDigging) {
                C07 = true;
            } else if (e.getPacket() instanceof C09PacketHeldItemChange) {
                if (((C09PacketHeldItemChange) e.getPacket()).getSlotId() == playerSlot && ((C09PacketHeldItemChange) e.getPacket()).getSlotId() == serverSlot) {
                    e.setCancelled(true);
                    return;
                }
                C09 = true;
                serverSlot = playerSlot = ((C09PacketHeldItemChange) e.getPacket()).getSlotId();
            }
        }
        if(e.direction != PacketReceiveEvent.Direction.RECEIVE){
            if (e.getPacket() instanceof S09PacketHeldItemChange) {
                S09PacketHeldItemChange packet = (S09PacketHeldItemChange) e.getPacket();
                if (packet.getHeldItemHotbarIndex() >= 0 && packet.getHeldItemHotbarIndex() < InventoryPlayer.getHotbarSize()) {
                    serverSlot = packet.getHeldItemHotbarIndex();
                }
            }
            else if (e.getPacket() instanceof S0CPacketSpawnPlayer && Minecraft.getMinecraft().thePlayer != null) {
                if (((S0CPacketSpawnPlayer) e.getPacket()).getEntityID() != Minecraft.getMinecraft().thePlayer.getEntityId()) {
                    return;
                }
                this.playerSlot = -1;
            }
        }
    };


    @EventLink
    public final Listener<MoveEvent> moveEventListener = e -> {
        if(e.isPost()) {
            if (delay) {
                delayAttack = false;
                delay = false;
            }
            if (C08 || C09) {
                delay = true;
                delayAttack = true;
            }
            C08 = C07 = C02 = C09 = false;
        }
    };
}
