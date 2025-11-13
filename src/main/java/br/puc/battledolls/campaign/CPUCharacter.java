package br.puc.battledolls.campaign;

/**
 * Representa um personagem da CPU que o jogador enfrentará nas fases.
 * Cada personagem tem um nome, sprite path base e dificuldade.
 */
public class CPUCharacter {
    private final String name;
    private final String spritePath; // caminho base para os sprites (ex: "/assets/pc-characters/Converted_Vampire/")
    private final int baseCredits; // créditos base para gerar o robô
    private final int rewardCredits; // créditos que o jogador ganha ao vencer
    private final SpriteFrameConfig frameConfig; // configuração de frames para cada animação

    public CPUCharacter(String name, String spritePath, int baseCredits, int rewardCredits) {
        this(name, spritePath, baseCredits, rewardCredits, new SpriteFrameConfig());
    }

    public CPUCharacter(String name, String spritePath, int baseCredits, int rewardCredits, SpriteFrameConfig frameConfig) {
        this.name = name;
        this.spritePath = spritePath;
        this.baseCredits = baseCredits;
        this.rewardCredits = rewardCredits;
        this.frameConfig = frameConfig;
    }

    public String name() { return name; }
    public String spritePath() { return spritePath; }
    public int baseCredits() { return baseCredits; }
    public int rewardCredits() { return rewardCredits; }
    public SpriteFrameConfig frameConfig() { return frameConfig; }

    /**
     * Retorna o caminho completo para um sprite específico.
     */
    public String getSpritePath(String spriteName) {
        return spritePath + "/" + spriteName;
    }

    /**
     * Configuração de frames para as animações de um personagem.
     * Permite definir quantos frames cada animação tem e o tamanho de cada frame.
     */
    public static class SpriteFrameConfig {
        public final int idleFrames;
        public final int attack1Frames;
        public final int attack2Frames;
        public final int attack3Frames;
        public final int runFrames;
        public final int defendFrames;
        public final int hurtFrames;
        public final int deathFrames;
        
        // Tamanho de cada frame (largura x altura em pixels)
        // Padrão: 128x128 (mesmo tamanho dos sprites do jogador)
        public final int frameWidth;
        public final int frameHeight;

        // Construtor com valores padrão (usa tamanho padrão dos sprites do jogador: 128x128)
        public SpriteFrameConfig() {
            this(8, 4, 4, 4, 8, 2, 3, 3, 128, 128);
        }

        // Construtor customizado com tamanho de frame
        public SpriteFrameConfig(int idleFrames, int attack1Frames, int attack2Frames, int attack3Frames,
                                 int runFrames, int defendFrames, int hurtFrames, int deathFrames,
                                 int frameWidth, int frameHeight) {
            this.idleFrames = idleFrames;
            this.attack1Frames = attack1Frames;
            this.attack2Frames = attack2Frames;
            this.attack3Frames = attack3Frames;
            this.runFrames = runFrames;
            this.defendFrames = defendFrames;
            this.hurtFrames = hurtFrames;
            this.deathFrames = deathFrames;
            this.frameWidth = frameWidth;
            this.frameHeight = frameHeight;
        }
        
        // Construtor customizado sem tamanho (usa padrão 128x128)
        public SpriteFrameConfig(int idleFrames, int attack1Frames, int attack2Frames, int attack3Frames,
                                 int runFrames, int defendFrames, int hurtFrames, int deathFrames) {
            this(idleFrames, attack1Frames, attack2Frames, attack3Frames, 
                 runFrames, defendFrames, hurtFrames, deathFrames, 128, 128);
        }
    }
}

