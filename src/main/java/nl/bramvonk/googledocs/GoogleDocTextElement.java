package nl.bramvonk.googledocs;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GoogleDocTextElement implements GoogleDocElement {
    public final String text;
    public final boolean isBold;
    public final boolean isItalic;
}
