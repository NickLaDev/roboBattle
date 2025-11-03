# Battle Dolls — Arena de Personagens (Java/JavaFX)

Duelo 1×1 em **pixel art**: cada jogador escolhe um **personagem base** (samurai, lutador, etc.), monta sua **construção de atributos** (ATK, DEF, HP, CRIT, EVA…) e usa **habilidades especiais únicas** em combate por turnos.

> **Aviso de migração:** o projeto era “RoboBattle” (robôs). Agora é **Battle Dolls** (personagens). Alguns nomes de classes/arquivos podem estar no padrão antigo (ex.: `Robot`, `Weapon`, `Armor`). Ver seção **“Mapeamento de Legado → Novo”** abaixo.

---

## ✨ Principais recursos

- **Personagens base** (ex.: Samurai, Lutador…): cada um com **habilidade especial** própria.  
- **Construção de atributos** por jogador: níveis 1–5 de armas/escudos e pontos de status.  
- **Combate por turnos** com botões: **ATTACK**, **DEFEND**, **SPECIAL**.  
- **Mecânicas de batalha**: crítico, esquiva, **Defend** reduz próximo dano, **sangramento** (dano por turno).  
- **Pixel art animado**: spritesheets (idle + **3 variações de ataque**), efeitos visuais.  
- **Arquitetura modular** (Modelo / Regras / UI) para facilitar expansão.

---

## 📸 Screenshots (placeholders)

- `assets/screens/login.png` – Tela de nomes  
- `assets/screens/store.png` – Loja (armas/escudos 1–5, preço por nível, saldo)  
- `assets/screens/battle.png` – Arena (barras de HP, botões, animações)

> Substitua pelos seus prints assim que tiver.

---

## 🎮 Loop de jogo

1. **Nomes** dos jogadores  
2. **Loja**: escolher personagem base + níveis de **arma** e **escudo** (1–5) e alocar atributos  
3. **Batalha**: turnos alternados com **Attack / Defend / Special**  
4. **Vence** quem zerar o HP do oponente

---

## 🧩 Atributos & Habilidades

- **Atributos** (exemplo base):  
  - **ATK** (ataque), **DEF** (defesa), **HP**, **CRIT%** (chance), **CRIT×** (multiplicador), **EVA%** (esquiva)  
- **Habilidades especiais** (exemplos):  
  - **Samurai – Iaijutsu**: primeiro ataque do duelo com bônus de dano.  
  - **Lutador – Contra-golpe**: chance de devolver parte do dano recebido ao atacar em seguida.  
  - *(Adicione outras classes facilmente; ver “Como adicionar um novo personagem”)*

---

## 🧠 Cálculo de dano (resumo)

1. Rola **ATK efetivo** vs **DEF efetiva** com pequena variação aleatória.  
2. **Esquiva** → dano 0.  
3. **Crítico** → aplica multiplicador (ex.: `dano *= CRIT×`).  
4. **Defend** no alvo → **dano / 2** (no próximo golpe recebido).  
5. Efeitos como **sangramento** causam dano no **início do turno** do afetado por N turnos.

---

## 🗂️ Estrutura de pastas (sugerida)

```
/assets
  /backgrounds
    store_bg.png
    arena_bg.png
  /characters
    /samurai
      idle.png
      attack1.png
      attack2.png
      attack3.png
    /lutador
      idle.png
      attack1.png
      attack2.png
      attack3.png
  /items
    /swords
      sword1.png
      sword2.png
      ...
      sword5.png
    /shields
      shield1.png
      ...
      shield5.png
/src
  (código Java/JavaFX)
```

> **Carregamento de itens** segue caminho direto (ex.):  
> `/assets/items/swords/sword1.png`, `/assets/items/shields/shield3.png`…

---

## 🛠️ Como rodar

### Requisitos
- **Java 17+**  
- **JavaFX 17+** (ou superior compatível)

### Opção A — Eclipse (recomendado)
1. Importe o projeto no **Eclipse**.  
2. Adicione o **JavaFX SDK** ao Build Path.  
3. Nas **VM arguments** da run configuration, inclua:  
   ```
   --module-path /caminho/para/javafx/lib --add-modules javafx.controls,javafx.fxml,javafx.graphics
   ```
4. Rode a classe **principal** (ex.: `Game.java` ou `GameFX.java`).

### Opção B — Gradle (exemplo de plugin)
```gradle
plugins {
  id 'application'
  id 'org.openjfx.javafxplugin' version '0.1.0'
}
javafx {
  version = '21'
  modules = ['javafx.controls','javafx.fxml','javafx.graphics']
}
application {
  mainClass = 'br.puc.battledolls.Game' // ajuste seu pacote/classe
}
```
> Rodar: `./gradlew run`

### Opção C — Maven (exemplo de plugin)
```xml
<plugin>
  <groupId>org.openjfx</groupId>
  <artifactId>javafx-maven-plugin</artifactId>
  <version>0.0.8</version>
  <configuration>
    <mainClass>br.puc.battledolls.Game</mainClass>
    <launcher>launch</launcher>
    <jlinkZip>true</jlinkZip>
    <stripDebug>true</stripDebug>
    <noHeaderFiles>true</noHeaderFiles>
    <noManPages>true</noManPages>
  </configuration>
</plugin>
```
> Rodar: `mvn javafx:run`

---

## 🧱 Arquitetura (alto nível)

- **Modelo**: `Player`, `Character` (antes `Robot`), `Equipment` (arma/escudo), `Stats`  
- **Regras de combate**: `BattleEngine`, `DamageCalculator`, `DamageResult`  
- **UI / FX**: `GameFX`/`PixelBattleView`, `UiBattleEngine`, `SpriteSheet`, `SpriteAnimator`, `GameFX` (efeitos)  

**Mapeamento de Legado → Novo (quando renomear):**

| Legado        | Novo sugerido      |
|---------------|---------------------|
| `Robot`       | `Character`         |
| `RobotStats`  | `CharacterStats`    |
| `Weapon`      | `Weapon` (mantém)   |
| `Armor`       | `Shield`            |
| `RobotStatsBuilder` | `CharacterStatsBuilder` |

> Enquanto renomeia, mantenha **adapters/aliases** ou uma camada de mapeamento para evitar quebras.

---

## ➕ Como adicionar um novo personagem

1. **Arte**: crie spritesheets `idle.png`, `attack1/2/3.png` em `/assets/characters/<nome>/`.  
2. **Registro**: adicione o personagem na fábrica/lista de personagens jogáveis.  
3. **Atributos base**: defina ATK/DEF/HP/CRIT/EVA iniciais.  
4. **Habilidade especial**: implemente um método/efeito (ex.: buff no 1º golpe, counter, bleed aumentado…).  
5. **Testes**: rode um duelo curto para validar animações e balance.

---

## 🛍️ Loja & Economia

- **Níveis 1–5** para **armas** e **escudos** com **preço por nível** exibido.  
- UI mostra **saldo restante** antes de confirmar a compra.  
- Sem “módulo/nível de módulo” (removidos na nova versão).

---

## 🎹 Controles

- **Mouse/Touch**: botões **ATTACK**, **DEFEND**, **SPECIAL**  
- **Teclado** (opcional): mapear atalhos (ex.: `A`, `D`, `S`) se desejar.

---

## 🗺️ Roadmap curto

- Novos personagens, arenas e efeitos sonoros  
- Melhor-de-3 e estatísticas pós-partida  
- Screen de “builds” salvas por jogador  
- Balance pass em CRIT/EVA/Special

---

## 🐞 Problemas conhecidos

- Alguns nomes de classes ainda no padrão “robô”  
- Sprites com recorte irregular podem “pular” (ajustar `SpriteAnimator`)  
- Background da loja precisa adequar layout à resolução do **store_bg** (3840×2160 recomendado)

---

## 🤝 Contribuição

1. Crie uma branch a partir de `main`.  
2. Commits pequenos e descritivos.  
3. Abra PR com **antes/depois** (GIF/print) e notas de balanceamento.

---

## 📄 Licença

Defina a licença do projeto (ex.: **MIT**).  

---

## 📬 Contato

**Autor:** Nicolas Laredo Alves de Araujo — RA 24001613  
*(adicione e-mail ou link do repositório quando publicar)*
