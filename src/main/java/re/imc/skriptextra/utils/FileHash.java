package re.imc.skriptextra.utils;

import com.google.common.hash.Hashing;
import java.io.File;
import java.nio.file.Files;

public class FileHash {
    public static String sha1(File file) throws Exception {
        byte[] bytes = Files.readAllBytes(file.toPath());
        return Hashing.sha1().hashBytes(bytes).toString();
    }
}
