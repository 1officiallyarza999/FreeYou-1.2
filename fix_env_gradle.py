with open('app/build.gradle.kts', 'r') as f:
    code = f.read()

parser_old = """    val envFile = rootProject.file(".env")
    val geminiApiKey = if (envFile.exists()) {
        val props = java.util.Properties()
        envFile.inputStream().use { props.load(it) }
        props.getProperty("GEMINI_API_KEY", "")
    } else ""
"""

parser_new = """    val envFile = rootProject.file(".env")
    var geminiApiKey = ""
    if (envFile.exists()) {
        envFile.readLines().forEach {
            if (it.startsWith("GEMINI_API_KEY=")) {
                geminiApiKey = it.substringAfter("=").trim()
            }
        }
    }
"""

code = code.replace(parser_old, parser_new)

with open('app/build.gradle.kts', 'w') as f:
    f.write(code)
