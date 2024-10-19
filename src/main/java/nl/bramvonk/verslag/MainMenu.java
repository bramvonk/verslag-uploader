package nl.bramvonk.verslag;

import lombok.Getter;

@Getter
public enum MainMenu {
    WILDWATER("/verslagen-wildwater", "wildwater"),
    KANOPOLO("/verslagen-kanopolo", "polo"),
    VLAKWATER("/verslagen-vlakwater", "vlakwater");

    private final String verslagenPath;
    private final String imageDirectoryName;

    MainMenu(String verslagenPath, String imageDirectoryName) {
        this.verslagenPath = verslagenPath;
        this.imageDirectoryName = imageDirectoryName;
    }

}
