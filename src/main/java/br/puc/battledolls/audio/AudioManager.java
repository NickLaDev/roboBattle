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
        ATTACK1,
        ATTACK2,
        DEFEND,
        DEFEND1,
        DEFEND2,
        CRITICAL,
        CRITICAL1,
        CRITICAL2,
        SPECIAL,
        SPECIAL_ESPADAS
    }

    private static final String MENU_THEME = "/assets/audio/awesomeness.wav";
    // Música genérica usada no PvP e como fallback de batalha
    private static final String DEFAULT_BATTLE_THEME = "/assets/audio/boss2.mp3";

    private static final AudioManager INSTANCE = new AudioManager();

    private final Map<String, String> bossThemes = new HashMap<>();
    private final Map<SfxType, AudioClip> sfxClips = new EnumMap<>(SfxType.class);

    private MediaPlayer currentMusic;
    private String currentMusicResource; // Rastreia qual música está tocando
    private double musicVolume = 0.55;
    private double sfxVolume = 0.8;
    private final java.util.Random rng = new java.util.Random();

    private AudioManager() {
        registerBossThemes();
        loadSfx();
    }

    public static AudioManager getInstance() {
        return INSTANCE;
    }

    private void registerBossThemes() {
        bossThemes.put("Converted Vampire", "/assets/audio/boss1.wav");
        bossThemes.put("Vampire Girl", "/assets/audio/boss2.mp3");
        bossThemes.put("Samurai Commander", "/assets/audio/boss3.mp3");
        bossThemes.put("Countess Vampire", "/assets/audio/finalboss.mp3");
    }

    private void loadSfx() {
        // Sons antigos (mantidos para compatibilidade)
        loadClip(SfxType.ATTACK, "/assets/audio/sfx_attack.wav");
        loadClip(SfxType.DEFEND, "/assets/audio/sfx_defend.wav");
        loadClip(SfxType.SPECIAL, "/assets/audio/sfx_special.wav");
        
        // Novos sons de ataque
        loadClip(SfxType.ATTACK1, "/assets/audio/ataque1.mp3");
        loadClip(SfxType.ATTACK2, "/assets/audio/ataque2.mp3");
        
        // Novos sons de defesa
        loadClip(SfxType.DEFEND1, "/assets/audio/defesa1.mp3");
        loadClip(SfxType.DEFEND2, "/assets/audio/defesa2.mp3");
        
        // Novos sons de crítico
        loadClip(SfxType.CRITICAL1, "/assets/audio/criticio.mp3");
        loadClip(SfxType.CRITICAL2, "/assets/audio/critico2.mp3");
        
        // Novo som de habilidade especial
        loadClip(SfxType.SPECIAL_ESPADAS, "/assets/audio/especial_Espasdas.mp3");
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
            clip.setRate(1.0); // Reset rate to normal
            clip.play(sfxVolume);
        }
    }
    
    /**
     * Toca um efeito sonoro com velocidade ajustada.
     * @param type Tipo do efeito sonoro
     * @param rate Velocidade de reprodução (1.0 = normal, 1.5 = 1.5x mais rápido)
     */
    public void playSfx(SfxType type, double rate) {
        AudioClip clip = sfxClips.get(type);
        if (clip != null) {
            clip.setRate(rate);
            clip.play(sfxVolume);
        }
    }
    
    /**
     * Toca uma variação aleatória de ataque (ataque1 ou ataque2).
     */
    public void playRandomAttack() {
        SfxType[] attacks = {SfxType.ATTACK1, SfxType.ATTACK2};
        SfxType chosen = attacks[rng.nextInt(attacks.length)];
        playSfx(chosen);
    }
    
    /**
     * Toca uma variação aleatória de defesa (defesa1 ou defesa2).
     */
    public void playRandomDefend() {
        SfxType[] defends = {SfxType.DEFEND1, SfxType.DEFEND2};
        SfxType chosen = defends[rng.nextInt(defends.length)];
        playSfx(chosen);
    }
    
    /**
     * Toca uma variação aleatória de crítico (criticio ou critico2).
     */
    public void playRandomCritical() {
        SfxType[] criticals = {SfxType.CRITICAL1, SfxType.CRITICAL2};
        SfxType chosen = criticals[rng.nextInt(criticals.length)];
        playSfx(chosen);
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
