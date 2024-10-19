package nl.bramvonk.website;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import nl.bramvonk.verslag.ImageVerslagBlock;
import nl.bramvonk.verslag.Verslag;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class VerslagWriter {
    private final String baseUrl;
    private final Verslag verslag;

    private Page page;

    public static void write(Verslag verslag, boolean save) {
        new VerslagWriter(System.getenv("BASE_URL"), verslag).write(save);
    }

    private void write(boolean save) {
        try (Playwright playwright = Playwright.create()) {
            try (Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false))) {
                try (BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                        .setRecordVideoDir(Paths.get("D:\\"))
                        //.setRecordVideoSize(640, 480)
                )) {
                    page = context.newPage();
                    page.context().setDefaultTimeout(15000);

                    login();

                    goToVerslagenPage();

                    makeNewArticle();

                    uploadImages();

                    fillContentTab();

                    fillPublicationTab();

                    fillMetadataTab();

                    letUserChooseMainImage();

                    goBackToContentsTab();

                    page.pause();

                    if (save) {
                        save();
                    }
                }
            }
        }
    }

    private void letUserChooseMainImage() {
        page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("Afbeeldingen en links")).click();
        page.locator("button:has-text('Selecteren')").first().click();
        FrameLocator frameLocator = page.mainFrame().frameLocator("iframe.iframe-content");
        openImageFolderCreateIfNotExists(frameLocator);
        page.pause();
    }

    private void save() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Opslaan & sluiten")).click();
    }

    private void goBackToContentsTab() {
        page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("Inhoud")).click();
    }

    private void fillMetadataTab() {
        if (verslag.getMetadataDescription() != null) {
            page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("Metadata")).click();
            page.getByLabel("Metabeschrijving").fill(verslag.getMetadataDescription());
        }
    }

    private void fillPublicationTab() {
        page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("Publiceren")).click();
        page.getByLabel("Status").selectOption("0"); //gedepubliceerd
        page.getByLabel("Speciaal", new Page.GetByLabelOptions().setExact(true)).selectOption("1"); //ja
    }

    private void uploadImages() {
        // open image dialog
        page.locator("span.mce_imgmanager_ext").click();

        // open right image folder (create if needed)
        FrameLocator frameLocator = page.mainFrame().frameLocator("iframe[aria-label='Dialog Content Iframe']");
        openImageFolderCreateIfNotExists(frameLocator);

        // upload images in that folder
        frameLocator.locator("button.upload").click();
        frameLocator.locator("input[type='file']").setInputFiles(
                verslag.getImageBlocks()
                        .stream()
                        .map(ImageVerslagBlock::getPathOnLocalDisk)
                        .toArray(Path[]::new)
        );
        frameLocator.locator("button#upload-start").click();
        frameLocator.locator("span:has-text('Cancel')").click();
    }

    private void openImageFolderCreateIfNotExists(FrameLocator frameLocator) {
        List<String> folderStructure = new ArrayList<>();
        folderStructure.add(verslag.getMainMenu().getImageDirectoryName());
        folderStructure.addAll(Arrays.stream(verslag.getPhotoSubDirectory().split("/")).toList());

        String currentOpenFolderStructure = "";
        for (String folderName : folderStructure) {
            String folderToOpen = currentOpenFolderStructure + (currentOpenFolderStructure.isEmpty() ? "" : "/") + folderName;
            Locator folderLocator = frameLocator.locator("li[data-id='%s'] > div.uk-tree-row".formatted(folderToOpen));
            try {
                // we don't want to wait too long before concluding that the folder does not exist
                folderLocator.click(new Locator.ClickOptions().setTimeout(5000));
            } catch (TimeoutError e) {
                frameLocator.locator("button#folder_new").click();
                frameLocator.locator("input#dialog-prompt-input").fill(folderName);
                frameLocator.locator("button#dialog-prompt_button_0").click();
                folderLocator.click();
            }
            currentOpenFolderStructure = folderToOpen;
        }

    }

    private void fillContentTab() {
        page.locator("input[name='jform[title]']").fill(verslag.getTitle());

        // go to HTML editor
        page.locator("button:has-text('Code')").click();
        assertThat(page.locator("button[title='Regular Expression']")).isVisible();

        // fill the html editor (which is in an iframe)
        FrameLocator editorFrameLocator = page.mainFrame().frameLocator("iframe#jform_articletext_editor_source_iframe");
        // due to the strange way the editor is made, it is necessary to click the editor first, otherwise it will fill the field your cursor is in
        editorFrameLocator.locator("div.cm-activeLine.cm-line").click();
        editorFrameLocator.locator("div.cm-activeLine.cm-line").fill(verslag.generateHtml());

        // go back to WYSIWYG editor
        page.locator("button:has-text('Editor')").click();
    }

    private void makeNewArticle() {
        page.locator("form > a:has-text('Nieuw artikel')").click();
    }

    private void goToVerslagenPage() {
        page.navigate(baseUrl + verslag.getMainMenu().getVerslagenPath() + (verslag.getSubMenu() == null ? "" : "/" + verslag.getSubMenu()));
        assertThat(page.locator("h1:has-text('Verslagen')")).isVisible();
    }

    private void login() {
        String username = System.getenv("USERNAME");
        String password = System.getenv("PASSWORD");
        assert (username != null && password != null);
        page.navigate(baseUrl + "/login");
        page.locator("input[name=username]").fill(username);
        page.locator("input[name=password]").fill(password);
        page.locator("button:has-text('Inloggen')").click();
        assertThat(page.locator("a[href='/nieuw']")).isAttached();
    }
}
