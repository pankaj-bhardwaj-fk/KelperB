package com.flipkart;

/**
 * Created on 06/03/17 by dark magic.
 */
public class PathUtils {
    public static String getPathForParentInHandShake(String parent) {
        return String.format("/%s", parent);
    }

    public static String getPathForChildInHandShake(String parent, String child) {
        return String.format("/%s/%s", parent, child);
    }

    public static String getPathForParentInLeaderShipElection(String parent) {
        return String.format("/%s-clientprovider", parent);
    }

    public static String getPathForChildInLeaderShipElection(String parent, String child) {
        return String.format("/%s-clientprovider/%s", parent, child);
    }
}
