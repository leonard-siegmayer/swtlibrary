
## Import data into SWT Library

- The file `media_import.json` contains a json representation of the `library_export.xml` file as exported from the Delicious Library application.
- Note that role `ADMIN` is requried to import data into SWT Library. 
- Provide the web request tool, e.g., Postman, with your `JSESSIONID`. The id is automatically set by the backend component after a successful login and can be extracted using the *Web Developer* feature of your browser.
- Assemble a `POST` request analogous one the request depicted below.
```
POST <HOSTNAME>:<PORT>/api/media/import HTTP/1.1
Cookie: JSESSIONID=<YOUR_SESSIONID>
Content-Type: application/json

<CONTENTS_OF_MEDIA_IMPORT.JSON>
```
- Submit the request, but note that the import may take up to three minutes.