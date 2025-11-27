# Sugestões de Skill Check para Ataques Especiais

## 🎯 Opção 1: **Timing Bar (Barra de Timing)** ⭐ RECOMENDADA

### Conceito:

Uma barra horizontal com um indicador que se move da esquerda para direita. O jogador precisa apertar ESPAÇO (ou qualquer tecla) quando o indicador estiver na "zona crítica" (centro da barra).

### Mecânica:

- **Zona Perfeita (verde, centro)**: 2.0x de dano (ao invés de 1.5x)
- **Zona Boa (amarela, perto do centro)**: 1.5x de dano (normal)
- **Zona Ruim (vermelha, bordas)**: 1.0x de dano (sem bônus)
- **Falha (não apertou)**: 0.8x de dano (penalidade)

### Vantagens:

✅ **Muito fácil de implementar** - apenas uma barra e um indicador
✅ **Visual claro** - fácil de entender
✅ **Tensão momentânea** - adiciona emoção
✅ **Feedback imediato** - jogador vê se acertou ou não
✅ **Funciona bem com o som** - pode sincronizar com o som do especial

### Implementação:

- Overlay sobre a tela de batalha durante o especial
- Barra de 400px de largura, indicador se move em loop
- Zona crítica: 30% do centro (120px)
- Zona boa: 60% do centro (240px)
- Timer de 2-3 segundos para apertar

---

## 🎯 Opção 2: **Rapid Taps (Apertos Rápidos)**

### Conceito:

O jogador precisa apertar ESPAÇO o máximo de vezes possível em 2 segundos. Quanto mais apertos, mais dano.

### Mecânica:

- **15+ apertos**: 2.0x de dano
- **10-14 apertos**: 1.5x de dano (normal)
- **5-9 apertos**: 1.2x de dano
- **0-4 apertos**: 1.0x de dano (sem bônus)

### Vantagens:

✅ **Simples de implementar** - apenas contar cliques
✅ **Intensidade** - cria tensão física
✅ **Escalável** - pode ajustar thresholds facilmente

### Desvantagens:

❌ Pode ser cansativo em múltiplas batalhas
❌ Menos estratégico, mais "spam"

---

## 🎯 Opção 3: **Combo Sequence (Sequência de Comandos)**

### Conceito:

Aparecem 3-4 setas na tela (↑ ↓ ← →) e o jogador precisa apertar na ordem correta rapidamente.

### Mecânica:

- **Todos corretos**: 2.0x de dano
- **2-3 corretos**: 1.5x de dano (normal)
- **1 correto**: 1.2x de dano
- **Nenhum correto**: 1.0x de dano

### Vantagens:

✅ **Estratégico** - requer atenção e memória
✅ **Visual interessante** - setas grandes na tela
✅ **Diferente** - não é apenas timing

### Desvantagens:

❌ Mais complexo de implementar (UI de setas)
❌ Pode ser difícil para jogadores casuais

---

## 🎯 Opção 4: **Aim Target (Mira)**

### Conceito:

Um círculo/alvo aparece no oponente e se move. O jogador precisa clicar quando o alvo estiver no centro do oponente.

### Mecânica:

- **Clique no centro**: 2.0x de dano
- **Clique perto do centro**: 1.5x de dano
- **Clique longe**: 1.0x de dano
- **Não clicou**: 0.8x de dano

### Vantagens:

✅ **Visualmente interessante** - alvo se move
✅ **Temático** - faz sentido com "mirar" o oponente

### Desvantagens:

❌ Requer mouse (não funciona bem só com teclado)
❌ Mais complexo (detectar posição do clique)

---

## 🎯 Opção 5: **Slider Stop (Parar o Slider)**

### Conceito:

Um slider se move rapidamente de um lado para outro. O jogador precisa apertar ESPAÇO para parar o slider na zona ideal (centro).

### Mecânica:

- **Parou no centro (zona verde)**: 2.0x de dano
- **Parou perto do centro (zona amarela)**: 1.5x de dano
- **Parou nas bordas**: 1.0x de dano

### Vantagens:

✅ **Similar ao Timing Bar** mas com controle do jogador
✅ **Tensão** - decisão de quando parar

### Desvantagens:

❌ Similar ao Timing Bar mas mais difícil de acertar
❌ Pode ser frustrante se o jogador parar muito cedo/tarde

---

## 🏆 **RECOMENDAÇÃO FINAL: Timing Bar (Opção 1)**

### Por quê?

1. **Mais fácil de implementar** - apenas desenhar uma barra e um indicador no canvas
2. **Melhor jogabilidade** - balance entre skill e acessibilidade
3. **Visual claro** - fácil de entender para qualquer jogador
4. **Funciona bem com o som** - pode sincronizar o timing com o som do especial
5. **Não quebra o ritmo** - rápido (2-3 segundos) e direto

### Implementação Sugerida:

```java
// Durante o especial, antes do impacto:
- Mostrar overlay com barra de timing
- Indicador se move em loop (esquerda → direita → esquerda)
- Jogador aperta ESPAÇO para "acertar"
- Calcular multiplicador baseado na posição do indicador
- Passar multiplicador para DamageCalculator
```

### Multiplicadores Sugeridos:

- **Zona Perfeita (centro ±15%)**: 2.0x
- **Zona Boa (centro ±30%)**: 1.5x (normal)
- **Zona Ruim (resto)**: 1.0x (sem bônus)
- **Falha (não apertou)**: 0.8x (penalidade)

### Para CPU:

- IA pode ter diferentes níveis de precisão:
  - Fácil: 70% chance de acertar zona boa
  - Normal: 50% chance de acertar zona perfeita
  - Difícil: 80% chance de acertar zona perfeita

---

## 📝 Próximos Passos (se escolher Timing Bar):

1. Criar classe `SpecialTimingBar` ou método em `PixelBattleView`
2. Mostrar overlay durante fase APPROACH quando `pendingAction == Action.SPECIAL`
3. Detectar tecla ESPAÇO durante o timing
4. Calcular multiplicador baseado na posição
5. Passar multiplicador para `DamageCalculator.compute()` ou criar método separado
6. Adicionar feedback visual (cores, texto "PERFEITO!", "BOM!", etc.)
7. Implementar para CPU com lógica de IA
