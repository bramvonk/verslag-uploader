# verslag-uploader

Run VerslagUploaderApplication with the url of a google docs as argument.

In the document, you first need to write some meta data:

| name                  | required? | description                                                                                                                                                                                   |
|-----------------------|-----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| hoofdmenu             | yes       | wildwater, kanopolo, vlakwater                                                                                                                                                                |
| submenu               | no        | for wildwater and vlakwater: the country                                                                                                                                                      |
| fotosubdirectory      | yes       | Where to place the photos.<br>You need to make sure it matches with earlier choices!<br>Examples:<ul><li>polo/amsterdam_open_2024<li>wildwater/erft_20241101<li>wildwater/basf_2023/dag1</ul> | 
| metabeschrijving      | no        | A short text what this verslag is about. Google will often use this to show on the search results page.                                                                                       |
| fotobestandsnaambasis | no        | Photo file names will be generated in the form 001-fotobestandsnaambasis.jpg.<br><br>If you leave this empty, it will be generated with the title of the verslag as base.                     |