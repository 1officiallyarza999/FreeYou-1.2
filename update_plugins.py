with open('gradle/libs.versions.toml', 'r') as f:
    code = f.read()

code = code.replace('room = "2.6.1"', 'room = "2.6.1"\nsecretsGradlePlugin = "2.0.1"')
code = code.replace('[plugins]', '[plugins]\nsecrets = { id = "com.google.android.libraries.mapsplatform.secrets-gradle-plugin", version.ref = "secretsGradlePlugin" }')

with open('gradle/libs.versions.toml', 'w') as f:
    f.write(code)

with open('build.gradle.kts', 'r') as f:
    root_build = f.read()
root_build = root_build.replace('}', '    alias(libs.plugins.secrets) apply false\n}')
with open('build.gradle.kts', 'w') as f:
    f.write(root_build)

with open('app/build.gradle.kts', 'r') as f:
    app_build = f.read()
app_build = app_build.replace('plugins {', 'plugins {\n    alias(libs.plugins.secrets)')
app_build = app_build.replace('    buildFeatures {', '    buildFeatures {\n        buildConfig = true')
app_build = app_build.replace('android {', 'android {\n    secrets {\n        propertiesFileName = ".env"\n        defaultPropertiesFileName = ".env.example"\n    }')

with open('app/build.gradle.kts', 'w') as f:
    f.write(app_build)

