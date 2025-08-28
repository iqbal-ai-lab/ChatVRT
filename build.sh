#!/data/data/com.termux/files/usr/bin/bash
# Script otomatis build dan copy APK

# Masuk ke folder project
cd /storage/emulated/0/Download/ChatVRT || exit

echo "[1/4] Kasih permission ke gradlew..."
chmod +x gradlew

echo "[2/4] Build APK Debug..."
./gradlew assembleDebug

# Lokasi output APK
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"

# Cek kalau APK berhasil dibuat
if [ -f "$APK_PATH" ]; then
    echo "[3/4] APK berhasil dibuat: $APK_PATH"
    
    # Copy ke folder Download biar gampang dicari
    cp "$APK_PATH" /storage/emulated/0/Download/
    echo "[4/4] APK sudah disalin ke folder Download/"
    
    echo ">>> Selesai! Silakan install manual dari file manager."
else
    echo ">>> Gagal build APK, cek error di atas!"
fi
