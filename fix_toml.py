with open('gradle/libs.versions.toml', 'r') as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if line.startswith('kotlinx-serialization-json =') or line.startswith('retrofit =') or line.startswith('okhttp =') or line.startswith('[libraries]') and lines.index(line) > 20:
        continue
    new_lines.append(line)

with open('gradle/libs.versions.toml', 'w') as f:
    f.writelines(new_lines)
