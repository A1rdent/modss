# Gradle Wrapper Setup

This project uses Gradle Wrapper for building. To set up the project correctly:

## Option 1: Using Pre-generated Gradle Wrapper (RECOMMENDED)

If you have a Minecraft Forge 1.20.1 MDK (Mod Development Kit), copy these files from the MDK to your CompanionMod directory:
- `gradlew` (Unix/Linux/macOS script)
- `gradlew.bat` (Windows batch script)
- `gradle/wrapper/gradle-wrapper.jar`
- `gradle/wrapper/gradle-wrapper.properties`

## Option 2: Generate Gradle Wrapper from Scratch

1. Install Gradle 7.0+ on your system
2. Run: `gradle wrapper --gradle-version=7.6`
3. This will generate all necessary wrapper files

## Option 3: Download Pre-built Files

You can download the Forge 1.20.1 MDK from:
https://files.minecraftforge.net/

Then extract the gradle wrapper files from there.

## Expected Directory Structure After Setup

```
CompanionMod/
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradlew (or gradlew.bat for Windows)
├── build.gradle
├── gradle.properties
└── ... (other files)
```

## Building After Setup

Once gradle wrapper is installed, use:

```bash
# Unix/Linux/macOS
./gradlew build

# Windows
gradlew.bat build

# Or on Windows PowerShell
.\gradlew.bat build
```

## Alternative: Use system Gradle

If you have Gradle installed system-wide:

```bash
gradle build
```

## Troubleshooting

### "gradlew not recognized"
- Ensure gradlew.bat is in the CompanionMod root directory
- On Windows, try: `.\gradlew.bat build`
- On Unix/Linux/macOS: `./gradlew build`

### "Gradle not found"
- Install Gradle 7.0+
- Or set up the wrapper files (see above)

### Java version mismatch
- Ensure Java 17+ is installed: `java -version`
- The build.gradle is configured for Java 17

### Out of memory during build
- Increase gradle memory: `export GRADLE_OPTS=-Xmx4G` (Unix/Linux/macOS)
- Or edit gradle.properties: `org.gradle.jvmargs=-Xmx4G`

## First Build Steps

1. Download and extract Forge 1.20.1 MDK
2. Copy gradle wrapper files to CompanionMod directory
3. Run: `gradlew.bat genSources` (Windows) or `./gradlew genSources` (Unix/Linux/macOS)
4. Run: `gradlew.bat build` to compile the mod

## Getting Forge MDK

Visit: https://files.minecraftforge.net/

Search for "Forge 47.3.0" (for Minecraft 1.20.1)

Download the "Installer" or "MDK" version.
