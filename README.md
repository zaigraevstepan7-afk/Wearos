# Google Поиск — Wear OS

Приложение поиска Google для часов на **Wear OS** (Wear OS 3+, например OnePlus Watch 2 /
часы на платформе Wear OS). Позволяет искать в Google прямо с запястья: голосом, с
клавиатуры или из истории недавних запросов. Результаты открываются во встроенном
браузере (WebView) в облегчённой версии страницы Google, удобной для маленького экрана.

> Примечание: «OnePlus Ace 6» — это смартфон, а не часы. Это приложение собрано для
> **Wear OS**, поэтому оно установится на любые часы под управлением Wear OS 3+
> (minSdk 30). Для часов на ColorOS Watch / RTOS оно не подойдёт — там нет среды
> исполнения Android-приложений.

## Возможности

- 🎙️ **Голосовой поиск** — системное распознавание речи (`RecognizerIntent`).
- ⌨️ **Ввод с клавиатуры / рукописный ввод** — системный текстовый ввод Wear OS
  (`RemoteInput`).
- 🕘 **История запросов** — последние 10 запросов сохраняются локально и доступны
  одним касанием.
- 🌐 **Результаты внутри приложения** — Google открывается в `WebView` (облегчённая
  верстка `igu=1`), навигация назад работает через свайп/кнопку.
- ⌚ **Standalone-приложение** — работает без телефона, через Wi‑Fi/LTE часов.
- 🎨 Тема и иконка в цветах Google, Material-компоненты для Wear (`androidx.wear.compose`).

## Технологии

| Слой | Что используется |
|------|------------------|
| UI | Jetpack Compose for Wear OS (`androidx.wear.compose:*`) |
| Навигация | `SwipeDismissableNavHost` |
| Ввод | `RecognizerIntent`, `androidx.wear:wear-input` (`RemoteInput`) |
| Результаты | `WebView` |
| Хранилище | `SharedPreferences` |
| Язык / сборка | Kotlin 2.0, AGP 8.5, Gradle 8.9, JDK 17 |

## Структура проекта

```
app/src/main/java/com/oneplus/watchsearch/
├── MainActivity.kt              # точка входа, splash + тема
├── data/
│   ├── GoogleSearch.kt          # построение URL поиска
│   └── SearchHistory.kt         # история запросов (SharedPreferences)
└── ui/
    ├── SearchApp.kt             # навигация между экранами
    ├── SearchScreen.kt          # главный экран (ввод + история)
    ├── SearchInput.kt           # голос + клавиатура (Activity Result)
    ├── ResultsScreen.kt         # WebView с результатами Google
    └── theme/Theme.kt           # тема Wear Material
```

## Сборка

Требуется Android SDK (platform 34, build-tools 34) и JDK 17.

```bash
# Debug APK
./gradlew :app:assembleDebug
# → app/build/outputs/apk/debug/app-debug.apk
```

CI (GitHub Actions, `.github/workflows/build.yml`) автоматически собирает debug APK
и публикует его как артефакт сборки при каждом push.

## Установка на часы

1. Включите на часах режим разработчика и **отладку по ADB** (по Wi‑Fi или USB).
2. Подключитесь: `adb connect <ip-часов>:5555` (или через USB-хаб).
3. Установите: `adb install -r app/build/outputs/apk/debug/app-debug.apk`.
4. Запустите «Google Поиск» из списка приложений.

## Минимальные требования

- Wear OS 3.0+ (API 30+)
- Доступ в интернет (Wi‑Fi или LTE на часах)
- На устройстве должны быть Сервисы Google Play (для голосового ввода)
