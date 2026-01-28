package QuarryDemo.util;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Backup {

    public static void teeVarukoopia(int varukoopiaid) {
        Path basePath = Paths.get(System.getenv("APPDATA"), "QuarryDemo");
        Path dbFile = basePath.resolve("andmebaas.db");
        Path backupDir = basePath.resolve("backups");

        try {
            Files.createDirectories(backupDir);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            Path backupFile = backupDir.resolve("andmebaas_" + timestamp + ".db");

            Files.copy(dbFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Varukoopia loodud: " + backupFile);

            try (Stream<Path> files = Files.list(backupDir)
                    .filter(p -> p.getFileName().toString().startsWith("andmebaas_"))
                    .sorted(Comparator.comparingLong((Path p) -> p.toFile().lastModified()).reversed())
            ) {
                var failid = files.collect(Collectors.toList());
                for (int i = varukoopiaid; i < failid.size(); i++) {
                    Files.deleteIfExists(failid.get(i));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
