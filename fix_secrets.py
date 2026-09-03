with open('app/build.gradle.kts', 'r') as f:
    code = f.read()

code = code.replace('''android {
    secrets {
        propertiesFileName = ".env"
        defaultPropertiesFileName = ".env.example"
    }''', '''secrets {
    propertiesFileName = ".env"
    defaultPropertiesFileName = ".env.example"
}
android {''')

with open('app/build.gradle.kts', 'w') as f:
    f.write(code)
