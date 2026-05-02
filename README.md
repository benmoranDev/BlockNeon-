# Block Neon 🎮

Jogo de blocos no estilo Tetris/Lumines com visual neon para Android,
desenvolvido com [libGDX](https://libgdx.com/).

As peças caem com efeito neon pulsante, cores vibrantes e animações suaves.
O objetivo é encaixar os blocos e limpar as linhas antes que a tela encha.

---

## 📱 Plataformas

- `core` — Lógica principal do jogo, compartilhada entre todas as plataformas
- `android` — Plataforma Android (requer Android SDK)

---

## 🎮 Controles

| Ação | Gesto |
|---|---|
| Mover peça | Swipe ← → |
| Rotacionar | Tap |
| Derrubar rápido | Fling ↓ |
| Opções | Botão OPTIONS ou tecla O |

---

## ✨ Funcionalidades

- Visual neon com efeito de glow e pulso de luz
- Peças animadas caindo no background (Tetrominos, losangos, cruzes)
- Transição suave entre telas com fade
- Opções de efeitos visuais (Background FX, Pulse Glow)
- Suporte a teclado e touch

---

## 🛠️ Tecnologias

- **Java** + **libGDX**
- `ShapeRenderer` para efeitos neon
- `ExtendViewport` para adaptar a qualquer tamanho de tela
- `BitmapFont` com fontes customizadas via `FontManager`

---

## ⚙️ Como compilar

1. Clone o repositório
   ```bash
   git clone https://github.com/benmoranDev/BlockNeon-.git
   ```
2. Abra no **Android Studio**
3. Aguarde o Sync do Gradle
4. Execute no dispositivo ou emulador Android

---

## 📦 Gradle — Comandos úteis

```bash
./gradlew build              # Compila o projeto completo
./gradlew android:lint       # Valida o projeto Android
./gradlew clean              # Remove as pastas de build
./gradlew test               # Executa os testes unitários
```

Flags úteis:
- `--daemon` — usa o Gradle Daemon para builds mais rápidos
- `--offline` — usa dependências em cache sem internet
- `--refresh-dependencies` — força revalidação de todas as dependências

---

## 📄 Licença

MIT License — sinta-se livre para usar, modificar e distribuir.
