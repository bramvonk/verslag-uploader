package nl.bramvonk.verslag;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class Verslag {
    private String title;
    private MainMenu mainMenu;
    private String subMenu;
    private String photoSubDirectory;
    private String metadataDescription;
    private String photoFileBaseName;

    private List<VerslagBlock> blocks = new ArrayList<>();

    public String getImageDirectory() {
        return "/images/" + mainMenu.getImageDirectoryName() + "/" + photoSubDirectory + "/";
    }

    public String generateHtml() {
        return blocks.stream().map(block -> block.generateHtml(this)).collect(Collectors.joining("\n"));
    }

    public List<ImageVerslagBlock> getImageBlocks() {
        return blocks.stream()
                .filter(it -> it instanceof ImageVerslagBlock)
                .map(it -> (ImageVerslagBlock) it)
                .toList();
    }
}
