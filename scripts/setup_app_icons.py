#!/usr/bin/env python3
import os
import sys
from PIL import Image

logo_path = "/home/alpsa/.gemini/antigravity-ide/brain/4a12c3ec-ead9-4d99-bd5d-a9b77f679ee8/mundo_logo_1790416082167.png"

if not os.path.exists(logo_path):
    print(f"Error: Logo file not found at {logo_path}")
    sys.exit(1)

img = Image.open(logo_path)

# Mipmap densities and sizes
densities = {
    "mipmap-mdpi": (48, 48),
    "mipmap-hdpi": (72, 72),
    "mipmap-xhdpi": (96, 96),
    "mipmap-xxhdpi": (144, 144),
    "mipmap-xxxhdpi": (192, 192),
    "drawable": (512, 512)
}

app_dirs = [
    "/home/alpsa/Desktop/NUST-Mobility-Platform/apps/conductor/app/src/main/res",
    "/home/alpsa/Desktop/NUST-Mobility-Platform/apps/student/app/src/main/res"
]

for app_res in app_dirs:
    for folder, size in densities.items():
        target_dir = os.path.join(app_res, folder)
        os.makedirs(target_dir, exist_ok=True)
        resized_img = img.resize(size, Image.Resampling.LANCZOS)
        resized_img.save(os.path.join(target_dir, "ic_launcher.png"), "PNG")
        resized_img.save(os.path.join(target_dir, "ic_launcher_round.png"), "PNG")

# Also save web logo asset
web_img_dir = "/home/alpsa/Desktop/NUST-Mobility-Platform/apps/web/src"
os.makedirs(web_img_dir, exist_ok=True)
img.resize((512, 512), Image.Resampling.LANCZOS).save(os.path.join(web_img_dir, "mundo_logo.png"), "PNG")

print("Successfully generated and installed Mundo app icons and web assets!")
