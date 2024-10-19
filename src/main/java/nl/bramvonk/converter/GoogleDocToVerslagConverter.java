package nl.bramvonk.converter;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import nl.bramvonk.googledocs.GoogleDoc;
import nl.bramvonk.googledocs.GoogleDocElement;
import nl.bramvonk.googledocs.GoogleDocImageElement;
import nl.bramvonk.googledocs.GoogleDocTextElement;
import nl.bramvonk.verslag.ImageVerslagBlock;
import nl.bramvonk.verslag.MainMenu;
import nl.bramvonk.verslag.TextVerslagBlock;
import nl.bramvonk.verslag.Verslag;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class GoogleDocToVerslagConverter {
    private final Verslag verslag = new Verslag();
    private final GoogleDoc googleDoc;
    private int nrOfImages = 0;

    public static Verslag convert(GoogleDoc googleDoc) {
        GoogleDocToVerslagConverter converter = new GoogleDocToVerslagConverter(googleDoc);
        converter.convert();
        return converter.verslag;
    }

    private void convert() {
        verslag.setTitle(googleDoc.title);

        List<GoogleDocElement> bodyElements = readMetaDataAndReturnRestOfTheDocument(googleDoc);

        while (!bodyElements.isEmpty()) {
            GoogleDocElement first = bodyElements.getFirst();
            if (first instanceof GoogleDocImageElement imageElement) {
                bodyElements.removeFirst();
                processImage(imageElement, bodyElements);
            } else if (first instanceof GoogleDocTextElement textElement) {
                bodyElements.removeFirst();
                processText(textElement, bodyElements);
            } else {
                throw new IllegalStateException("Unknown type " + first.getClass().getSimpleName());
            }
        }
    }

    private void processText(GoogleDocTextElement textElement, List<GoogleDocElement> bodyElements) {
        List<GoogleDocTextElement> blockElements = new ArrayList<>();
        blockElements.add(textElement);
        String text = textElement.text;
        while (!text.endsWith("\n") && !bodyElements.isEmpty() && bodyElements.getFirst() instanceof GoogleDocTextElement nextTextElement) {
            blockElements.add(nextTextElement);
            bodyElements.removeFirst();
        }
        verslag.getBlocks().add(new TextVerslagBlock(blockElements));
    }

    private void processImage(GoogleDocImageElement imageElement, List<GoogleDocElement> restOfTheBody) {
        String caption = null;
        // after an image often is directly a new line
        ifFirstElementIsANewlineRemoveIt(restOfTheBody);
        if (!restOfTheBody.isEmpty() && restOfTheBody.getFirst() instanceof GoogleDocTextElement textElement && !textElement.text.equals("\n") && textElement.isItalic) {
            // caption
            restOfTheBody.removeFirst();
            String text = textElement.text;
            if (!text.endsWith("\n")) {
                throw new IllegalStateException("After an image, an italic text is an image caption. Add a newline after it. And please don't add any other text styling to that. Text found: '%s'".formatted(text));
            }

            caption = text.substring(0, text.length() - 1);
        }
        String fileNameBase;
        if (verslag.getPhotoFileBaseName() != null) {
            fileNameBase = verslag.getPhotoFileBaseName();
        } else {
            fileNameBase = verslag.getTitle();
        }
        ImageVerslagBlock imageVerslagBlock = new ImageVerslagBlock(toImageFileName(String.format("%03d", ++nrOfImages) + "-" + fileNameBase), caption, imageElement.url);
        verslag.getBlocks().add(imageVerslagBlock);
    }

    private void ifFirstElementIsANewlineRemoveIt(List<GoogleDocElement> restOfTheBody) {
        if (firstElementIsANewline(restOfTheBody)) {
            restOfTheBody.removeFirst();
        }
    }

    private boolean firstElementIsANewline(List<GoogleDocElement> restOfTheBody) {
        return !restOfTheBody.isEmpty() && restOfTheBody.getFirst() instanceof GoogleDocTextElement textElement && textElement.text.equals("\n");
    }

    private List<GoogleDocElement> readMetaDataAndReturnRestOfTheDocument(GoogleDoc googleDoc) {
        for (int i = 0; i < googleDoc.elements.size(); i++) {
            GoogleDocElement element = googleDoc.elements.get(i);

            if (element instanceof GoogleDocTextElement textElement) {
                String text = textElement.text;
                if (text.equals("\n")) {
                    if (verslag.getMainMenu() == null || verslag.getPhotoSubDirectory() == null) {
                        throw new IllegalStateException("First write meta data: hoofdmenu, datum/jaar or locatie is missing");
                    }
                    return googleDoc.elements.stream().skip(i + 1).collect(Collectors.toCollection(ArrayList::new));
                }
                String[] metaData = text.replaceFirst(": ", ":").split(":", 2);
                if (metaData.length == 1) {
                    throw new IllegalStateException("First write meta data, needs to have a : on each line, then an empty line.");
                }
                String key = metaData[0].toLowerCase();
                String value = metaData[1].replace("\n", "");
                switch (key) {
                    case "hoofdmenu":
                        verslag.setMainMenu(MainMenu.valueOf(value.toUpperCase()));
                        break;
                    case "submenu":
                        verslag.setSubMenu(value);
                        break;
                    case "fotosubdirectory":
                        verslag.setPhotoSubDirectory(value);
                        break;
                    case "metabeschrijving":
                        verslag.setMetadataDescription(value);
                        break;
                    case "fotobestandsnaambasis":
                        verslag.setPhotoFileBaseName(value);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown meta data key: " + metaData[0]);
                }
            } else {
                throw new IllegalStateException("First write meta data and a newline");
            }

        }
        throw new IllegalStateException("First write meta data and a new line");
    }

    private String toImageFileName(String input) {
        return makeFileNameSafe(input) + ".jpg";
    }

    private static String makeFileNameSafe(String input) {
        return input.replaceAll("[^A-Za-z0-9]+", "-").replaceAll("^-+", "").replaceAll("-+$", "");
    }
}
