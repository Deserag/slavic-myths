package org.slavicmyths.losthalls;
import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.*;
import com.google.gson.*;
/** Durable recipient pin outside the rollback-prone encounter SavedData.
 * A pin is never reassigned. Player inventory and its delivery receipt share one player file.
 * Process-crash recovery is supported; restoring this authority itself requires a consistent
 * world+player backup. Corrupt/future authority fails closed and is never overwritten. */
public final class BossRewardAuthority {
 public record Entry(String key,String kind,UUID recipient,int cores,long readyAt){}
 private final Path file;private final Map<String,Entry> entries=new LinkedHashMap<>();
 private final Map<UUID,List<Entry>> recipients=new HashMap<>();private boolean readOnly;
 public BossRewardAuthority(Path file){this.file=file.toAbsolutePath().normalize();if(!Files.exists(this.file))return;try{
   JsonObject root=JsonParser.parseString(Files.readString(this.file)).getAsJsonObject();if(root.get("version").getAsInt()!=1)throw new IllegalArgumentException("Unsupported reward authority");
   for(var raw:root.getAsJsonArray("entries")){var j=raw.getAsJsonObject();Entry e=new Entry(j.get("key").getAsString(),j.get("kind").getAsString(),UUID.fromString(j.get("recipient").getAsString()),j.get("cores").getAsInt(),j.get("readyAt").getAsLong());validate(e);if(entries.putIfAbsent(e.key(),e)!=null)throw new IllegalArgumentException("Duplicate reward pin");}
   entries.values().forEach(this::index);
  }catch(IOException|RuntimeException invalid){readOnly=true;entries.clear();recipients.clear();}
 }
 public boolean isReadOnly(){return readOnly;}
 private static void validate(Entry e){
  boolean extension=Set.of("morova_21x","trizna_21x").contains(e.kind());
  boolean parameter=e.kind().equals("magma")?e.cores()>=4&&e.cores()<=6:extension?e.cores()>=1&&e.cores()<=3:e.cores()==0;
  if(e.key()==null||e.key().isBlank()||e.key().length()>512||e.recipient()==null||!Set.of("magma","posten","guardian_head","zharenitsa_head","morova_21x","trizna_21x").contains(e.kind())||e.readyAt()<0||!parameter)throw new IllegalArgumentException("Invalid boss reward pin");
 }
 private void index(Entry e){recipients.computeIfAbsent(e.recipient(),ignored->new ArrayList<>()).add(e);}
 public List<Entry> forRecipient(UUID recipient){return List.copyOf(recipients.getOrDefault(recipient,List.of()));}
 public synchronized Entry pin(Entry proposed)throws IOException{
  validate(proposed);if(readOnly)throw new IOException("Reward authority is read-only: "+file);
  Entry existing=entries.get(proposed.key());if(existing!=null){if(!existing.kind().equals(proposed.kind()))throw new IOException("Conflicting reward kind");return existing;}
  JsonObject root=new JsonObject();root.addProperty("version",1);JsonArray list=new JsonArray();for(Entry e:entries.values())list.add(json(e));list.add(json(proposed));root.add("entries",list);
  Files.createDirectories(file.getParent());Path temp=Files.createTempFile(file.getParent(),"boss-rewards-",".tmp");
  try{byte[] bytes=new Gson().toJson(root).getBytes(java.nio.charset.StandardCharsets.UTF_8);try(FileChannel out=FileChannel.open(temp,StandardOpenOption.WRITE,StandardOpenOption.TRUNCATE_EXISTING)){ByteBuffer buffer=ByteBuffer.wrap(bytes);while(buffer.hasRemaining())out.write(buffer);out.force(true);}Files.move(temp,file,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}finally{Files.deleteIfExists(temp);}
  entries.put(proposed.key(),proposed);index(proposed);return proposed;
 }
 private static JsonObject json(Entry e){JsonObject j=new JsonObject();j.addProperty("key",e.key());j.addProperty("kind",e.kind());j.addProperty("recipient",e.recipient().toString());j.addProperty("cores",e.cores());j.addProperty("readyAt",e.readyAt());return j;}
}
