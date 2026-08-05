# DefraDB Java SDK

[![Build and Test](https://github.com/sourcenetwork/defradb-java-sdk/actions/workflows/build.yml/badge.svg)](https://github.com/sourcenetwork/defradb-java-sdk/actions/workflows/build.yml)
[![Discord](https://img.shields.io/discord/427944769851752448.svg?color=768AD4&label=discord)](https://source.network/discord)

Embed [DefraDB](https://github.com/sourcenetwork/defradb) in Java applications on Linux and Android. The SDK exposes DefraDB's collections, documents, queries, transactions, identities, access control, peer-to-peer networking, and other node APIs through a Java interface backed by JNI.

Read the DefraDB documentation at [docs.source.network](https://docs.source.network/defradb).

> [!WARNING]
> This SDK is experimental and under active development. It aims to track DefraDB's client APIs, but is not yet recommended for production use. Thoroughly test your integration and keep the SDK and DefraDB source revisions aligned.

## Contents

- [Build](#build)
- [Use on Linux](#use-on-linux)
- [Use on Android](#use-on-android)
- [Start a node](#start-a-node)
- [Documentation](#documentation)
- [Community](#community)

## Build

The SDK currently builds from source together with a local checkout of DefraDB. Published Maven or Gradle packages are not yet available.

### Requirements

- Linux
- Git
- JDK 11 or later for Linux builds
- JDK 17 or later and the Android NDK for Android builds
- The toolchains required to build [DefraDB from source](https://github.com/sourcenetwork/defradb#build-requirements)

Clone both repositories, then choose one or both targets:

```shell
git clone https://github.com/sourcenetwork/defradb.git
git clone https://github.com/sourcenetwork/defradb-java-sdk.git
cd defradb-java-sdk

# Linux
./build.sh --defra-dir ../defradb --linux

# Android; ANDROID_NDK must point to your installed NDK
ANDROID_NDK=/path/to/android-ndk ./build.sh --defra-dir ../defradb --android
```

Add `--cleanup` to remove copied headers and native libraries after packaging. Add `--silent` to build DefraDB with its `silent` build tag.

Build outputs:

| Target | Artifact | Supported architecture |
| --- | --- | --- |
| Linux | `build/libs/defradb.jar` | Host architecture |
| Android | `build/outputs/aar/defradb-release.aar` | `arm64-v8a`, `x86_64` |

See [DEVELOPMENT.md](DEVELOPMENT.md) for the manual build process, tests, and troubleshooting.

## Use on Linux

Add the generated JAR to the compile-time and runtime classpaths:

```shell
javac -cp defradb.jar Example.java
java -cp .:defradb.jar Example
```

The JAR bundles its Java dependency and the Linux native libraries. It must be built for the same operating system and architecture on which it runs.

## Use on Android

Copy `defradb-release.aar` into your app module, for example at `app/libs/defradb.aar`, and add it as a dependency:

```groovy
dependencies {
    implementation files('libs/defradb.aar')
}
```

Restrict the app to the ABIs included in the AAR:

```groovy
android {
    defaultConfig {
        minSdk 24

        ndk {
            abiFilters 'arm64-v8a', 'x86_64'
        }
    }
}
```

## Start a node

The following creates an in-memory node with the HTTP API and peer-to-peer networking disabled by default:

```java
import source.defra.DefraException;
import source.defra.DefraNode;
import source.defra.DefraNodeInitOptions;

public final class Example {
    public static void main(String[] args) {
        DefraNodeInitOptions options = new DefraNodeInitOptions();
        options.inMemory = true;

        try {
            DefraNode node = new DefraNode(options);
            System.out.println(node.getVersion(false, false));
            node.close();
        } catch (DefraException error) {
            System.err.println(error.getMessage());
        }
    }
}
```

`DefraNode` is the main SDK entry point. Its methods return JSON strings for structured DefraDB responses and throw `DefraException` when a native operation fails. Always call `close()` when the node is no longer needed.

## Documentation

- [Architecture](ARCHITECTURE.md) — how Java, JNI, and DefraDB's C bindings fit together
- [Development](DEVELOPMENT.md) — build internals, manual builds, tests, and cleanup
- [DefraDB documentation](https://docs.source.network/defradb) — database concepts and DQL
- [Java API sources](src/main/java/source/defra) — currently the authoritative SDK API reference

## Community

Ask questions and share feedback in [Discord](https://source.network/discord) or [GitHub Discussions](https://github.com/sourcenetwork/defradb/discussions). Please report SDK-specific bugs in this repository's [issue tracker](https://github.com/sourcenetwork/defradb-java-sdk/issues).

DefraDB and this SDK are part of the [Source](https://source.network) ecosystem.
