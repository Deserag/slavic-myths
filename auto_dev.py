import os
import subprocess

# Список сломанных файлов, которые мы чиним по очереди
TARGET_FILES = [
    "src/main/java/org/slavicmyths/posad/PosadResidentEntity.java",
    "src/main/java/org/slavicmyths/posad/PosadNaturalPopulation.java"
]

def get_gradle_errors():
    print("🤖 Проверяю компиляцию через Gradle...")
    result = subprocess.run([".\\gradlew.bat", "compileJava", "--no-daemon"], capture_output=True, text=True, shell=True)
    lines = result.stdout.split('\n')
    errors = [line for line in lines if "error:" in line or "cannot find symbol" in line]
    return "\n".join(errors[:15])

def prepare_context_for_gpt(file_path):
    if not os.path.exists(file_path):
        return f"Файл {file_path} не найден."
    with open(file_path, "r", encoding="utf-8") as f:
        code = f.read()
    
    errors = get_gradle_errors()
    
    # Формируем ультра-сжатый контекст без мусора для экономии лимитов GPT
    prompt = f"""=== КРИТИЧЕСКАЯ ЗАДАЧА ДЛЯ ИСПОЛНИТЕЛЯ ===
Проект: Slavic Myths (Minecraft 1.21.1, NeoForge 21.1.255)
Текущие ошибки компиляции:
{errors}

Файл для исправления: {file_path}
Текущий код файла:
{code}

Исправь ОШИБКИ компиляции в этом файле. Выведи ТОЛЬКО ИСПРАВЛЕННЫЙ КОД класса от начала до конца. Без комментариев и лишнего текста.
"""
    return prompt

# Генерируем сжатый контекст для первого сломанного файла
first_file = TARGET_FILES[0]
print(f"🤖 Собираю контекст для файла: {first_file}")
gpt_payload = prepare_context_for_gpt(first_file)

with open("gpt_task.txt", "w", encoding="utf-8") as f:
    f.write(gpt_payload)

print("✅ Файл 'gpt_task.txt' успешно создан в корне проекта!")
print("👉 Скопируйте его содержимое и отправьте в НОВЫЙ чат ChatGPT.")
