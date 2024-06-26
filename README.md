# TgcTranslations
TgcTranslations is a lightweight library designed to simplify translation management for [Minecraft Plugins](https://minecraft.wiki/w/Mods#Server-based),
specifically for usage in plugins for [PaperMC](https://papermc.io/)'s [Paper](https://papermc.io/software/paper) and [Velocity](https://papermc.io/software/velocity) software.

## Getting started
You can find the latest version [here](https://repo.skyblocksquad.de/#/repo/de/timongcraft/TgcTranslations).

### Maven

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.6.0</version>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <relocations>
                    <relocation>
                        <pattern>de.timongcraft.tgctranslations</pattern>
                        <!-- Replace 'com.yourpackage' with the package of your project ! -->
                        <shadedPattern>com.yourpackage.tgctranslations</shadedPattern>
                    </relocation>
                </relocations>
            </configuration>
        </plugin>
    </plugins>
</build>

<repositories>
    <repository>
        <id>skyblocksquad-repo</id>
        <url>https://repo.skyblocksquad.de/repo</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>de.timongcraft</groupId>
        <artifactId>TgcTranslations</artifactId>
        <version>LATEST_VERSION</version>
    </dependency>
</dependencies>
```

When using Maven, make sure to build directly with Maven and not with your IDE configuration (on IntelliJ IDEA: in the `Maven` tab on the right, in `Lifecycle`, use `package`).

### Gradle

```groovy
plugins {
    id 'io.github.goooler.shadow' version '8.1.7'
}

repositories {
    maven {
        url "https://repo.skyblocksquad.de/repo"
    }
}

dependencies {
    implementation 'de.timongcraft:TgcTranslations:LATEST_VERSION'
}

shadowJar {
    // Replace 'com.yourpackage' with the package of your project 
    relocate 'de.timongcraft.tgctranslations', 'com.yourpackage.tgctranslations'
}
```