# Slavic Myths2.0 — опубликованные измерения

Начните с [REPORT.md](REPORT.md), [METRICS.md](METRICS.md), [HOTSPOTS.md](HOTSPOTS.md) и [RUNS.md](RUNS.md). Это законченная сводка доступной итерации с непройденными сценариями, не сертификат готовности релиза. Production отчётной ветки идентичен main fb2994e9; guard краша вынесен отдельно.

Исходные JFR/client.log/несжатые frames остаются локально; публикуются SHA256, JFR summaries, heap timelines, gzip frames и обезличенные логи. `profile_event_span_seconds` не равно длительности recording. Измерительные инструменты opt-in, исключены из production JAR.

Изначальные15min интервалы владелец явно отменил17октября; история запусков сохранена. **Последнее указание владельца: Minecraft не запускать.** Эта публикация выполнена offline. City/старыйsave/reopen v2 требуют нового разрешения и не выдаются за пройденные проверки.
