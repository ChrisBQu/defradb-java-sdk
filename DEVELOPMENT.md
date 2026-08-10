# Development

This guide covers building and testing the SDK itself. For installation and application usage, start with the [README](README.md).

## Prerequisites

All builds require a local [DefraDB](https://github.com/sourcenetwork/defradb) checkout and its build prerequisites. The Gradle wrapper downloads Gradle 8.3 when necessary.

Linux builds additionally require:

- JDK 11 or later, with `JAVA_HOME` set
- GCC

Android builds additionally require:

- JDK 17 or later, with `JAVA_HOME` set
- Android SDK with API level 34
- Android NDK r26d or a compatible release, with `ANDROID_NDK` set

## Automated build

From the SDK repository root:

```shell
./build.sh --defra-dir /path/to/defradb --linux
./build.sh --defra-dir /path/to/defradb --android
./build.sh --defra-dir /path/to/defradb --linux --android
```

The script performs three stages for each target:

1. Runs the matching `make build-c-shared-*` target in the DefraDB checkout.
2. Copies the generated C headers and shared libraries into this project, then compiles `libnativewrapper`.
3. Uses Gradle to package the Java classes and native libraries.

Options:

- `--cleanup` removes copied headers and native libraries after the build.
- `--silent` passes `BUILD_TAGS=silent` to the DefraDB build.

## Manual build

### Linux

Build DefraDB's C bindings:

```shell
cd /path/to/defradb
make build-c-shared-linux
```

Copy `build/libdefradb.so`, `build/libdefradb.h`, and `build/defra_structs.h` into the SDK:

```text
src/main/linuxLibs/libdefradb.so
src/main/c/libdefradb.h
src/main/c/defra_structs.h
```

Then build the JNI library and JAR:

```shell
cd src/main/c
./build.sh --linux
cd ../../..
./gradlew -b build-linux.gradle build
```

The result is `build/libs/defradb.jar`.

### Android

Build DefraDB's Android C bindings:

```shell
cd /path/to/defradb
make build-c-shared-android
```

Copy the generated shared libraries and headers into the SDK:

```text
src/main/jniLibs/arm64-v8a/libdefradb.so
src/main/jniLibs/x86_64/libdefradb.so
src/main/c/libdefradb.h
src/main/c/defra_structs.h
```

Then build the JNI libraries and AAR:

```shell
cd src/main/c
./build.sh --android
cd ../../..
./gradlew -b build-android.gradle assembleRelease
```

The result is `build/outputs/aar/defradb-release.aar`.

## Tests

The Linux smoke test creates and closes an in-memory node:

```shell
mkdir -p test-out
javac -cp build/libs/defradb.jar test/DefraTest.java -d test-out
java -cp build/libs/defradb.jar:test-out DefraTest
```

The Android instrumented tests live in `androidTest`. The GitHub Actions workflow builds the AAR, stages it as `androidTest/libs/defradb.aar`, and runs the tests on an API 29 x86_64 emulator.

## Troubleshooting

### JNI headers are missing

Run the DefraDB C bindings build first and ensure both `libdefradb.h` and `defra_structs.h` were copied into `src/main/c`.

### `jni.h` cannot be found

Set `JAVA_HOME` to the JDK used for the build. The native build reads JNI headers from `$JAVA_HOME/include` and `$JAVA_HOME/include/linux`.

### Android compiler cannot be found

Set `ANDROID_NDK` to the NDK root. The current native build expects the NDK's Linux x86_64 host toolchain.

### A native library fails to load

Confirm that the artifact was built for the runtime platform and architecture. Android supports `arm64-v8a` and `x86_64`; the Linux JAR contains libraries for the machine on which it was built.
