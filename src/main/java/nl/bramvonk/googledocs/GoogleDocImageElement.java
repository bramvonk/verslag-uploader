package nl.bramvonk.googledocs;

import lombok.RequiredArgsConstructor;

import java.net.URI;

@RequiredArgsConstructor
public class GoogleDocImageElement implements GoogleDocElement {
    public final URI url;
}
