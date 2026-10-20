# 📸 GalleryApp (Моменты)

Учебное Android-приложение на Kotlin и Jetpack Compose для создания личной коллекции фотографий.

---

## 🛠 Предварительные требования

Для сборки проекта из консоли вам понадобятся:

*   **Git** — для клонирования репозитория.
*   **JDK 17** — проект использует Android Gradle Plugin 8.7.3, который требует именно 17-ю версию Java.
*   **Android SDK** — платформа Android 15 (API 35) и Build-Tools.

> ⚠️ **Важно (Windows):** Убедитесь, что в путях к JDK и Android SDK **нет кириллицы**. Gradle читает `.properties` в ISO-8859-1 и превращает `Комп` в `?ом?`. Если у вас учётка `C:\Users\Иван`, скопируйте JDK и SDK в чистые пути: `C:\Java\jdk17` и `C:\Android\Sdk`.

---

## ⚡ Быстрый старт (одна команда)

В корне репозитория уже лежат два готовых скрипта:

| Файл | ОС |
|---|---|
| `setup_windows.bat` | Windows (CMD) |
| `setup_linux.sh` | Linux / macOS |

**Windows:**

```cmd
setup_windows.bat
```

**Linux:**

```bash
chmod +x setup_linux.sh
./setup_linux.sh
```

Скрипт сам:
1. Проверит наличие JDK 17 и Android SDK.
2. Создаст `gradle.properties` и `local.properties` с правильными путями.
3. Соберёт `app-debug.apk`.
4. (Опционально) Установит APK на подключённый телефон через `adb`.

---

## 📄 Содержимое скриптов (можно создать вручную)

### 🪟 `setup_windows.bat`

Создайте файл `setup_windows.bat` в корне репозитория и вставьте:

```bat
@echo off
setlocal EnableDelayedExpansion
chcp 65001 >nul

REM ============================================================
REM  GalleryApp build script for Windows
REM  Запуск: setup_windows.bat
REM ============================================================

REM ---------- НАСТРОЙКИ (можно переопределить) ---------------
if not defined JDK17 set "JDK17=C:\Java\jdk17"
if not defined SDK   set "SDK=C:\Android\Sdk"
if not defined REPO  set "REPO=%~dp0"
REM -----------------------------------------------------------

echo.
echo === 1. Проверка JDK 17 ===
if not exist "%JDK17%\bin\java.exe" (
    echo [ERROR] JDK 17 не найден: %JDK17%\bin\java.exe
    echo Установите: winget install EclipseAdoptium.Temurin.17.JDK
    echo Затем скопируйте папку JDK в %JDK17% ^(без кириллицы в пути^).
    exit /b 1
)
"%JDK17%\bin\java.exe" -version 2>&1 | findstr /C:"17." >nul
if errorlevel 1 (
    echo [ERROR] По пути %JDK17% не JDK 17.
    exit /b 1
)
echo [OK] JDK 17: %JDK17%

echo.
echo === 2. Проверка Android SDK ===
if not exist "%SDK%\platforms\android-35\android.jar" (
    echo [ERROR] Android SDK 35 не найден: %SDK%
    echo Откройте Android Studio -^> SDK Manager и установите:
    echo   - Android 15 ^(API 35^)
    echo   - Android SDK Build-Tools
    echo   - Android SDK Platform-Tools
    exit /b 1
)
echo [OK] Android SDK: %SDK%

echo.
echo === 3. gradle.properties ===
pushd "%REPO%"
if exist gradle.properties (
    findstr /B /C:"org.gradle.java.home=" gradle.properties >nul
    if not errorlevel 1 (
        powershell -NoProfile -Command "(Get-Content gradle.properties) -notmatch '^org.gradle.java.home=' | Set-Content gradle.properties -Encoding ASCII"
    )
)
echo org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8>> gradle.properties
echo android.useAndroidX=true>> gradle.properties
echo kotlin.code.style=official>> gradle.properties
echo org.gradle.java.home=%JDK17:\=\\%>> gradle.properties
echo [OK] gradle.properties

echo.
echo === 4. local.properties ===
echo sdk.dir=%SDK:\=\\%> local.properties
echo [OK] local.properties

echo.
echo === 5. Сборка APK ===
set "JAVA_HOME=%JDK17%"
set "PATH=%JDK17%\bin;%PATH%"
call gradlew.bat :app:assembleDebug
if errorlevel 1 (
    echo [ERROR] Сборка не удалась.
    popd
    exit /b 1
)
echo [OK] APK: %REPO%app\build\outputs\apk\debug\app-debug.apk

echo.
echo === 6. Установка на устройство (если подключено) ===
if not exist "%SDK%\platform-tools\adb.exe" (
    echo [SKIP] adb не найден - пропускаем установку.
    popd
    exit /b 0
)
"%SDK%\platform-tools\adb.exe" devices | findstr /R /C:"\sdevice$" >nul
if errorlevel 1 (
    echo [SKIP] Устройство не подключено или unauthorized.
    echo        Установить вручную: "%SDK%\platform-tools\adb.exe" install -r app\build\outputs\apk\debug\app-debug.apk
    popd
    exit /b 0
)
"%SDK%\platform-tools\adb.exe" install -r "%REPO%app\build\outputs\apk\debug\app-debug.apk"
"%SDK%\platform-tools\adb.exe" shell monkey -p ru.mirea.gallery -c android.intent.category.LAUNCHER 1
echo [OK] Приложение установлено и запущено.

popd
endlocal
```

### 🐧 `setup_linux.sh`

Создайте файл `setup_linux.sh` в корне репозитория и вставьте:

```bash
#!/usr/bin/env bash
# ============================================================
#  GalleryApp build script for Linux / macOS
#  Запуск: chmod +x setup_linux.sh && ./setup_linux.sh
# ============================================================
set -euo pipefail

# ---------- НАСТРОЙКИ (можно переопределить) ---------------
JDK17="${JDK17:-/usr/lib/jvm/java-17-openjdk-amd64}"
SDK="${SDK:-$HOME/Android/Sdk}"
REPO="$(cd "$(dirname "$0")" && pwd)"
# -----------------------------------------------------------

echo
echo "=== 1. Проверка JDK 17 ==="
if [ ! -x "$JDK17/bin/java" ]; then
    echo "[ERROR] JDK 17 не найден: $JDK17/bin/java"
    echo "Установите: sudo apt install openjdk-17-jdk"
    exit 1
fi
"$JDK17/bin/java" -version 2>&1 | grep -q '17\.' || {
    echo "[ERROR] По пути $JDK17 не JDK 17"; exit 1;
}
echo "[OK] JDK 17: $JDK17"

echo
echo "=== 2. Проверка Android SDK ==="
if [ ! -f "$SDK/platforms/android-35/android.jar" ]; then
    echo "[ERROR] Android SDK 35 не найден: $SDK"
    echo "Установите: sdkmanager \"platforms;android-35\" \"build-tools;35.0.0\" \"platform-tools\""
    exit 1
fi
echo "[OK] Android SDK: $SDK"

cd "$REPO"

echo
echo "=== 3. gradle.properties ==="
if [ -f gradle.properties ]; then
    sed -i.bak '/^org\.gradle\.java\.home=/d' gradle.properties
fi
{
    echo "org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8"
    echo "android.useAndroidX=true"
    echo "kotlin.code.style=official"
    echo "org.gradle.java.home=$JDK17"
} >> gradle.properties
echo "[OK] gradle.properties"

echo
echo "=== 4. local.properties ==="
echo "sdk.dir=$SDK" > local.properties
echo "[OK] local.properties"

echo
echo "=== 5. Сборка APK ==="
export JAVA_HOME="$JDK17"
export PATH="$JDK17/bin:$PATH"
chmod +x gradlew
./gradlew :app:assembleDebug
echo "[OK] APK: $REPO/app/build/outputs/apk/debug/app-debug.apk"

echo
echo "=== 6. Установка на устройство (если подключено) ==="
if command -v adb >/dev/null 2>&1; then
    if adb devices | awk 'NR>1 && $2=="device"' | grep -q .; then
        adb install -r "$REPO/app/build/outputs/apk/debug/app-debug.apk"
        adb shell monkey -p ru.mirea.gallery -c android.intent.category.LAUNCHER 1
        echo "[OK] Приложение установлено и запущено."
    else
        echo "[SKIP] Устройство не подключено или unauthorized."
    fi
else
    echo "[SKIP] adb не найден."
fi
```

---

## 📖 Ручная инструкция (если скрипт не подходит)

### 🪟 Windows

**Шаг 1. Установить JDK 17**

```powershell
winget install EclipseAdoptium.Temurin.17.JDK
```

Затем **скопируйте** папку установки (например, `C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot`) в `C:\Java\jdk17` — так вы гарантированно избежите проблем с кириллицей.

**Шаг 2. Установить Android SDK**

1. Установите [Android Studio](https://developer.android.com/studio).
2. Откройте **SDK Manager** (иконка с кубиком).
3. **SDK Platforms** → отметьте **Android 15 (API 35)**.
4. **SDK Tools** → отметьте **Build-Tools**, **Platform-Tools**, **Command-line Tools**.
5. **Apply** → дождитесь загрузки.
6. Скопируйте папку `%LOCALAPPDATA%\Android\Sdk` в `C:\Android\Sdk`.

**Шаг 3. Клонировать проект**

```cmd
cd C:\
mkdir Projects
cd Projects
git clone https://github.com/AchGerGov/Gallery.git
cd Gallery\GalleryApp
```

**Шаг 4. Настроить пути**

```cmd
echo org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8>> gradle.properties
echo android.useAndroidX=true>> gradle.properties
echo kotlin.code.style=official>> gradle.properties
echo org.gradle.java.home=C:\\Java\\jdk17>> gradle.properties
echo sdk.dir=C:\\Android\\Sdk> local.properties
```

**Шаг 5. Собрать APK**

```cmd
set "JAVA_HOME=C:\Java\jdk17"
set "PATH=%JAVA_HOME%\bin;%PATH%"
gradlew.bat :app:assembleDebug
```

**Шаг 6. Установить на телефон**

```cmd
set "ADB=C:\Android\Sdk\platform-tools\adb.exe"
"%ADB%" devices
"%ADB%" install -r app\build\outputs\apk\debug\app-debug.apk
"%ADB%" shell monkey -p ru.mirea.gallery -c android.intent.category.LAUNCHER 1
```

### 🐧 Linux (Ubuntu / Debian)

**Шаг 1. Установить зависимости**

```bash
sudo apt update
sudo apt install git openjdk-17-jdk -y
```

**Шаг 2. Установить Android SDK (без Android Studio)**

```bash
mkdir -p ~/Android/Sdk/cmdline-tools
cd ~/Android/Sdk/cmdline-tools
wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O cmdline-tools.zip
unzip cmdline-tools.zip
mv cmdline-tools latest
rm cmdline-tools.zip

export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
export PATH=$PATH:$ANDROID_HOME/platform-tools

sdkmanager --licenses
sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0"
```

**Шаг 3. Клонировать проект**

```bash
mkdir -p ~/Projects && cd ~/Projects
git clone https://github.com/AchGerGov/Gallery.git
cd Gallery/GalleryApp
```

**Шаг 4. Настроить пути**

```bash
echo "org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8" >> gradle.properties
echo "android.useAndroidX=true" >> gradle.properties
echo "kotlin.code.style=official" >> gradle.properties
echo "org.gradle.java.home=/usr/lib/jvm/java-17-openjdk-amd64" >> gradle.properties
echo "sdk.dir=$HOME/Android/Sdk" > local.properties
```

**Шаг 5. Собрать APK**

```bash
chmod +x gradlew
./gradlew :app:assembleDebug
```

**Шаг 6. Установить на телефон**

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell monkey -p ru.mirea.gallery -c android.intent.category.LAUNCHER 1
```

---

## 💡 Решение частых проблем

| Ошибка | Что делать |
|---|---|
| `SDK location not found` | Проверьте `local.properties`. На Windows — двойные слэши: `sdk.dir=C:\\Android\\Sdk` |
| `Value '...' given for org.gradle.java.home Gradle property is invalid` | В пути к JDK кириллица (`?ом?`) или пробел. Скопируйте JDK в `C:\Java\jdk17` и перепишите `org.gradle.java.home` |
| `Failed to install ... licenses have not been accepted` | Выполните `sdkmanager --licenses` и нажимайте `y` |
| `device unauthorized` | Разблокируйте телефон и нажмите «Разрешить» в диалоге «Разрешить отладку по USB?» |
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | Удалите старое: `adb uninstall ru.mirea.gallery`, установите заново |
| `Could not resolve ...` / таймауты | Нет доступа к `maven.google.com`, `repo.maven.apache.org` — проверьте VPN/прокси |
| `Shell does not have permission to access user 999` (Xiaomi) | Это баг MIUI. Используйте `adb shell pm list packages --user 0` и `adb shell am start --user 0 -n ...` |
