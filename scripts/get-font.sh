#!/usr/bin/env bash
# Downloads the Sora variable font into the app assets. Optional: without it the app uses the system font.
set -u
DEST="$(cd "$(dirname "$0")/.." && pwd)/app/src/main/assets/fonts/sora.ttf"
mkdir -p "$(dirname "$DEST")"

URLS=(
  "https://github.com/google/fonts/raw/main/ofl/sora/Sora%5Bwght%5D.ttf"
  "https://cdn.jsdelivr.net/gh/google/fonts@main/ofl/sora/Sora%5Bwght%5D.ttf"
)

for url in "${URLS[@]}"; do
  echo "Trying $url"
  if curl -fsSL --retry 2 --max-time 60 -o "$DEST.tmp" "$url"; then
    size=$(wc -c < "$DEST.tmp")
    magic=$(head -c 4 "$DEST.tmp" | od -An -tx1 | tr -d ' \n')
    # valid TrueType starts with 00010000 (or "true"/OTTO) and a variable font is well over 40 KB
    if [ "$size" -gt 40000 ] && { [ "$magic" = "00010000" ] || [ "$magic" = "74727565" ] || [ "$magic" = "4f54544f" ]; }; then
      mv "$DEST.tmp" "$DEST"
      echo "Saved font ($size bytes) -> $DEST"
      exit 0
    fi
  fi
  rm -f "$DEST.tmp"
done

echo "WARNING: could not download Sora, the app will use the system font."
exit 0
