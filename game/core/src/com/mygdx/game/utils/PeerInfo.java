package com.mygdx.game.utils;

import javax.jmdns.ServiceInfo;

public class PeerInfo {

    public String ip;
    public String peer_id;
    public ServiceInfo serviceInfo;
    public int port;
    public String username;

    public PeerInfo(String ip_unformatted, ServiceInfo serviceInfo, String data) {
        this.serviceInfo = serviceInfo;
        this.peer_id = data.split(",")[0];
        this.port = Integer.parseInt(data.split(",")[1]);
        this.username = data.split(",")[2];

        this.ip = ip_unformatted;
    }

    public PeerInfo(String ip, String id, int port, String username) {
        this.ip = ip;
        this.peer_id = id;
        this.port = port;
        this.username = username;
    }

}
