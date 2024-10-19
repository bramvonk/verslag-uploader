package nl.bramvonk;

import lombok.SneakyThrows;
import nl.bramvonk.converter.GoogleDocToVerslagConverter;
import nl.bramvonk.converter.PhotoDownloaderAndResizer;
import nl.bramvonk.googledocs.*;
import nl.bramvonk.verslag.Verslag;
import nl.bramvonk.website.VerslagWriter;

import java.net.URI;
import java.util.List;

public class VerslagUploaderApplication {

    private static void processDoc(URI googleDocsDocUri, boolean save) {
        log("Processing %s", googleDocsDocUri.toString());
        log("Downloading document");
        GoogleDoc googleDoc = new GoogleDocumentReader().readFrom(googleDocsDocUri);
        log("Converting %s to verslag", googleDoc.title);
        Verslag verslag = GoogleDocToVerslagConverter.convert(googleDoc);
        log("Downloading and resizing photos");
        PhotoDownloaderAndResizer.process(verslag);
        log("Adding verslag to website");
        VerslagWriter.write(verslag, save);
    }

    private static void log(String message, String... parameters) {
        System.out.printf((message) + "%n", (Object[]) parameters);
    }

    @SneakyThrows
    public static void main(String[] args) {
        processDoc(URI.create(args[0]), true);
    }

    private static GoogleDoc mockDoc() {
        GoogleDoc googleDoc = new GoogleDoc("Amsterdam Open");
        googleDoc.elements.addAll(List.of(
                createTextElement("Hoofdmenu: kanopolo\n"),
                createTextElement("Fotosubdirectory: amsterdamopen_2024\n"),
                createTextElement("Metabeschrijving: hier metabeschrijving\n"),
                createTextElement("\n"),
                createTextElement("De tekst\n"),
                createTextElement("Meer tekst\n"),
                new GoogleDocImageElement(URI.create("http://192.168.0.20/testphoto.jpg")),
                new GoogleDocTextElement("Onderschrift foto\n", false, true)
        ));
        return googleDoc;
    }

    private static GoogleDocElement createTextElement(String s) {
        return new GoogleDocTextElement(s, false, false);
    }

}
