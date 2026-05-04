package com.vinzy.cataddons.features.proxies;

public class ProxyProfiles {
    public String name, address, user, pass;

    public ProxyProfiles(String name, String address, String user, String pass) {
        this.name = name;
        this.address = address;
        this.user = user;
        this.pass = pass;
    }

    public String getHost() {
        if (address != null && address.contains(":")) return address.split(":")[0];
        return address != null ? address : "";
    }

    public int getPort() {
        if (address != null && address.contains(":")) {
            try { return Integer.parseInt(address.split(":")[1]); }
            catch (NumberFormatException ignored) {}
        }
        return 8080;
    }
}