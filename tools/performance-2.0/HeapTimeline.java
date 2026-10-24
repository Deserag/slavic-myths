import java.nio.file.*;
import jdk.jfr.consumer.*;

/** Offline GC heap observations; neither retained-size accounting nor a forced full GC. */
class HeapTimeline {
    public static void main(String[] args) throws Exception {
        for (String arg : args) {
            var file = Path.of(arg);
            try (var input = new RecordingFile(file);
                 var output = Files.newBufferedWriter(file.resolveSibling(file.getFileName()+".heap.csv"))) {
                output.write("epoch_ms,gc_id,when,heap_used\n");
                while (input.hasMoreEvents()) {
                    var event = input.readEvent();
                    if (event.getEventType().getName().equals("jdk.GCHeapSummary")) {
                        output.write(event.getStartTime().toEpochMilli()+","+event.getInt("gcId")+","+
                                event.getString("when")+","+event.getLong("heapUsed")+"\n");
                    }
                }
            }
        }
    }
}
