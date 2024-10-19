package nl.bramvonk.googledocs;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.docs.v1.Docs;
import com.google.api.services.docs.v1.model.*;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.SneakyThrows;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class GoogleDocumentReader {
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private Map<String, URI> imagesByInlineKey = new HashMap<>();

    private final Docs service;


    @SneakyThrows
    public GoogleDocumentReader() {
        String googleApiKey = System.getenv("GOOGLE_API_KEY");
        GoogleCredentials credential = GoogleCredentials.fromStream(new ByteArrayInputStream(googleApiKey.getBytes(StandardCharsets.UTF_8)))
                .createScoped(Collections.singleton("https://www.googleapis.com/auth/documents.readonly"));
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credential);


        service = new Docs.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, requestInitializer)
                .setApplicationName("VerslagPlacer")
                .build();
    }

    @SneakyThrows
    public GoogleDoc readFrom(URI googleDocsDoc) {
        String documentId = getDocumentId(googleDocsDoc);

        Document document = service.documents().get(documentId).execute();

        GoogleDoc googleDoc = new GoogleDoc(document.getTitle());

        imagesByInlineKey = document.getInlineObjects().entrySet().stream().filter(this::isImage).collect(Collectors.toMap(
                Map.Entry::getKey,
                this::getImageUri
        ));

        googleDoc.elements.addAll(
                document.getBody().getContent().stream()
                        .flatMap(content -> convertBodyElementToGoogleDocElements(content).stream())
                        .filter(Objects::nonNull)
                        .toList()
        );
        return googleDoc;
    }

    private List<GoogleDocElement> convertBodyElementToGoogleDocElements(StructuralElement content) {
        if (content.getParagraph() != null) {
            return content.getParagraph().getElements().stream().map(this::mapParagraph).toList();
        } else {
            return Collections.emptyList();
        }
    }

    private GoogleDocElement mapParagraph(ParagraphElement paragraphElement) {
        InlineObjectElement inlineObjectElement = paragraphElement.getInlineObjectElement();
        if (inlineObjectElement != null) {
            return new GoogleDocImageElement(imagesByInlineKey.get(inlineObjectElement.getInlineObjectId()));
        }

        TextRun textRun = paragraphElement.getTextRun();
        if (textRun != null) {
            return new GoogleDocTextElement(textRun.getContent(), Boolean.TRUE.equals(textRun.getTextStyle().getBold()), Boolean.TRUE.equals(textRun.getTextStyle().getItalic()));
        }
        return null;
    }

    private URI getImageUri(Map.Entry<String, InlineObject> stringInlineObjectEntry) {
        return tryGetImageUri(stringInlineObjectEntry).orElseThrow();
    }


    private boolean isImage(Map.Entry<String, InlineObject> entry) {
        return tryGetImageUri(entry).isPresent();
    }

    @SneakyThrows
    private Optional<URI> tryGetImageUri(Map.Entry<String, InlineObject> entry) {
        ImageProperties imageProperties = entry.getValue().getInlineObjectProperties().getEmbeddedObject().getImageProperties();
        if (imageProperties.getContentUri() != null) {
            return Optional.of(new URI(imageProperties.getContentUri()));
        } else if (imageProperties.getSourceUri() != null) {
            return Optional.of(new URI(imageProperties.getSourceUri()));
        }
        return Optional.empty();
    }

    private String getDocumentId(URI googleDocsDoc) {
        String path = googleDocsDoc.getPath();
        String prefix = "/document/d/";
        if (!path.startsWith(prefix)) {
            throw new IllegalArgumentException("The path does not start with %s".formatted(prefix));
        }
        path = path.substring(prefix.length());
        if (path.contains("/")) {
            path = path.substring(0, path.indexOf("/"));
        }
        return path;
    }
}
