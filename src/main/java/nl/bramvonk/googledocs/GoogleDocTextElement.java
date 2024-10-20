package nl.bramvonk.googledocs;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class GoogleDocTextElement implements GoogleDocElement {
    private final String text;
    private final boolean bold;
    private final boolean italic;
}
