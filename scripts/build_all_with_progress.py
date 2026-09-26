#!/usr/bin/env python3
import sys
import time
import subprocess
import os

def render_progress_bar(label, percent, width=30, status="Building"):
    filled = int(width * percent // 100)
    bar = '█' * filled + '░' * (width - filled)
    sys.stdout.write(f"\r  {label:<42} [{bar}] {percent:>3}% | {status:<25}")
    sys.stdout.flush()

def main():
    print("\n=========================================================================")
    print("        NUST Mobility Platform - Sequential Build Orchestrator")
    print("=========================================================================\n")

    apps = [
        {"name": "1. Shared Contracts & Domain", "cmd": "npx pnpm build", "cwd": "."},
        {"name": "2. Backend API Service (apps/api)", "cmd": "npx pnpm check", "cwd": "."},
        {"name": "3. Web PWA Shell (apps/web)", "cmd": "npx prettier --check apps/web/src/index.html", "cwd": "."},
        {"name": "4. Conductor Android App (apps/conductor)", "cmd": "./gradlew assembleDebug --no-daemon", "cwd": "apps/conductor"},
        {"name": "5. Student Android App (apps/student)", "cmd": "./gradlew assembleDebug --no-daemon", "cwd": "apps/student"}
    ]

    total_apps = len(apps)
    for idx, app in enumerate(apps, 1):
        label = app["name"]
        print(f"[{idx}/{total_apps}] Building: {label}...")
        
        process = subprocess.Popen(
            app["cmd"],
            shell=True,
            cwd=os.path.abspath(app["cwd"]),
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE
        )

        progress = 5
        render_progress_bar(label, progress, status="Starting build...")

        # Animate progress bar dynamically while process runs
        while process.poll() is None:
            time.sleep(0.3)
            if progress < 90:
                progress += 5
            render_progress_bar(label, progress, status="Compiling & Linking...")

        stdout, stderr = process.communicate()

        if process.returncode == 0:
            render_progress_bar(label, 100, status="✓ SUCCESS")
            print()
        else:
            render_progress_bar(label, progress, status="✗ FAILED")
            print(f"\nError details:\n{stderr.decode('utf-8')}")
            sys.exit(1)

    print("\n=========================================================================")
    print("        ✓ ALL APPLICATIONS BUILT SUCCESSFULLY!")
    print("=========================================================================\n")

if __name__ == "__main__":
    main()
