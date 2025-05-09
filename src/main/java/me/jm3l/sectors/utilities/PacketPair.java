package me.jm3l.sectors.utilities;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;

public class PacketPair {
    private WrapperPlayServerSpawnEntity packetOne;
    private WrapperPlayServerSpawnEntity packetTwo;

    public PacketPair(WrapperPlayServerSpawnEntity packetOne, WrapperPlayServerSpawnEntity packetTwo) {
        this.packetOne = packetOne;
        this.packetTwo = packetTwo;
    }

    public void setPacketOne(WrapperPlayServerSpawnEntity packet){
        this.packetOne = packet;
    }

    public void setPacketTwo(WrapperPlayServerSpawnEntity packet){
        this.packetTwo = packet;
    }

    public WrapperPlayServerSpawnEntity getPacketOne() {
        return this.packetOne;
    }

    public WrapperPlayServerSpawnEntity getPacketTwo() {
        return this.packetTwo;
    }
}