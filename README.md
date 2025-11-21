ActiveMQ Example — Java 21

This project is configured to build with Java 21 (LTS).

Quick setup

- Install a JDK 21 distribution (Temurin, Zulu, Azul, etc.). On macOS you can use Homebrew:

```bash
brew update
brew install --cask temurin21
```

- Or use SDKMAN to install/switch:

```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 21.0.4-tem
sdk use java 21.0.4-tem
```

Set `JAVA_HOME` (zsh)

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v21)
export PATH="$JAVA_HOME/bin:$PATH"
```

Helper script

- Use `scripts/setup-java.sh` to set `JAVA_HOME` for your shell and run `mvn clean verify`.

CI

The repository includes a GitHub Actions workflow that runs the build on Java 21.

Why this exists

- The `pom.xml` enforces Java 21 with the `maven-enforcer-plugin` and sets the compiler `<release>21`.

If you want me to also update CI to use a different distribution (Zulu/Azul, Temurin), tell me which one and I can change it.
