package nl.bramvonk.googledocs;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class GoogleDoc {
    private final String title;
    private final List<GoogleDocElement> elements;
}
