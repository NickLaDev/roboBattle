package br.puc.battledolls.audio;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Centraliza o carregamento e a reprodução de músicas e efeitos sonoros do jogo.
 */
public final class AudioManager {

    public enum SfxType {
        ATTACK,
        DEFEND,
        SPECIAL
    }

    private static final String MENU_THEME = "/assets/audio/awesomeness.wav";
    private static final String DEFAULT_BATTLE_THEME = "/assets/audio/battle_default.wav";

    private static final AudioManager INSTANCE = new AudioManager();

    private final Map<String, String> bossThemes = new HashMap<>();
    private final Map<SfxType, AudioClip> sfxClips = new EnumMap<>(SfxType.class);

    private MediaPlayer currentMusic;
    private String currentMusicResource; // Rastreia qual música está tocando
    private double musicVolume = 0.55;
    private double sfxVolume = 0.8;

    private AudioManager() {
        registerBossThemes();
        loadSfx();
    }

    public static AudioManager getInstance() {
        return INSTANCE;
    }

    private void registerBossThemes() {
        bossThemes.put("Converted Vampire", "/assets/audio/boss_converted_vampire.wav");
        bossThemes.put("Vampire Girl", "/assets/audio/boss_vampire_girl.wav");
        bossThemes.put("Samurai Commander", "/assets/audio/boss_samurai_commander.wav");
        bossThemes.put("Countess Vampire", "/assets/audio/boss_countess_vampire.wav");
    }

    private void loadSfx() {
        loadClip(SfxType.ATTACK, "/assets/audio/sfx_attack.wav");
        loadClip(SfxType.DEFEND, "/assets/audio/sfx_defend.wav");
        loadClip(SfxType.SPECIAL, "/assets/audio/sfx_special.wav");
    }

    private void loadClip(SfxType type, String resource) {
        URL url = AudioManager.class.getResource(resource);
        if (url == null) {
            System.err.println("[AUDIO] Recurso não encontrado: " + resource);
            return;
        }
        try {
            AudioClip clip = new AudioClip(url.toExternalForm());
            clip.setVolume(sfxVolume);
            sfxClips.put(type, clip);
        } catch (Exception e) {
            System.err.println("[AUDIO] Falha ao carregar efeito " + resource + ": " + e.getMessage());
        }
    }

    private Media loadMedia(String resource) {
        URL url = AudioManager.class.getResource(resource);
        if (url == null) {
            System.err.println("[AUDIO] Recurso de música não encontrado: " + resource);
            return null;
        }
        try {
            return new Media(url.toExternalForm());
        } catch (Exception e) {
            System.err.println("[AUDIO] Falha ao carregar mídia " + resource + ": " + e.getMessage());
            return null;
        }
    }

    private void playMusicResource(String resource) {
        if (resource == null) {
            stopMusic();
            return;
        }
        
        // Se a mesma música já está tocando, não reinicia
        if (currentMusicResource != null && currentMusicResource.equals(resource) 
            && currentMusic != null && currentMusic.getStatus() == MediaPlayer.Status.PLAYING) {
            return;
        }
        
        Media media = loadMedia(resource);
        if (media == null)
            return;

        stopMusic();
        currentMusic = new MediaPlayer(media);
        currentMusic.setCycleCount(MediaPlayer.INDEFINITE);
        currentMusic.setVolume(musicVolume);
        currentMusicResource = resource; // Rastreia a música atual
        currentMusic.play();
    }

    public void playMenuMusic() {
        playMusicResource(MENU_THEME);
    }

    public void playDefaultBattleMusic() {
        playMusicResource(DEFAULT_BATTLE_THEME);
    }

    public void playBossMusic(String bossName) {
        String resource = bossThemes.get(bossName);
        if (resource == null) {
            playDefaultBattleMusic();
        } else {
            playMusicResource(resource);
        }
    }

    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.dispose();
            currentMusic = null;
            currentMusicResource = null;
        }
    }

    public void playSfx(SfxType type) {
        AudioClip clip = sfxClips.get(type);
        if (clip != null) {
            clip.play(sfxVolume);
        }
    }

    public void setMusicVolume(double volume) {
        this.musicVolume = Math.max(0, Math.min(1, volume));
        if (currentMusic != null) {
            currentMusic.setVolume(this.musicVolume);
        }
    }

    public void setSfxVolume(double volume) {
        this.sfxVolume = Math.max(0, Math.min(1, volume));
        sfxClips.values().forEach(clip -> clip.setVolume(this.sfxVolume));
    }
}
