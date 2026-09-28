# Development

This guide covers building and testing the SDK itself. For installation and application usage, start with the [README](README.md).

## Prerequisites

All builds require a local [DefraDB](https://github.com/sourcenetwork/defradb) checkout and its build prerequisites. The SDK and CI target the latest DefraDB default branch. Keep your local checkout up to date; upstream C API changes should be reflected in the Java/JNI bindings rather than worked around by pinning an older revision. The Gradle wrapper downloads Gradle 8.3 when necessary.

Desktop builds require a full JDK (11–20, preferably 17) with `JAVA_HOME` set and a C compiler: Xcode Command Line Tools on macOS, or GCC on Linux. Gradle 8.3 cannot run on JDK 21 or later.

Android builds require JDK 17, Android SDK API 34, and an Android NDK with `ANDROID_NDK` set. The build selects the Darwin or Linux NDK host toolchain automatically. On macOS, the upstream Android build additionally requires GNU `sed` available as `sed` on `PATH`; the native macOS target supports the system BSD `sed`.

## Automated build

```shell
# Linux
./build.sh --defra-dir /path/to/defradb --linux

# macOS
./build.sh --defra-dir /path/to/defradb --macos

# Android; can also be combined with the host desktop flag
./build.sh --defra-dir /path/to/defradb --android
```

The script uses DefraDB’s `build-c-shared-linux`, `build-c-shared-macos`, or `build-c-shared-android` Makefile target. These targets write native libraries and headers to the DefraDB checkout’s `build` directory before copying them into the SDK. Go must satisfy the version in that checkout's `go.mod`; normal Go toolchain auto-download behavior applies.

It then compiles the JNI wrapper and packages the Java classes and native libraries. Desktop output is `build/libs/defradb.jar`, for the host architecture only. Android output is `build/outputs/aar/defradb-release.aar`, containing `arm64-v8a` and `x86_64` libraries.

Options:

- `--cleanup` removes generated headers and native libraries after successful packaging.
- `--silent` passes the `silent` build tag to Go.
- `--help` displays usage.

The script can be invoked from any directory. Relative `--defra-dir` paths resolve from the caller's working directory.

## Native compilation and packaging

After the Go build, native libraries are staged in `src/main/macosLibs`, `src/main/linuxLibs`, or `src/main/jniLibs/<abi>`. Generated headers are staged in `src/main/c`.

To rebuild just the desktop JNI wrapper and JAR using existing generated files:

```shell
# macOS; use --linux on Linux
./src/main/c/build.sh --macos
./gradlew -b build-linux.gradle build
```

`build-linux.gradle` is shared by both desktop platforms; its existing name is retained for compatibility. macOS uses `.dylib` libraries with `@loader_path`; Linux uses `.so` libraries with `$ORIGIN`. The Java loader selects the platform's library extension automatically.

## Tests

CI builds and runs the desktop smoke test on macOS and Linux.

The desktop smoke test creates and closes an in-memory node:

```shell
mkdir -p test-out
javac -cp build/libs/defradb.jar test/DefraTest.java -d test-out
java -cp build/libs/defradb.jar:test-out DefraTest
```

The Android instrumented tests live in `androidTest`. The GitHub Actions workflow builds the AAR, stages it as `androidTest/libs/defradb.aar`, and runs the tests on an API 29 x86_64 emulator.

## Troubleshooting

### JNI headers are missing

Run the top-level build first and ensure both `libdefradb.h` and `defra_structs.h` were copied into `src/main/c`.

### `jni.h` cannot be found

Set `JAVA_HOME` to the JDK used for the build. Use a full JDK containing `include/jni.h`. The native build also uses `include/darwin` on macOS or `include/linux` on Linux. Some bundled application runtimes omit these headers.

### Android compiler cannot be found

Set `ANDROID_NDK` to the NDK root. The native build selects `darwin-x86_64` on macOS and `linux-x86_64` on Linux.

### A native library fails to load

Confirm that the artifact was built for the runtime platform and architecture. Android supports `arm64-v8a` and `x86_64`; the desktop JAR contains libraries for the machine on which it was built.
