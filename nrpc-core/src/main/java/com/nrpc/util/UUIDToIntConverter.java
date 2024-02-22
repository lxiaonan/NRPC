package com.nrpc.util;

import java.util.UUID;

public class UUIDToIntConverter {
    public static int uuidToInt(UUID uuid) {
        long mostSignificantBits = uuid.getMostSignificantBits();
        long leastSignificantBits = uuid.getLeastSignificantBits();
        return (int) ((mostSignificantBits >> 32) ^ mostSignificantBits ^ (leastSignificantBits >> 32) ^ leastSignificantBits);
    }

    public static void main(String[] args) {
        UUID uuid = UUID.randomUUID();
        int result = uuidToInt(uuid);
        System.out.println(result);
    }
}
