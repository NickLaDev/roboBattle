# Sistema de Efeitos Visuais - Documentação Técnica

**Última atualização:** 13 de Novembro de 2025

---

## Visão Geral

O sistema de efeitos visuais do RoboBattle utiliza uma arquitetura de **eventos** para comunicação entre o motor de combate (`UiBattleEngine`) e a camada de visualização (`PixelBattleView`). Este design permite adicionar novos efeitos visuais sem modificar a lógica de combate.

---

## Arquitetura do Sistema

### 1. Geração de Eventos (UiBattleEngine)

**Localização:** `src/main/java/br/puc/battledolls/ui/UiBattleEngine.java`

#### Classe BattleEvent

```java
public static class BattleEvent {
    public final boolean isCritical;     // Golpe crítico?
    public final boolean isEvaded;       // Esquivou?
    public final boolean isDefended;     // Defendeu?
    public final boolean isSpecial;      // Habilidade especial?
    public final boolean isBleeding;     // Aplicou sangramento?
    public final int damage;             // Dano causado
    public final String attackerName;    // Nome do atacante
    public final String defenderName;    // Nome do defensor
}
```

#### Quando os Eventos São Criados

Os eventos são criados no método `perform(Action)` e retornados em `StepResult`:

```java
public StepResult perform(Action action) {
    BattleEvent event = BattleEvent.none();

    switch (action) {
        case ATTACK, SPECIAL -> {
            // ... cálculo de dano ...

            if (res.evaded) {
                // Evento de esquiva
                event = new BattleEvent(false, true, false, useSpecial,
                                       false, 0, current.name(), enemy.name());
            } else {
                // Evento de dano
                event = new BattleEvent(res.critical, false, wasDefended,
                                       useSpecial, appliedBleed, dmg,
                                       current.name(), enemy.name());
            }
        }
    }

    return pack(logs, event);  // event incluído no resultado
}
```

**Propriedades do Evento:**
- `isCritical`: Se o ataque foi crítico (multiplica dano por 1.5x)
- `isEvaded`: Se o defensor esquivou completamente
- `isDefended`: Se o defensor estava em postura de guarda (reduz dano em 50%)
- `isSpecial`: Se foi usado ataque especial
- `isBleeding`: Se o sangramento foi aplicado (automático em críticos)
- `damage`: Quantidade de dano causado (após todas as reduções)
- `attackerName` / `defenderName`: Para identificar posicionamento visual

---

### 2. Processamento de Eventos (PixelBattleView)

**Localização:** `src/main/java/br/puc/battledolls/ui/PixelBattleView.java`

#### Sistema de Fases de Animação

```java
private enum Phase {
    NONE,      // Aguardando input do jogador
    APPROACH,  // Personagem correndo em direção ao inimigo
    IMPACT,    // Golpe sendo executado + processamento do evento
    RETURN     // Personagem retornando à posição original
}
```

#### Fluxo de Processamento

1. **Fase APPROACH** (Aproximação)
   - Personagem corre em direção ao oponente
   - Usa animação de corrida (`r1_run.png` / `r2_run.png`)
   - Move sprite usando `r1OffsetX` / `r2OffsetX`

2. **Fase IMPACT** (Impacto - CRÍTICO!)
   ```java
   private void startImpact() {
       // 1. Executa o ataque no engine
       var result = engine.perform(pendingAction);

       // 2. Cria efeitos visuais baseados no evento
       if (result.event != null) {
           createVisualEffects(result.event);  // ← AQUI!
       }

       // 3. Inicia animação de ataque do atacante
       if (attackerIsP1) {
           curAtk1 = rng.nextInt(r1Atks.length);  // Escolhe ataque aleatório
           p1Attacking = true;
       } else {
           // Similar para P2
       }

       // 4. Decide animação do defensor (defend ou hurt)
       // ...
   }
   ```

3. **Fase RETURN** (Retorno)
   - Personagem retorna à posição inicial
   - Limpa flags de animação

---

### 3. Criação de Efeitos Visuais

#### Método createVisualEffects()

**Localização:** `PixelBattleView.java:1167`

```java
private void createVisualEffects(UiBattleEngine.BattleEvent event) {
    // 1. Determina posição do defensor
    boolean defenderIsP1 = event.defenderName.equals(leftName);
    double centerX = BASE_W / 2;
    double defenderX = defenderIsP1 ? (centerX - GAP - 100) : (centerX + GAP + 100);
    double defenderY = R_Y - 150; // Acima do sprite

    // 2. Cria textos flutuantes baseados no evento
    if (event.isEvaded) {
        // Esquiva
        floatingTexts.add(new FloatingText("ESQUIVOU!", defenderX, defenderY,
                Color.web("#00D9FF"), false));

    } else if (event.damage > 0) {
        // Dano (normal ou crítico)
        Color damageColor = event.isCritical ?
            Color.web("#FFD700") : Color.web("#FF4444");

        // Texto "CRÍTICO!"
        if (event.isCritical) {
            floatingTexts.add(new FloatingText("CRÍTICO!",
                defenderX, defenderY - 30, Color.web("#FFD700"), true));
        }

        // Texto "SPECIAL!"
        if (event.isSpecial) {
            floatingTexts.add(new FloatingText("SPECIAL!",
                defenderX, defenderY - 60, Color.web("#BB86FC"), false));
        }

        // Valor do dano
        floatingTexts.add(new FloatingText(String.valueOf(event.damage),
            defenderX, defenderY, damageColor, event.isCritical));

        // Texto "DEFENDIDO"
        if (event.isDefended) {
            floatingTexts.add(new FloatingText("DEFENDIDO",
                defenderX, defenderY + 30, Color.web("#60A5FA"), false));
        }

        // Texto "SANGRAMENTO"
        if (event.isBleeding) {
            floatingTexts.add(new FloatingText("SANGRAMENTO",
                defenderX, defenderY + 60, Color.web("#DC143C"), false));
        }
    }
}
```

---

### 4. Sistema de FloatingText

#### Estrutura da Classe

```java
private static class FloatingText {
    String text;          // Texto a exibir
    double x, y;          // Posição inicial
    double t;             // Timer (0.0 → 1.0)
    Color color;          // Cor do texto
    double scale;         // Escala (1.0 normal, 1.5 para crítico)
    boolean isCritical;   // Se é crítico (afeta tamanho e brilho)
}
```

#### Ciclo de Vida

1. **Criação:** Adicionado à lista `floatingTexts`
2. **Atualização:** `updateFloatingTexts(dt)` - incrementa timer
3. **Renderização:** `drawFloatingTexts()` - desenha com efeitos
4. **Remoção:** Automaticamente removido quando `t >= 1.0`

#### Animação do FloatingText

```java
private void drawFloatingTexts() {
    for (FloatingText ft : floatingTexts) {
        double progress = ft.t;  // 0.0 → 1.0

        // 1. Movimento para cima
        double offsetY = -progress * 80;  // Sobe 80px

        // 2. Fade out
        double alpha = 1.0 - progress;

        // 3. Escala crescente
        double currentScale = ft.scale * (1.0 + progress * 0.3);

        // 4. Tamanho da fonte
        int fontSize = (int) ((ft.isCritical ? 48 : 36) * currentScale);

        // 5. Desenha sombra
        gc.setGlobalAlpha(alpha * 0.7);
        gc.setFill(Color.BLACK);
        gc.fillText(ft.text, x + 3, y + 3);

        // 6. Desenha texto principal
        gc.setGlobalAlpha(alpha);
        gc.setFill(ft.color);
        gc.fillText(ft.text, x, y);

        // 7. Brilho para críticos (primeira metade da animação)
        if (ft.isCritical && progress < 0.5) {
            double glowAlpha = alpha * 0.5 * (1.0 - progress * 2);
            gc.setGlobalAlpha(glowAlpha);
            gc.setFill(Color.web("#FFD700"));
            gc.setEffect(new Glow(0.8));
            gc.fillText(ft.text, x, y);
        }
    }
}
```

---

## Tabela de Efeitos Visuais

| Condição | Texto | Cor | Posição Y | Crítico? | Descrição |
|----------|-------|-----|-----------|----------|-----------|
| `isEvaded` | ESQUIVOU! | `#00D9FF` (ciano) | `defenderY` | Não | Ataque foi esquivado |
| `isCritical` | CRÍTICO! | `#FFD700` (dourado) | `defenderY - 30` | **Sim** | Golpe crítico |
| `isSpecial` | SPECIAL! | `#BB86FC` (roxo) | `defenderY - 60` | Não | Habilidade especial usada |
| `damage > 0` | [valor] | `#FFD700` (crítico) ou `#FF4444` (normal) | `defenderY` | Se crítico | Dano causado |
| `isDefended` | DEFENDIDO | `#60A5FA` (azul) | `defenderY + 30` | Não | Guarda ativa (reduz 50%) |
| `isBleeding` | SANGRAMENTO | `#DC143C` (vermelho escuro) | `defenderY + 60` | Não | Status de sangramento aplicado |

---

## Posicionamento Visual

### Cálculo de Posição

```java
boolean defenderIsP1 = event.defenderName.equals(leftName);
double centerX = BASE_W / 2;           // 1280 / 2 = 640px
double defenderX = defenderIsP1 ?
    (centerX - GAP - 100) :            // P1: 640 - 80 - 100 = 460px
    (centerX + GAP + 100);             // P2: 640 + 80 + 100 = 820px
double defenderY = R_Y - 150;          // 480 - 150 = 330px
```

### Layout dos Textos (ordem vertical)

```
                    Y - 60: "SPECIAL!"      (roxo)
                    Y - 30: "CRÍTICO!"      (dourado, com brilho)
defenderY → Y      Y:      [DANO]          (dourado se crit, vermelho se normal)
                    Y + 30: "DEFENDIDO"     (azul)
                    Y + 60: "SANGRAMENTO"   (vermelho escuro)
```

---

## Fluxo Completo: Ataque → Efeitos Visuais

```
┌─────────────────┐
│ Jogador clica   │
│ "ATTACK" ou     │
│ "SPECIAL"       │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────┐
│ Phase.APPROACH                      │
│ - Personagem corre                  │
│ - Animação de corrida               │
│ - inputLocked = true                │
└────────┬────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│ Phase.IMPACT (startImpact)          │
│                                     │
│ 1. engine.perform(action)           │
│    ↓                                │
│ 2. UiBattleEngine calcula dano      │
│    ↓                                │
│ 3. Cria BattleEvent com:            │
│    - isCritical                     │
│    - isEvaded                       │
│    - isDefended                     │
│    - isSpecial                      │
│    - isBleeding                     │
│    - damage                         │
│    ↓                                │
│ 4. Retorna StepResult com event     │
│    ↓                                │
│ 5. createVisualEffects(event)       │
│    ↓                                │
│ 6. Adiciona FloatingText à lista    │
│    ↓                                │
│ 7. Inicia anim. ataque (atacante)   │
│    ↓                                │
│ 8. Inicia anim. defend/hurt (def.)  │
└────────┬────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│ Game Loop (update)                  │
│                                     │
│ 1. updateFloatingTexts(dt)          │
│    - Incrementa ft.t += dt * 1.5    │
│    - Remove se t >= 1.0             │
│    ↓                                │
│ 2. render()                         │
│    ↓                                │
│ 3. drawFloatingTexts()              │
│    - Calcula posição com movimento  │
│    - Aplica fade out                │
│    - Desenha sombra                 │
│    - Desenha texto                  │
│    - Aplica brilho (se crítico)     │
└────────┬────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│ Phase.RETURN                        │
│ - Personagem retorna                │
│ - inputLocked = false               │
│ - Phase = NONE                      │
└─────────────────────────────────────┘
```

---

## Como Adicionar Novos Efeitos Visuais

### Passo 1: Adicionar propriedade ao BattleEvent

```java
// Em UiBattleEngine.java
public static class BattleEvent {
    // ... campos existentes ...
    public final boolean isNewEffect;  // ← NOVO

    public BattleEvent(..., boolean isNewEffect) {
        // ... atribuições existentes ...
        this.isNewEffect = isNewEffect;
    }
}
```

### Passo 2: Configurar evento na criação

```java
// Em UiBattleEngine.perform()
boolean newEffectTriggered = /* sua lógica aqui */;

event = new BattleEvent(
    res.critical,
    false,
    wasDefended,
    useSpecial,
    appliedBleed,
    dmg,
    current.name(),
    enemy.name(),
    newEffectTriggered  // ← NOVO
);
```

### Passo 3: Criar efeito visual

```java
// Em PixelBattleView.createVisualEffects()
if (event.isNewEffect) {
    floatingTexts.add(new FloatingText(
        "NOVO EFEITO!",           // Texto
        defenderX,                // Posição X
        defenderY + 90,           // Posição Y (abaixo dos outros)
        Color.web("#00FF00"),     // Cor
        false                     // Não é crítico
    ));
}
```

---

## Cores Utilizadas no Sistema

| Efeito | Cor Hex | RGB | Descrição |
|--------|---------|-----|-----------|
| Esquiva | `#00D9FF` | (0, 217, 255) | Ciano brilhante |
| Crítico | `#FFD700` | (255, 215, 0) | Dourado |
| Especial | `#BB86FC` | (187, 134, 252) | Roxo (tema do jogo) |
| Dano normal | `#FF4444` | (255, 68, 68) | Vermelho |
| Defendido | `#60A5FA` | (96, 165, 250) | Azul claro |
| Sangramento | `#DC143C` | (220, 20, 60) | Vermelho escuro (Crimson) |
| Sombra | `#000000` | (0, 0, 0) | Preto |

---

## Timing e Performance

### Configurações de Animação

```java
// Velocidade do floating text
updateFloatingTexts(dt) {
    ft.t += dt * 1.5;  // 1.5x velocidade = ~0.67s duração total
}

// Distância de movimento
double offsetY = -progress * 80;  // Sobe 80 pixels

// Escala crescente
double currentScale = ft.scale * (1.0 + progress * 0.3);  // +30% no final
```

### Otimizações

- ✅ FloatingTexts são removidos automaticamente quando `t >= 1.0`
- ✅ Brilho de crítico só é aplicado na primeira metade (`progress < 0.5`)
- ✅ Lista é limpa em cada frame (`removeIf`)
- ✅ Sem alocação de objetos durante renderização

---

## Arquivos Relacionados

### Core Engine
- **UiBattleEngine.java** - Gerencia combate e cria eventos
  - Classe `BattleEvent` (linhas 62-88)
  - Método `perform()` (linhas 131-208)
  - Criação de eventos (linhas 164, 191)

### Visual Layer
- **PixelBattleView.java** - Renderização e efeitos visuais
  - Classe `FloatingText` (linhas 146-162)
  - Método `createVisualEffects()` (linhas 1167-1206)
  - Método `updateFloatingTexts()` (linhas 1211-1216)
  - Método `drawFloatingTexts()` (linhas 1221-1259)
  - Processamento em `startImpact()` (linhas 616-622)

### Combat Logic
- **DamageCalculator.java** - Cálculo de dano e flags
- **BattleEngine.java** - Engine de combate (modo texto)

---

## Notas Importantes

1. **Thread Safety**: Todos os efeitos são processados na thread de renderização JavaFX
2. **Estado Imutável**: `BattleEvent` é imutável (todos campos `final`)
3. **Garbage Collection**: FloatingTexts são removidos automaticamente, não há memory leak
4. **Extensibilidade**: Sistema projetado para fácil adição de novos efeitos
5. **Separação de Responsabilidades**: Engine não conhece visualização, apenas produz eventos

---

## Exemplo Completo: Golpe Crítico + Especial

```
Jogador usa SPECIAL → Engine calcula:
  - Dano: 86 (crítico + especial)
  - isCritical: true
  - isSpecial: true
  - isBleeding: true (automático em críticos)

↓

createVisualEffects() cria 4 FloatingTexts:
  1. "SPECIAL!"     (Y - 60, roxo)
  2. "CRÍTICO!"     (Y - 30, dourado, com brilho)
  3. "86"           (Y, dourado, grande)
  4. "SANGRAMENTO"  (Y + 60, vermelho escuro)

↓

Durante ~0.67s:
  - Textos sobem 80px
  - Fade out gradual
  - Escala cresce 30%
  - Crítico com efeito de brilho
```

---

**FIM DA DOCUMENTAÇÃO**
