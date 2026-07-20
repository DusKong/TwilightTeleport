package net.ochibo.twilightteleport.network;

public enum TeleportEffectAction {
    START_DISSOLVE(0),
    HIDDEN(1),
    START_REBUILD(2),
    CLEAR(3);

    private final int wireId;

    TeleportEffectAction(int wireId) {
        this.wireId = wireId;
    }

    public int wireId() {
        return wireId;
    }

    public static TeleportEffectAction fromWireId(int wireId) {
        for (TeleportEffectAction action : values()) {
            if (action.wireId == wireId) {
                return action;
            }
        }

        throw new IllegalArgumentException(
                "Unknown teleport effect action: " + wireId
        );
    }
}
