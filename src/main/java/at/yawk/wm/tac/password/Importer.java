package at.yawk.wm.tac.password;

import at.yawk.password.LocalStorageProvider;
import at.yawk.password.MultiFileLocalStorageProvider;
import at.yawk.password.client.PasswordClient;
import at.yawk.password.model.PasswordBlob;
import at.yawk.password.model.PasswordEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Map;
import lombok.Data;

/**
 * @author yawkat
 */
class Importer {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("<java> <input file> <local dir> <host> <port>");
            System.exit(-1);
            return;
        }

        Blob in = new ObjectMapper().readValue(new FileInputStream(args[0]), Blob.class);
        PasswordBlob out = new PasswordBlob();
        in.getPasswords().forEach((k, v) -> {
            PasswordEntry entry = new PasswordEntry();
            entry.setName(k);
            entry.setValue(v.getPassword());
            out.getPasswords().add(entry);
        });

        char[] password = System.console().readPassword("Password: ");

        PasswordClient client = PasswordClient.create();
        client.setLocalStorageProvider(new MultiFileLocalStorageProvider(Paths.get(args[1])));
        client.setRemote(args[2], Integer.parseInt(args[3]));
        client.setPassword(new String(password).getBytes(StandardCharsets.UTF_8));
        client.save(out);
    }

    @Data
    private static final class Blob {
        int version;
        Map<String, Entry> passwords;
    }

    @Data
    private static final class Entry {
        String password;
    }
}
