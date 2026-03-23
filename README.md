# String Resource Finder

An Android Studio / IntelliJ plugin that fixes **Go to Declaration** (Cmd+Click) for string resource keys that use **dots as separators** in `strings.xml` files.

## The Problem

In `strings.xml`, keys often use dots as namespace separators:

```xml
<string name="payment_method.add.whitescreen.body">...</string>
```

But in Kotlin/Java code and XML layouts, Android generates references with underscores:

```kotlin
R.string.payment_method_add_whitescreen_body
```

**Cmd+Click on a dotted key in `strings.xml` finds nothing** — Android Studio doesn't know about this dot-to-underscore mapping.

## What This Plugin Does

When you **Cmd+Click** (or Ctrl+Click) on a string `name` attribute value in `strings.xml`:

1. Converts dots to underscores
2. Searches the project for `R.string.<name>` and `@string/<name>` references
3. **Single usage** → navigates directly to it
4. **Multiple usages** → opens the **Find in Files** search overlay with the reference pre-filled
5. **No usages found** → falls back to default Android Studio behavior (locale file navigation)

## Screenshots

<!-- Add screenshots here -->
<!-- ![Single navigation](screenshots/single-nav.png) -->
<!-- ![Find in Files overlay](screenshots/find-in-files.png) -->

## Installation

### From JetBrains Marketplace

1. Open Android Studio → **Settings** → **Plugins**
2. Search for **"String Resource Finder"**
3. Click **Install** and restart

### From Disk

1. Download the latest `.zip` from [Releases](https://github.com/alicankorkmaz-sudo/string-res-finder/releases)
2. Android Studio → **Settings** → **Plugins** → **⚙️** → **Install Plugin from Disk...**
3. Select the `.zip` file and restart

## Building from Source

```bash
./gradlew buildPlugin
```

The plugin zip will be at `build/distributions/string-res-finder-<version>.zip`.

## Publishing

### Signing

Set the following environment variables:

```bash
export CERTIFICATE_CHAIN="<your-certificate-chain>"
export PRIVATE_KEY="<your-private-key>"
export PRIVATE_KEY_PASSWORD="<your-password>"
```

Then build the signed plugin:

```bash
./gradlew signPlugin
```

### Publishing to Marketplace

```bash
export PUBLISH_TOKEN="<your-jetbrains-marketplace-token>"
./gradlew publishPlugin
```

## Compatibility

- **Android Studio Hedgehog (2023.1)** and later
- **IntelliJ IDEA 2023.1** and later (with Android plugin)
- Build range: `231.*` – `253.*`

## License

MIT
