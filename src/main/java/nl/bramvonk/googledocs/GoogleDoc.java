package nl.bramvonk.googledocs;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class GoogleDoc {
    public final String title;
    public List<GoogleDocElement> elements = new ArrayList<>();
}
