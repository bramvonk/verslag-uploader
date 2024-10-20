package nl.bramvonk.googledocs;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.URI;

@RequiredArgsConstructor
@Getter
public class GoogleDocImageElement implements GoogleDocElement {
    private final URI url;
}
