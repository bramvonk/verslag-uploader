package nl.bramvonk.converter;

import lombok.SneakyThrows;
import net.coobird.thumbnailator.Thumbnails;
import nl.bramvonk.verslag.ImageVerslagBlock;
import nl.bramvonk.verslag.Verslag;

import java.io.File;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class PhotoDownloaderAndResizer {
    @SneakyThrows
    public static void process(Verslag verslag) {
        Path tmpDir = Files.createTempDirectory(UUID.randomUUID().toString());
        try (HttpClient client = HttpClient.newHttpClient()) {
            for (ImageVerslagBlock imageVerslagBlock : verslag.getImageBlocks()) {
                downloadImage(tmpDir, imageVerslagBlock, client);
                imageVerslagBlock.getPathOnLocalDisk().toFile().deleteOnExit();
                resizeImage(imageVerslagBlock);
            }
        }
        tmpDir.toFile().deleteOnExit();
    }

    @SneakyThrows
    private static void resizeImage(ImageVerslagBlock imageVerslagBlock) {
        File file = imageVerslagBlock.getPathOnLocalDisk().toFile();
        Thumbnails.of(file).size(600, 600).outputQuality(0.8f).toFile(file);
    }

    @SneakyThrows
    private static void downloadImage(Path tmpDir, ImageVerslagBlock imageVerslagBlock, HttpClient client) {
        HttpRequest request = HttpRequest.newBuilder().uri(imageVerslagBlock.getImageUrl()).GET().build();
        Path tmpFileName = tmpDir.resolve(imageVerslagBlock.getImageFilename());
        HttpResponse<Path> send = client.send(request, HttpResponse.BodyHandlers.ofFile(tmpFileName));
        if (send.statusCode() != 200) {
            throw new RuntimeException("Failed downloading %s: HTTP error code: %s".formatted(imageVerslagBlock.getImageUrl(), send.statusCode()));
        }
        imageVerslagBlock.setPathOnLocalDisk(send.body());
    }

}
