package vip.radium.event.impl.packet;

import jdk.nashorn.internal.objects.annotations.Getter;
import vip.radium.event.CancellableEvent;
import net.minecraft.network.Packet;

public final class PacketReceiveEvent extends CancellableEvent {

    private Packet<?> packet;
    public Direction direction;

    public enum Direction {
        SEND, RECEIVE
    }

    public PacketReceiveEvent(Packet<?> packet) {
        this.packet = packet;
    }
    public Packet<?> getPacket() {
        return packet;
    }
    public void setPacket(Packet<?> packet) {
        this.packet = packet;
    }

}

