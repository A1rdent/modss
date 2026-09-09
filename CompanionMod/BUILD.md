# Building Companion Mod for Minecraft 1.20.1

## Prerequisites

- **Java Development Kit (JDK) 17 or higher**
  - Download from: https://adoptium.net/ or Oracle JDK
  - Verify: `java -version`

- **Minecraft Forge 1.20.1 MDK (Mod Development Kit)**
  - Download from: https://files.minecraftforge.net/
  - Look for version 47.3.0 for Minecraft 1.20.1

## Quick Start (Recommended Approach)

### Step 1: Get Forge MDK
1. Visit https://files.minecraftforge.net/
2. Find "Forge 47.3.0" for Minecraft 1.20.1
3. Download the "MDK" version

### Step 2: Extract and Prepare
1. Extract the Forge MDK to a temporary directory
2. Copy these files/folders from the MDK to your CompanionMod directory:
   - `gradle/` (entire folder)
   - `gradlew`
   - `gradlew.bat`
   - `.gitignore` (optional)

### Step 3: Setup Sources
```bash
cd CompanionMod
./gradlew genSources      # On Unix/Linux/macOS
# OR
gradlew.bat genSources    # On Windows PowerShell
```

### Step 4: Build
```bash
./gradlew build           # On Unix/Linux/macOS
# OR
gradlew.bat build         # On Windows PowerShell
```

The compiled JAR will be in: `build/libs/CompanionMod-1.0.0.jar`

## Detailed Build Instructions

### Option A: Using IntelliJ IDEA (Easiest)

1. Open IntelliJ IDEA
2. File → Open → Select CompanionMod folder
3. When prompted, configure Gradle:
   - Select "Use Gradle from..." → "gradle-wrapper.properties"
   - Java version: 17 or higher
4. Wait for indexing to complete
5. Run → Edit Configurations
6. Add Gradle configuration:
   - Name: "Build Companion Mod"
   - Gradle Project: CompanionMod
   - Tasks: `build`
7. Click "Run" or use Run → Run 'Build Companion Mod'

### Option B: Using Eclipse

1. Import as Gradle project
2. Right-click project → Gradle → Refresh Gradle Project
3. Right-click project → Run As → Gradle Build
4. Type "build" in the tasks field
5. Run

### Option C: Command Line

Windows (PowerShell):
```powershell
cd C:\Users\YourName\MinecraftMods\CompanionMod
.\gradlew.bat build
```

Windows (Command Prompt):
```cmd
cd C:\Users\YourName\MinecraftMods\CompanionMod
gradlew.bat build
```

Linux/macOS:
```bash
cd ~/MinecraftMods/CompanionMod
./gradlew build
```

## Gradle Wrapper JAR File

The `gradle/wrapper/gradle-wrapper.jar` file is not included in the repository because:
1. It's a binary file and repositories prefer source code
2. The gradlew scripts will automatically download it on first run

**First build will take longer** (5-15 minutes) because it:
1. Downloads Gradle 8.4
2. Downloads Forge dependencies
3. Decompiles Minecraft source
4. Generates mappings

**Subsequent builds will be faster** (1-3 minutes)

## Troubleshooting

### Error: "gradle-wrapper.jar not found"
**Solution:** Run `./gradlew build` - it will download the JAR automatically

### Error: "Java not found" or "JAVA_HOME not set"
**Solution:** 
- Install JDK 17+
- Set JAVA_HOME environment variable:
  - Windows: `setx JAVA_HOME "C:\Program Files\Java\jdk-17"`
  - Linux/macOS: `export JAVA_HOME=/path/to/jdk17`

### Error: "Out of memory" during build
**Solution:** Increase gradle heap:
- Edit `gradle.properties`:
  ```properties
  org.gradle.jvmargs=-Xmx4G -Xms1G
  ```

### Build is very slow
**Solution:** This is normal for first build. Subsequent builds are faster. Consider:
- Closing other applications
- Increasing available RAM
- Using SSD instead of HDD

### "Permissions denied" on gradlew (Linux/macOS)
**Solution:**
```bash
chmod +x gradlew
./gradlew build
```

## Build Output

After successful build, you'll have:
```
build/
├── libs/
│   ├── CompanionMod-1.0.0.jar      ← Your compiled mod
│   └── CompanionMod-1.0.0-sources.jar (optional)
├── classes/
├── resources/
└── ... (other build artifacts)
```

## Installing the Built Mod

1. Locate your Minecraft mods folder:
   - Windows: `%APPDATA%\.minecraft\mods\`
   - Linux: `~/.minecraft/mods/`
   - macOS: `~/Library/Application Support/minecraft/mods/`

2. Copy the JAR file:
   ```
   build/libs/CompanionMod-1.0.0.jar → [minecraft]/mods/
   ```

3. Launch Minecraft with Forge 1.20.1

## Development Workflow

### Run Client (for testing)
```bash
./gradlew runClient    # Unix/Linux/macOS
# OR
gradlew.bat runClient  # Windows PowerShell
```

### Run Server
```bash
./gradlew runServer    # Unix/Linux/macOS
# OR
gradlew.bat runServer  # Windows PowerShell
```

### Clean Build (if you have build issues)
```bash
./gradlew clean build
```

### Generate IDE Files
For IntelliJ IDEA:
```bash
./gradlew idea
```

For Eclipse:
```bash
./gradlew eclipse
```

## Understanding the Build Process

The Forge build system does several things:

1. **genSources** - Decompile and generate Minecraft source code
2. **build** - Compile your mod code against Minecraft
3. **reobfJar** - Reobfuscate the compiled code for runtime
4. **jar** - Package everything into a single JAR file

This is why the build takes time and space - Minecraft's full source is involved.

## Next Steps After Building

1. Install the JAR in your mods folder (see above)
2. Add textures (see TEXTURES.md):
   - Companion entity texture (64x64 PNG)
   - Spawner item texture (16x16 PNG)
   - GUI background (176x222 PNG)
3. Launch Minecraft with Forge
4. Summon companion with: `/companion summon`
5. Test features (mining, following, inventory)

## Need Help?

1. Check troubleshooting section above
2. Review Forge documentation: https://docs.minecraftforge.net/
3. Check build.gradle for Forge version compatibility
4. Verify Java 17+ is installed

## Advanced: Custom Build Properties

Edit `gradle.properties` to customize:

```properties
# Java compilation
org.gradle.jvmargs=-Xmx4G -Xms1G

# Forge version
forge_version=47.3.0

# Mod version
mod_version=1.0.0

# Java language level
java.toolchain.languageVersion=17
```

## Performance Tips

- **First build:** Takes 5-15 minutes (downloads ~2GB)
- **Subsequent builds:** Takes 1-3 minutes
- **Incremental builds:** Much faster if only changing few files
- **Parallel builds:** Add `-Dorg.gradle.parallel=true` to GRADLE_OPTS

## Additional Resources

- Forge Docs: https://docs.minecraftforge.net/
- Gradle Docs: https://docs.gradle.org/
- Minecraft Forge Discord: https://discord.minecraftforge.net/
- Minecraft Modding Wiki: https://minecraftmoddingtutorials.com/

---

**Good luck with your build!** 🚀
