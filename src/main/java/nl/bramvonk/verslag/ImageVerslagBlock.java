package nl.bramvonk.verslag;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.text.StringEscapeUtils;

import java.net.URI;
import java.nio.file.Path;

@RequiredArgsConstructor
@Getter
public class ImageVerslagBlock implements VerslagBlock {
    private final String imageFilename;
    private final String caption;
    private final URI imageUrl;
    @Setter
    private Path pathOnLocalDisk;

    @Override
    public String generateHtml(Verslag verslag) {
        String ret = "<img src='%s'".formatted(StringEscapeUtils.escapeHtml4(verslag.getImageDirectory() + imageFilename));
        if (caption != null) {
            ret += " alt='%s'".formatted(StringEscapeUtils.escapeHtml4(caption));
        }
        ret += "/ >";
        if (caption != null) {
            ret += "<br><em>%s</em>".formatted(StringEscapeUtils.escapeHtml4(caption));
        }
        return "<p style='text-align: center;'>%s</p>".formatted(ret);
    }

}
