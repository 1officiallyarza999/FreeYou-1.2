with open('app/build.gradle.kts', 'r') as f:
    code = f.read()

code = code.replace("alias(libs.plugins.ksp)", "alias(libs.plugins.ksp)\n    alias(libs.plugins.kotlin.serialization)")

deps = """
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
"""

code = code.replace("    implementation(libs.androidx.room.ktx)\n    ksp(libs.androidx.room.compiler)", deps)

with open('app/build.gradle.kts', 'w') as f:
    f.write(code)
