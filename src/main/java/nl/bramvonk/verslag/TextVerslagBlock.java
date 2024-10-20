package nl.bramvonk.verslag;

import lombok.RequiredArgsConstructor;
import nl.bramvonk.googledocs.GoogleDocTextElement;
import org.apache.commons.text.StringEscapeUtils;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class TextVerslagBlock implements VerslagBlock {
    private final List<GoogleDocTextElement> blockElements;

    @Override
    public String generateHtml(Verslag verslag) {
        return "<p>" + blockElements.stream().map(this::generateHtml).collect(Collectors.joining("")) + "</p>\n";
    }

    private String generateHtml(GoogleDocTextElement textRun) {
        String ret = StringEscapeUtils.escapeHtml4(textRun.getText());
        if (textRun.isBold()) {
            ret = "<strong>" + ret + "</strong>";
        }
        if (textRun.isItalic()) {
            ret = "<em>" + ret + "</em>";
        }
        return ret;
    }

}
