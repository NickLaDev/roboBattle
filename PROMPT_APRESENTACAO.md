# PROMPT PARA GERAÇÃO DE SLIDES - BATTLE DOLLS

Crie uma apresentação de slides profissional sobre o jogo **Battle Dolls**, um jogo de arena de personagens em pixel art desenvolvido em Java/JavaFX. Use o seguinte conteúdo:

## SLIDE 1: TÍTULO
**Battle Dolls**
Arena de Personagens em Pixel Art
[Subtítulo: Projeto de Jogo em Java/JavaFX]

## SLIDE 2: CONCEITO DO JOGO
**A Ideia:**
- Jogo de duelo 1×1 em pixel art estilo arena
- Cada jogador escolhe um personagem base com habilidades únicas
- Sistema de customização: armas, armaduras e poções (níveis 1-5)
- Combate estratégico por turnos com decisões táticas
- Dois modos de jogo: **PvP** (Player vs Player) e **PvC** (Player vs CPU/Campanha)

**Objetivo:** Derrotar o oponente reduzindo seu HP a zero através de ataques, defesas e habilidades especiais.

## SLIDE 3: PERSONAGENS E CLASSES
**Sistema de Personagens:**
- **Beatriz** - Espadachim solar focada em golpes críticos e agressivos
  - Habilidade: Golpe Solar (70% de dano extra)
  
- **Yuri** - Guardião prismático com alta defesa e HP
  - Habilidade: Escudo Prismático (cura 35 HP + ativa defesa)
  
- **Shinobi** - Assassino ágil com esquivas e veneno
  - Habilidade: Lâminas Envenenadas (aplica sangramento garantido)

Cada personagem tem atributos base únicos (ATK, DEF, HP, CRIT%, EVA%) e uma habilidade especial única.

## SLIDE 4: SISTEMA DE EQUIPAMENTOS
**Customização de Personagens:**
- **Armas** (níveis 1-5): Aumentam ATK e chance de crítico
- **Armaduras** (níveis 1-5): Aumentam DEF e HP máximo
- **Sistema de Créditos**: Jogadores começam com 1000 créditos para comprar equipamentos
- **Loja**: Interface visual para seleção de personagem, arma e armadura antes da batalha

Custo dos equipamentos aumenta exponencialmente com o nível, exigindo decisões estratégicas de investimento.

## SLIDE 5: SISTEMA DE POÇÕES
**Poções Consumíveis (4 tipos, 5 níveis cada):**
- **Poção de Vida**: Restaura HP (35-95 pontos conforme nível)
- **Poção de Barreira**: Adiciona escudo temporário (25-65 pontos)
- **Poção de Energia**: Recupera carga especial (35-95 pontos)
- **Poção de Fúria**: Aumenta ataque em 15-75% por 3-7 turnos

**Mecânica:**
- Compradas em loja separada após equipamentos
- Usadas durante a batalha através de inventário lateral
- Consomem o turno do jogador
- CPU também usa poções estrategicamente

## SLIDE 6: MECÂNICAS DE COMBATE
**Sistema de Batalha por Turnos:**
- **ATTACK**: Ataque básico com dano baseado em ATK vs DEF
- **DEFEND**: Reduz próximo dano recebido pela metade + gera carga especial
- **SPECIAL**: Habilidade única do personagem (requer 100% de carga)

**Mecânicas Avançadas:**
- **Crítico**: Chance baseada em CRIT% com multiplicador 1.5x
- **Esquiva**: Chance baseada em EVA% de evitar completamente o dano
- **Sangramento**: Aplicado por críticos, causa dano por turno
- **Escudo/Barreira**: Absorve dano antes do HP
- **Fúria**: Multiplicador de ataque temporário (de poções)

## SLIDE 7: MODO CAMPANHA (PvC)
**Sistema de Fases:**
- **4 Fases Progressivas** com dificuldade crescente
- **Bosses Únicos**: Converted Vampire, Vampire Girl, Samurai Commander, Countess Vampire
- **Recompensas**: Créditos ganhos após cada vitória (200-500 créditos)
- **Sistema de Upgrade**: Entre fases, jogador pode melhorar equipamentos e comprar poções
- **IA Inteligente**: CPU usa poções e estratégias baseadas em situação (HP baixo, especial disponível, etc.)

**Progressão:**
1. Vitória → Ganha créditos
2. Loja de Upgrade → Melhora equipamentos
3. Loja de Poções → Compra consumíveis
4. Próxima Fase → Novo boss mais difícil

## SLIDE 8: RECURSOS VISUAIS E SONOROS
**Pixel Art Animado:**
- Spritesheets completas para cada personagem (Idle, Attack 1-3, Run, Defend, Hurt, Death)
- Animações fluidas durante batalha
- Efeitos visuais para eventos de batalha (crítico, esquiva, defesa, sangramento, poções)
- Textos flutuantes informativos

**Sistema de Áudio:**
- Música temática para menu e batalhas
- Músicas únicas para cada boss da campanha
- Efeitos sonoros para ataques, defesas e habilidades especiais
- Controle de volume separado para música e SFX

## SLIDE 9: O QUE JÁ FOI IMPLEMENTADO ✅
**Funcionalidades Completas:**
- ✅ Sistema completo de personagens e classes
- ✅ Modo PvP (Player vs Player) funcional
- ✅ Modo PvC com campanha de 4 fases
- ✅ Sistema de loja de equipamentos (armas e armaduras)
- ✅ Sistema de loja de poções
- ✅ Sistema de inventário durante batalha
- ✅ IA para CPU com lógica estratégica
- ✅ Sistema de créditos e recompensas
- ✅ Animações de sprites e efeitos visuais
- ✅ Sistema de áudio completo
- ✅ Interface gráfica completa (seleção de modo, loja, batalha)
- ✅ Cálculo de dano com todas as mecânicas (crítico, esquiva, defesa, sangramento, fúria)

## SLIDE 10: O QUE AINDA SERÁ FEITO 🔄
**Melhorias e Expansões Futuras:**
- 🔄 Adicionar mais personagens jogáveis (expandir de 3 para 5-6)
- 🔄 Adicionar mais fases na campanha (expandir de 4 para 8-10)
- 🔄 Sistema de save/load de progresso
- 🔄 Sistema de ranking/estatísticas
- 🔄 Modo de dificuldade (Fácil, Normal, Difícil)
- 🔄 Mais tipos de poções e efeitos especiais
- 🔄 Sistema de conquistas/achievements
- 🔄 Melhorias na IA (níveis de dificuldade diferentes)
- 🔄 Polimento visual (mais efeitos, transições)
- 🔄 Balanceamento fino de atributos e custos

## SLIDE 11: TECNOLOGIAS UTILIZADAS
**Stack Tecnológico:**
- **Java 17+**: Linguagem principal
- **JavaFX**: Framework para interface gráfica
- **Gradle**: Gerenciamento de dependências e build
- **Pixel Art**: Assets visuais customizados
- **Arquitetura Modular**: Separação clara entre Modelo, Regras de Combate e UI

**Estrutura do Projeto:**
- Modelo: Personagens, Equipamentos, Poções, Stats
- Combate: Engine de batalha, Calculadora de dano, IA
- UI: Telas de jogo, Animações, Efeitos visuais
- Áudio: Gerenciador de música e SFX

## SLIDE 12: CONCLUSÃO
**Battle Dolls** é um jogo completo e funcional que demonstra:
- Desenvolvimento de jogos 2D em Java/JavaFX
- Sistema de combate estratégico por turnos
- IA para oponentes controlados por CPU
- Sistema de progressão e customização
- Interface gráfica polida com pixel art

**Status Atual:** Jogo jogável e completo nos modos PvP e PvC, pronto para expansões futuras.

---

**INSTRUÇÕES PARA A IA:**
- Crie slides visuais e profissionais
- Use cores que combinem com o tema de pixel art (tons escuros com acentos vibrantes como roxo #BB86FC, dourado #FFD700)
- Inclua ícones e elementos visuais quando apropriado
- Mantenha o texto conciso e direto
- Use emojis ou símbolos para destacar seções (✅ para completo, 🔄 para futuro)
- Faça slides informativos mas não sobrecarregados



