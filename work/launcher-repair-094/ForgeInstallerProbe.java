import io.github.zekerzhayard.forgewrapper.installer.detector.MultiMCFileDetector;
import java.nio.file.Files;
public class ForgeInstallerProbe {
    public static void main(String[] args) throws Exception {
        // Calls the real wrapper detector only. Never calls the wrapper launcher/main.
        var detector = new MultiMCFileDetector();
        var installer = detector.getInstallerJar("net.neoforged", "neoforge", "21.1.255");
        var minecraft = detector.getMinecraftJar("1.21.1");
        if (installer == null || !Files.isRegularFile(installer)) throw new IllegalStateException("Installer missing");
        if (minecraft == null || !Files.isRegularFile(minecraft)) throw new IllegalStateException("Minecraft JAR missing");
        System.out.println("PASS: real ForgeWrapper detector finds NeoForge installer and Minecraft JAR. No game launched.");
        System.out.println(installer);
        System.out.println(minecraft);
    }
}
