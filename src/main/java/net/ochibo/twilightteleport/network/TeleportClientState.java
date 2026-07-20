package net.ochibo.twilightteleport.network;

public enum TeleportClientState {
    BLACK_READY(0),
    RENDER_READY(1);

    private final int wireId;

    TeleportClientState(int wireId) {
        this.wireId = wireId;
    }

    public int wireId() {
        return wireId;
    }

    public static TeleportClientState fromWireId(int wireId) {
        for (TeleportClientState state : values()) {
            if (state.wireId == wireId) {
                return state;
            }
        }

        throw new IllegalArgumentException(
                "Unknown teleport client state: " + wireId
        );
    }
}
