package de.tum.cit.fop.maze;

import com.badlogic.gdx.audio.Sound;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {

    // Volume levels for sound effects
    private float soundCurrentVolume = 1.0f; // Default sound effects volume
    private boolean muted = false; // Track mute state

    // A map to store all sound effects
    private final Map<String, Sound> soundEffects = new HashMap<>();
    // A map to store the sound ID for each sound effect
    private final Map<String, Long> soundIds = new HashMap<>();

    public SoundManager() {
    }

    /**
     * Adds a sound effect to the SoundManager.
     *
     * @param key   The identifier for the sound effect (e.g., "key", "hurt").
     * @param sound The sound effect to add.
     */
    public void addSoundEffect(String key, Sound sound) {
        soundEffects.put(key, sound);
    }

    /**
     * Plays a one-time sound effect and stores the sound ID.
     *
     * @param key The identifier for the sound effect.
     * @return The sound ID of the played sound.
     */
    public long playSound(String key) {
        Sound sound = soundEffects.get(key);
        if (sound != null) {
            long soundId = sound.play(getCurrentVolume()); // Play sound at the current volume
            soundIds.put(key, soundId); // Store the sound ID
            return soundId;
        }
        return -1; // Return an invalid ID if sound is not found
    }

    /**
     * Sets the volume for all sound effects.
     *
     * @param value The new volume level (0.0f to 1.0f).
     */
    public void setVolume(float value) {
        if (!muted) {
            this.soundCurrentVolume = value;
        } else {
            this.soundCurrentVolume = 0.0f; // Mute all sounds
        }

        // Update the volume for all active sounds using their stored sound IDs
        for (Map.Entry<String, Long> entry : soundIds.entrySet()) {
            Sound sound = soundEffects.get(entry.getKey());
            if (sound != null) {
                sound.setVolume(entry.getValue(), soundCurrentVolume); // Set the volume for the active sound
            }
        }
    }


    /**
     * Adjusts the volume of the sound effects based on the mute state.
     *
     * @return The volume to apply, either the current volume or 0 if muted.
     */
    private float getCurrentVolume() {
        return muted ? 0.0f : soundCurrentVolume;
    }

    /**
     * Mutes or unmutes all sound effects.
     *
     * @param mute true to mute, false to unmute.
     */
    public void muteAll(boolean mute) {
        this.muted = mute;
        setVolume(this.soundCurrentVolume); // Update volume based on mute state
    }

    /**
     * Returns the current volume level.
     *
     * @return The current sound effects volume.
     */
    public float getVolume() {
        return soundCurrentVolume;
    }

    /**
     * Checks if the sound effects are currently muted.
     *
     * @return true if muted, false otherwise.
     */
    public boolean isMuted() {
        return muted;
    }
}
