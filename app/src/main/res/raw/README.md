# Placeholder Sound Files

This directory contains placeholder OGG files for WakeForge's built-in alarm sounds.

## Required Files (replace with real audio before production)

| File | Description |
|------|-------------|
| `builtin_dawn.ogg` | Soft, gentle wake-up tone (dawn theme) |
| `builtin_rise.ogg` | Ascending, energizing melody |
| `builtin_forge.ogg` | Bold, industrial ringtone |
| `builtin_crystal.ogg` | Clear, crystalline chime |
| `builtin_digital.ogg` | Classic digital alarm beep |

## Notes

- These placeholder files contain only the OGG magic header bytes and will NOT play audio.
- Replace each file with a real OGG Vorbis audio file (recommended: 44.1kHz mono, <500KB).
- OGG Vorbis is the recommended format for Android `RingtoneManager`.
- Use a tool like Audacity or ffmpeg to convert audio:
  ```
  ffmpeg -i input.mp3 -c:a libvorbis -q:a 4 builtin_dawn.ogg
  ```

## Current files

The current files are minimal placeholders created to allow the project to compile.
R.raw references in code will resolve at compile time, but attempting to play these
files at runtime will produce no audible output.
