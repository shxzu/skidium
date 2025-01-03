package vip.radium.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class PacketUtil {

    public static void sendPacket(Packet<?> p) {
        mc.getNetHandler().sendPacket(p);
    }

    public static boolean isInbound(Packet<?> packet) {
        for (Type type : packet.getClass().getGenericInterfaces()) {
            if (type instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) type;
                if (Packet.class.equals(paramType.getRawType())) {
                    Type[] typeArgs = paramType.getActualTypeArguments();
                    if (typeArgs.length > 0 && typeArgs[0] instanceof Class) {
                        Class<?> typeArgClass = (Class<?>) typeArgs[0];
                        return INetHandlerPlayClient.class.isAssignableFrom(typeArgClass);
                    }
                }
            }
        }
        return false;
    }

    public static boolean isOutbound(Packet<?> packet) {
        for (Type type : packet.getClass().getGenericInterfaces()) {
            if (type instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) type;
                if (Packet.class.equals(paramType.getRawType())) {
                    Type[] typeArgs = paramType.getActualTypeArguments();
                    if (typeArgs.length > 0 && typeArgs[0] instanceof Class) {
                        Class<?> typeArgClass = (Class<?>) typeArgs[0];
                        return INetHandlerPlayServer.class.isAssignableFrom(typeArgClass);
                    }
                }
            }
        }
        return false;
        }
    }
