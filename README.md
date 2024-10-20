# verslag-uploader

## Google setup

On google cloud console:

- make a project
- then a service account credential in that
- and in that create a json key

## Environment variables

| name                 | description                                                                                               |
|----------------------|-----------------------------------------------------------------------------------------------------------|
| GOOGLE_API_KEY       | The json service account credential key you created above. Used to read google docs.                      |
| USERNAME<br>PASSWORD | Username/password to log in to verslag website. Must have rights to make new verslagen and upload photos. |
| BASE_URL             | Base url of the verslag site without / after hostname. E.g. https://example.com                           |

## Usage

### Make a document

Make a google docs document. In the document, you first need to write some meta data:

| name                  | required? | description                                                                                                                                                                                   |
|-----------------------|-----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| hoofdmenu             | yes       | wildwater, kanopolo, vlakwater                                                                                                                                                                |
| submenu               | no        | for wildwater and vlakwater: the country                                                                                                                                                      |
| fotosubdirectory      | yes       | Where to place the photos.<br>You need to make sure it matches with earlier choices!<br>Examples:<ul><li>polo/amsterdam_open_2024<li>wildwater/erft_20241101<li>wildwater/basf_2023/dag1</ul> | 
| metabeschrijving      | no        | A short text what this verslag is about. Google will often use this to show on the search results page.                                                                                       |
| fotobestandsnaambasis | no        | Photo file names will be generated in the form 001-fotobestandsnaambasis.jpg.<br><br>If you leave this empty, it will be generated with the title of the verslag as base.                     |

then an empty line and then the text of the verslag.

You can only use bold and italic, other style gets lost.

Under each image you can place a caption. That must be in italic on a line by itself.

### Upload the document to the verslag website

Make sure the document is 'shared' with at least 'Anyone on the internet with the link can view'.

Run VerslagUploaderApplication with the url of the google docs as argument.

Halfway the process you will need to select the 'main' photo of the verslag. You will need to zoom out to see the blue
button to click. Then continue with the 'play' symbol on the extra window.
