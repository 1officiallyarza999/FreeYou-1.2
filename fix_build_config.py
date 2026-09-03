with open('app/build.gradle.kts', 'r') as f:
    code = f.read()

# Remove the secrets block and plugin
code = code.replace('alias(libs.plugins.secrets)', '')
code = code.replace('''secrets {
    propertiesFileName = ".env"
    defaultPropertiesFileName = ".env.example"
}''', '')

# Add the manual .env parser in android block
env_parser = """
    val envFile = rootProject.file(".env")
    val geminiApiKey = if (envFile.exists()) {
        val props = java.util.Properties()
        envFile.inputStream().use { props.load(it) }
        props.getProperty("GEMINI_API_KEY", "")
    } else ""
    
    defaultConfig {
        buildConfigField("String", "GEMINI_API_KEY", "\\"${geminiApiKey}\\"")
"""
code = code.replace('defaultConfig {', env_parser)

with open('app/build.gradle.kts', 'w') as f:
    f.write(code)
