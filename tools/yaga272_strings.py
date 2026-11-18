"""Scoped #272 language overlay; never invokes historical asset/audio generators."""
import json
from pathlib import Path

TEXT = {
    'status.AVAILABLE': ('Поиск Яги доступен.', 'The search for Yaga is available.'),
    'status.SEARCHING': ('Поиск места и создание избушки выполняются.', 'Finding a site and building the hut.'),
    'status.FOUND': ('Место Яги найдено. Указатель направления доступен в книге.', 'Yaga’s home has been found. A direction marker is available in the book.'),
    'status.UNAVAILABLE': ('Место пока недоступно.', 'The site is currently unavailable.'),
    'reason.surface': ('Нет подходящей поверхности поблизости. Попробуйте поиск из другого леса.', 'No suitable surface nearby. Try searching from another forest.'),
    'reason.water': ('Место занято водой.', 'The site is covered by water.'),
    'reason.slope': ('Слишком неровная поверхность.', 'The terrain is too uneven.'),
    'reason.height': ('Недостаточно места по высоте.', 'Not enough vertical space.'),
    'reason.chunks': ('Не удалось подготовить необходимые чанки.', 'Required chunks could not be prepared.'),
    'reason.container': ('На месте есть хранилище или блок с данными.', 'A container or block entity occupies the site.'),
    'reason.protected': ('Место защищено от перестройки.', 'The site is protected from rebuilding.'),
    'reason.obstruction': ('Постройке мешают блоки.', 'Blocks obstruct construction.'),
    'reason.occupied': ('Место уже зарезервировано другой постройкой.', 'Another structure has reserved the site.'),
    'reason.border': ('Место находится за границей мира.', 'The site is outside the world border.'),
    'reason.timeout': ('Время поиска истекло. Можно повторить запрос.', 'The search timed out. You can retry.'),
    'reason.interrupted': ('Поиск был прерван. Можно повторить запрос.', 'The search was interrupted. You can retry.'),
    'reason.dimension': ('Начните поиск в обычном мире.', 'Start the search in the Overworld.'),
    'reason.npc': ('Не удалось подтвердить присутствие Яги. Повторная избушка не создаётся.', 'Yaga’s presence could not be confirmed. No duplicate hut is created.'),
    'reason.rollback': ('При откате постройки произошла ошибка. Нужна проверка места.', 'Construction rollback failed. The site needs inspection.'),
    'reason.cancelled': ('Создание отменено. Можно повторить поиск.', 'Construction was cancelled. You can retry.'),
    'reason.internal': ('Создание остановлено из-за ошибки; подробности в журнале.', 'Construction stopped after an error; see the log.'),
    'reason.exists': ('Уникальная избушка уже существует.', 'The unique hut already exists.'),
}

def apply(root):
    for lang, index in [('ru_ru', 0), ('en_us', 1)]:
        path = Path(root)/f'src/main/resources/assets/slavicmyths/lang/{lang}.json'
        data = json.loads(path.read_text(encoding='utf-8'))
        data.update({'yaga.search.'+key: pair[index] for key, pair in TEXT.items()})
        path.write_text(json.dumps(data, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')

if __name__ == '__main__':
    apply(Path(__file__).resolve().parents[1])
