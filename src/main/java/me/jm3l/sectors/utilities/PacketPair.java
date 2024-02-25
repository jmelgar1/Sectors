package me.jm3l.sectors.utilities;

import com.comphenix.protocol.events.PacketContainer;

public class PacketPair {
    private PacketContainer packetOne;
    private PacketContainer packetTwo;

    public PacketPair(PacketContainer packetOne, PacketContainer packetTwo) {
        this.packetOne = packetOne;
        this.packetTwo = packetTwo;
    }

    public void setPacketOne(PacketContainer packet){
        this.packetOne = packet;
    }

    public void setPacketTwo(PacketContainer packet){
        this.packetTwo = packet;
    }

    public PacketContainer getPacketOne() {
        return this.packetOne;
    }

    public PacketContainer getPacketTwo() {
        return this.packetTwo;
    }
}