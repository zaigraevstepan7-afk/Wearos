# ИИ Чат — Wear OS

Чат с нейросетью (как ChatGPT / Gemini / Claude) прямо на часах под управлением
**Wear OS** / Android-часах. Вопрос можно ввести голосом или текстом, ответ
показывается родным списком на экране часов с прокруткой колёсиком/безелем.
Никакого WebView и браузера — всё рисуется нативно.

## Возможности

- 🤖 **Три нейросети на выбор:** Google **Gemini**, **ChatGPT** (OpenAI),
  **Claude** (Anthropic). Переключаются в настройках.
- 🔑 **Свой API-ключ** — хранится локально на часах (по ключу на каждого
  провайдера). Можно ввести на часах или прислать с компьютера через adb.
- 🎙️ **Голосовой ввод** + встроенное текстовое поле (работают без браузера).
- 💬 **Диалог с контекстом** — история сообщений отправляется в модель.
- 🎡 **Прокрутка короной/безелем** (`rotaryScrollable`).
- 🛟 **Экран отчёта о краше** — при любой ошибке показывает стек на экране.

## Провайдеры и модели

| Провайдер | Модель по умолчанию | Ключ |
|-----------|---------------------|------|
| Gemini | `gemini-2.0-flash` | бесплатно: https://aistudio.google.com/app/apikey |
| ChatGPT | `gpt-4o-mini` | платно: https://platform.openai.com/api-keys |
| Claude | `claude-haiku-4-5` | платно: https://console.anthropic.com |

Модели заданы в `data/AiProvider.kt` — поменять можно одной строкой.

## Как задать ключ

**На часах:** открой приложение → экран «Нейросеть» → выбери провайдера →
вставь ключ → «Сохранить».

**С компьютера (удобнее для длинного ключа):**
```bash
adb shell am broadcast -n com.oneplus.watchsearch/.KeyReceiver \
    --es provider GEMINI --es key "ВАШ_КЛЮЧ"
```
(`provider` = `GEMINI` | `OPENAI` | `CLAUDE`)

## Структура

```
app/src/main/java/com/oneplus/watchsearch/
├── MainActivity.kt          точка входа
├── App.kt / CrashActivity   глобальный отчёт о крашах
├── KeyReceiver.kt           установка ключа через adb
├── data/
│   ├── AiProvider.kt        провайдеры и модели
│   ├── Settings.kt          провайдер + ключи (SharedPreferences)
│   ├── ChatMessage.kt       модель сообщения
│   └── AiClient.kt          HTTP-запросы к Gemini/OpenAI/Claude
└── ui/
    ├── ChatApp.kt           настройки ↔ чат
    ├── ChatScreen.kt        диалог + ввод
    ├── SettingsScreen.kt    выбор сети + ключ
    ├── SearchInput.kt       голос / системная клавиатура
    └── theme/Theme.kt
```

## Сборка

Требуется Android SDK (platform 34, build-tools 34) и JDK 17.
```bash
./gradlew :app:assembleDebug
# → app/build/outputs/apk/debug/app-debug.apk
```
CI (`.github/workflows/build.yml`) собирает debug APK на каждый push.

## Минимальные требования

- Android-часы (Wear OS 3+ / API 30+) с доступом в интернет
- API-ключ выбранной нейросети
