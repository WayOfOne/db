package bot.script.listener;

import net.runelite.api.GameState;

/** Game-state callbacks off `GameStateChanged`. */
public interface GameStateListener extends java.util.EventListener {
    /** The client state changed (previous, current). */
    default void onGameStateChange(GameState oldState, GameState newState) {
    }
}
