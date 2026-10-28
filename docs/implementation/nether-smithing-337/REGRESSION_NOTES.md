# Расширенные регрессии

- Spears:9старыхID×72nativecontexts PASS. Thrown:14старыхID×15flightposes×2contexts PASS.
- FlailMotion: прежнийcode добавлялcontact×1unit кeyelet-offset, отрываягирюотцепи
  во времяудара. Исправленclient-onlyoffset на постоянные2units; прежний тест
 15120poses прошёл безослабленияasserts. Gameplay/damage/cooldown не менялись.
- Отдельный старый weapons87Balance: **FAIL: ApprovedB cooldown**. Он ожидает
 60/54, current StormStaffItem имеет48/42 сcommit97e78e40, доначалаэтойработы.
 `git diff94c9cc3e -- StormStaffItem.java` пуст. Сам посох иего балансНЕизменены,
 тест не переписан под желаемыйPASS. Это известное расхождение старого теста,
 не скрыто и не записано как успешнаярегрессия. Лог:legacy-staff-failure.txt.
- Windows дважды запретила Java TRUNCATE_EXISTING JSON из-за memory-mapped file.
 Экспорт FlailMotion теперь допускает новый output path и пропускает идентичный
 файл; проверки связности/геометрии неизменны. Test registry не размораживался.

Результаты здесь относятся только к offline/native checks, не к игровым боям,
задержке MP, FPS ипрогрессии. Все соответствующие runtime пункты NOT_RUN.
