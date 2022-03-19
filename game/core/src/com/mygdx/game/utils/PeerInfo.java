package com.mygdx.game.utils;

import javax.jmdns.ServiceInfo;

public class PeerInfo {

    public String ip;
    public String peer_id;
    public ServiceInfo serviceInfo;

    public PeerInfo(String ip_unformatted, ServiceInfo serviceInfo, String peer_id) {
        this.serviceInfo = serviceInfo;
        this.peer_id = peer_id;

        String[] split = ip_unformatted.split("/");
        this.ip = split[1];
    }

}
