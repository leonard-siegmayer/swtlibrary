package de.teamA.SWT.service;

import static com.google.common.collect.Iterables.isEmpty;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import javax.persistence.EntityNotFoundException;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.teamA.SWT.entities.Borrowing;
import de.teamA.SWT.entities.BorrowingStatus;
import de.teamA.SWT.entities.Keyword;
import de.teamA.SWT.entities.Medium;
import de.teamA.SWT.entities.Note;
import de.teamA.SWT.entities.Person;
import de.teamA.SWT.entities.PhysicalMedium;
import de.teamA.SWT.entities.Reservation;
import de.teamA.SWT.entities.Tag;
import de.teamA.SWT.entities.reqres.JsonResponse;
import de.teamA.SWT.repository.BorrowingRepository;
import de.teamA.SWT.repository.KeywordRepository;
import de.teamA.SWT.repository.MediumRepositoryWrapper;
import de.teamA.SWT.repository.MediumRepositoryWrapperException;
import de.teamA.SWT.repository.NoteRepository;
import de.teamA.SWT.repository.PersonRepository;
import de.teamA.SWT.repository.PhysicalRepository;
import de.teamA.SWT.repository.ReservationRepository;
import de.teamA.SWT.repository.TagRepository;

@Service
public class LibraryService {

    @Value("${borrowings.max_days}")
    private int MAX_DAYS;

    private MediumRepositoryWrapper mediumRepositoryWrapper;
    private PersonRepository personRepository;
    private TagRepository tagRepository;
    private ReservationRepository reservationRepository;
    private PhysicalRepository physicalRepository;
    private NoteRepository noteRepository;
    private KeywordRepository keywordRepository;
    private AuthService authService;
    private BorrowingRepository borrowingRepository;
    private PhysicalService physicalService;

    private final String openLibISBN = "http://openlibrary.org/api/books?bibkeys=ISBN:";
    private final String openLibISBN2 = "&jscmd=details&format=json";

    private final String openCoverISBN = "http://covers.openlibrary.org/b/isbn/";
    private final String openCoverJpg = "-M.jpg";

    /**
     * Takes person and returns a person with id, which is persisted in the
     * database.
     *
     * @param person Person, which doesn't necessarily has an id and isn't
     *               necessarily persisted in the database
     *
     * @return person Person with id, which is persisted in the database.
     */
    private Function<Person, Person> getPerson = person -> {

        if (person.getId() != null) {
            // Object alreadys has a id, so it can be returned
            return person;
        } else if (!(personRepository.existsByName(person.getName()))) {
            // Object doesn't have an id and doesn't exist with this name in the database.
            // So it will be saved and the new enity wiht id will be returned
            return (Person) personRepository.save(person);
        } else {
            // Object already exists in the Repository, so it will return this object
            return personRepository.findByName(person.getName()).get(0);
        }
    };

    /**
     * Takes tag and returns a tag with id, which is persisted in the database.
     *
     * @param tag Tag, which doesn't necessarily has an id and isn't necessarily
     *            persisted in the database
     *
     * @return tag Tag with id, which is persisted in the database.
     */
    private Function<Tag, Tag> getTag = tag -> {

        if (!(tag.getId() == null)) {
            // Object alreadys has a id, so it can be returned
            return tag;
        } else if (!(tagRepository.existsByName(tag.getName()))) {
            // Object doesn't have an id and doesn't exist with this name in the database.
            // So it will be saved and the new enity wiht id will be returned
            return (Tag) tagRepository.save(tag);
        } else {
            // Object already exists in the Repository, so it will return this object
            return tagRepository.findByName(tag.getName()).get(0);
        }
    };

    /**
     * Takes keyword and returns a keyword with id, which is persisted in the
     * database.
     *
     * @param keyword Keyword, which doesn't necessarily has an id and isn't
     *                necessarily persisted in the database
     *
     * @return keyword Keyword with id, which is persisted in the database.
     */
    private Function<Keyword, Keyword> getKeyword = keyword -> {
        if (!(keyword.getId() == null)) {
            // Object alreadys has a id, so it can be returned
            return keyword;
        } else if (!(keywordRepository.existsByName(keyword.getName()))) {
            // Object doesn't have an id and doesn't exist with this name in the database.
            // So it will be saved and the new enity wiht id will be returned
            return (Keyword) keywordRepository.save(keyword);
        } else {
            // Object already exists in the Repository, so it will return this object
            return keywordRepository.findByName(keyword.getName()).get(0);
        }
    };

    @Autowired
    public LibraryService(MediumRepositoryWrapper mediumRepositoryWrapper, PersonRepository personRepository,
            TagRepository tagRepository, PhysicalRepository physicalRepository, NoteRepository noteRepository,
            KeywordRepository keywordRepository, ReservationRepository reservationRepository, AuthService authService,
            BorrowingRepository borrowingRepository, PhysicalService physicalService) {
        this.mediumRepositoryWrapper = mediumRepositoryWrapper;
        this.personRepository = personRepository;
        this.tagRepository = tagRepository;
        this.physicalRepository = physicalRepository;
        this.noteRepository = noteRepository;
        this.keywordRepository = keywordRepository;
        this.reservationRepository = reservationRepository;
        this.authService = authService;
        this.borrowingRepository = borrowingRepository;
        this.physicalService = physicalService;
    }

    private static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        String jsonText = IOUtils.toString(is, Charset.forName("UTF-8"));

        return new JSONObject(jsonText);
    }

    /**
     * If the given medium doesn't have an id, it will be inserted in the database
     * as new mediun, else it will be updated.
     *
     * @param medium the medium which shall be saved/updated
     * @return returns the new medium inserted in the database
     * @throws MediumRepositoryWrapperException
     */
    public Medium saveMedium(Medium medium) throws MediumRepositoryWrapperException {
        Medium savedMedium = null;

        Set<Tag> tags = new HashSet<>((medium.getTags()));
        Set<Keyword> keywords = new HashSet<>((medium.getKeywords()));
        Set<PhysicalMedium> physicals = new HashSet<>(medium.getPhysicals());
        Set<Person> authors = new HashSet<>(medium.getAuthors());
        Set<Person> editors = new HashSet<>(medium.getEditors());

        Set<PhysicalMedium> oldPhysicals = new HashSet<>();

        if (medium.getId() != null) {
            savedMedium = getMediumById(medium.getId());
            oldPhysicals = savedMedium.getPhysicals();
        }

        /*
         * Checks if this medium already exists. In this case it will be checked, if the
         * edited Medium is on the latest state. If the already saved medium has a newer
         * lastEdited date than the date of the to be saved medium, the new medium
         * shouldn't be saved.
         * 
         * If no last edited date exists, the statement will be skipped and the current
         * time set.
         */
        if (medium.getId() != null && savedMedium.getLastEdited() != null) {
            if (savedMedium.getLastEdited().isAfter(medium.getLastEdited())) {
                return savedMedium;
            }
        }
        medium.setLastEdited(LocalDateTime.now());

        /*
         * Saving new entites of persons,tags, keywords and physicals
         */
        Set<Person> newAuthors = new HashSet<>();
        Set<Person> newEditors = new HashSet<>();
        Set<Keyword> newKeywords = new HashSet<>();

        for (Person p : authors) {
            newAuthors.add(getPerson.apply(p));
        }

        for (Person p : editors) {
            newEditors.add(getPerson.apply(p));
        }

        for (Keyword k : keywords) {
            newKeywords.add(getKeyword.apply(k));
        }

        medium.setAuthors(newAuthors);
        medium.setEditors(newEditors);
        medium.setKeywords(newKeywords);

        for (Tag t : tags) {
            tagRepository.save(t);
        }

        for (PhysicalMedium physical : physicals) {
            // If no owner is provided, the default user will be setted as owner
            if (physical.getOwner() == null) {
                physical.setOwner(authService.getDefaultUser());
            }

            physical.setMedium(medium);

            /*
             * Checking if the physical is new. (If no id exists it is new). Already
             * existing physicals will be removed from oldPhysicals. oldPhysicals: a Set
             * containing the physicals from the medium saved in the db. If a physical
             * remains in this set, it means it isn't contained anymore in the new medium
             * and should be deleted.
             */
            if ((physical.getId() == null)) {
                physicalRepository.save(physical);
            } else if (oldPhysicals != null) {
                oldPhysicals.remove(physical);
            }
        }
        for (PhysicalMedium p : oldPhysicals) {
            List<Reservation> r = reservationRepository.findByPhysicalID(p.getId());
            reservationRepository.deleteInBatch(r);
        }
        physicalRepository.deleteAll(oldPhysicals);

        return mediumRepositoryWrapper.save(medium);
    }

    public Medium getMediumByISBN(String isbn) {
        return mediumRepositoryWrapper.findByIsbn(isbn);
    }

    public JsonResponse importMedia(List<Medium> media) throws MediumRepositoryWrapperException {
        int errorsWhileImporting = 0;

        List<Medium> logicals = new LinkedList<>();
        List<Medium> physicals = new LinkedList<>();

        for (Medium medium : media) {
            // Each medium per ISBN will only be saved once, all others will be added as
            // PhysicalMedium
            if (!medium.getIsbn().equals("") && containsISBN(logicals, medium.getIsbn())) {
                physicals.add(medium);
            } else {
                logicals.add(medium);
            }
        }

        for (Medium medium : logicals) {

            /*
             * Retrieving already existing Tags, Keywords, persons from the database. If no
             * entry in the db is present, the tag will be saved and retrieve an id
             * 
             * Only save one entity per name
             */
            Set<Person> newAuthors = new HashSet<>();
            Set<Person> newEditors = new HashSet<>();
            Set<Tag> newTags = new HashSet<>();
            Set<Keyword> newKeywords = new HashSet<>();

            Set<Keyword> keywords = medium.getKeywords();
            Set<Tag> tags = medium.getTags();
            Set<Person> authors = medium.getAuthors();
            Set<Person> editors = medium.getEditors();

            for (Keyword k : keywords) {
                newKeywords.add(getKeyword.apply(k));
            }

            for (Tag t : tags) {
                newTags.add(getTag.apply(t));
            }

            for (Person p : authors) {
                newAuthors.add(getPerson.apply(p));
            }

            for (Person p : editors) {
                newEditors.add(getPerson.apply(p));
            }

            medium.setKeywords(newKeywords);
            medium.setTags(newTags);
            medium.setAuthors(newAuthors);
            medium.setEditors(newEditors);

            setCoverUrl(medium);

            /*
             * Adding a physical to the Medium, if there isn't any medium linked yet. If
             * physicals are attached it is not necessary to create a new physical, because
             * it is probably an exported medium from SWTLib
             */
            if (isEmpty(medium.getPhysicals())) {
                PhysicalMedium physicalMedium = new PhysicalMedium(medium.getOwner(), medium.getLocation(),
                        medium.getDepartment(), medium.getRoom(), medium.isHandapparat(), medium.getRvkSignature(),
                        medium);
                medium.addPhysical(physicalMedium);

            }
        }

        for (Medium medium : physicals) {
            Medium logical;
            Optional<Medium> optionalMedium = getByISBN(logicals, medium.getIsbn());
            if (optionalMedium.isPresent()) {
                logical = optionalMedium.get();
            } else {
                errorsWhileImporting++;
                continue;
            }

            // Checking of the object already contains physicals
            if (isEmpty(medium.getPhysicals())) {
                // If not we are creating a physical out of its data and add it to the logical
                // medium
                PhysicalMedium physicalMedium = new PhysicalMedium(medium.getOwner(), medium.getLocation(),
                        medium.getDepartment(), medium.getRoom(), medium.isHandapparat(), medium.getRvkSignature(),
                        logical);
                physicalRepository.save(physicalMedium);
                logical.addPhysical(physicalMedium);
            } else {
                // If it contains already physicals, we add all of its physicals to the logical
                // medium
                logical.getPhysicals().addAll(medium.getPhysicals());
            }
        }

        mediumRepositoryWrapper.save(logicals);

        return new JsonResponse(200, logicals.size() + " Logicals imported, " + physicals.size()
                + " Physicals imported, " + errorsWhileImporting + " errors while importing");
    }

    /**
     * Returning a medium object filled with all available inforamtion retrieved
     * from the OpenLibrary API, if it contains a entry with the isbn {@link isbn}.
     *
     * @param isbn isbn of the medium, for which the data should be loaded
     * @return medium object filled with the data for the @isbn from OpenLibrary
     */
    public Medium getDataByISBN(String isbn) {
        Medium medium = new Medium();
        JSONObject json;
        JSONObject details;
        try {
            // TODO: maybe refactor this out into a new class that receives an isbn
            // and a medium .. different implementations for different vendors, such as
            // openlibrary
            json = readJsonFromUrl(openLibISBN + isbn + openLibISBN2);

            details = json.getJSONObject("ISBN:" + isbn).getJSONObject("details");
        } catch (JSONException | IOException e) {
            return null;
        }
        medium.setIsbn(isbn);

        Iterator<String> keySet = details.keys();

        /**
         * Iterating over all keys and adding the values of the keys to the belonging
         * mediums attributes.
         */
        keySet.forEachRemaining(keyStr -> {
            String key = String.valueOf(keyStr);

            try {
                JSONArray jsonArray;

                switch (key) {
                case "number_of_pages":
                    medium.setPages(details.getString("number_of_pages"));
                    break;

                case "revision":
                    medium.setEdition(details.getString("revision"));
                    break;

                case "title":
                    medium.setBooktitle(details.getString("title"));
                    break;

                case "languages":
                    String language = details.getJSONArray("languages").getJSONObject(0).getString("key");
                    if (language.length() > 11) {
                        medium.setLanguage(language.substring(11));
                    } else {
                        medium.setLanguage(language);
                    }
                    break;

                case "subjects":
                    Set<Keyword> keywords = new HashSet<>();
                    jsonArray = details.getJSONArray("subjects");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        String keywordName = jsonArray.getString(i);

                        keywords.add(getKeyword.apply(new Keyword(keywordName)));
                    }
                    medium.setKeywords(keywords);
                    break;

                case "authors":
                    Set<Person> authors = new HashSet<>();
                    jsonArray = details.getJSONArray("authors");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        authors.add(getPerson.apply(new Person(jsonArray.getJSONObject(i).getString("name"))));

                    }
                    medium.setAuthors(authors);
                    break;

                case "publish_places":
                    medium.setAddress(details.getJSONArray("publish_places").getString(0));
                    break;

                case "publishers":
                    medium.setPublisher(details.getJSONArray("publishers").getString(0));
                    break;

                case "physical_format":
                    medium.setHowPublished(details.getString("physical_format"));
                    break;

                case "description":
                    medium.setAnnote(details.getString("description"));
                    break;

                case "publish_date":
                    String publishDate = details.getString("publish_date");
                    medium.setYear(publishDate.substring(publishDate.length() - 4));
                    break;

                default:
                    break;
                }

            } catch (JSONException e) {
                // if 'details' does not have a specific key/value entry, a JsonException will
                // be thrown. It can be ignored, so no error handling is necessary
            }
        });
        medium.setOwner(this.authService.getLoggedInUser());

        return medium;
    }

    public Tag addTagToMedium(Long mediumId, Tag tag) throws MediumRepositoryWrapperException {
        Medium medium = getMediumById(mediumId);
        medium.getTags().add(tag);

        tagRepository.save(tag);
        mediumRepositoryWrapper.save(medium);

        return tag;
    }

    public Note addNote(Long mediumId, String text) {
        Medium medium = getMediumById(mediumId);

        Note note = new Note(medium, text, this.authService.getLoggedInUser());

        return noteRepository.save(note);
    }

    public Medium getMediumById(Long id) {
        Optional<Medium> medium = mediumRepositoryWrapper.get(id);
        if (!medium.isPresent()) {
            throw new EntityNotFoundException("Medium with id " + id + " doesn't exist");
        }
        return medium.get();
    }

    public Iterable<Medium> getAllMedia() {
        return mediumRepositoryWrapper.getAll();
    }

    public void deleteMedium(Long id) throws MediumRepositoryWrapperException, EntityNotFoundException {
        Medium medium = getMediumById(id);
        mediumRepositoryWrapper.delete(medium);
    }

    public void deleteAll() throws MediumRepositoryWrapperException {
        mediumRepositoryWrapper.deleteAll();
    }

    public Tag getTagById(Long id) {
        Optional<Tag> tag = tagRepository.findById(id);
        if (!tag.isPresent()) {
            throw new EntityNotFoundException("Tag with id " + id + " doesn't exist");
        }
        return tag.get();
    }

    /**
     * Returns all tags
     * 
     * @return all tags
     */
    public List<Tag> getTags() {
        return tagRepository.findAll();
    }

    /**
     * Saves a tag without adding it to a medium
     * 
     * @param tag the tag to be saved
     * @return the saved tag
     */
    public Tag saveTag(Tag tag) {
        return tagRepository.save(tag);
    }

    public JsonResponse deleteTag(Long id) {

        try {
            Tag tag = getTagById(id);

            tagRepository.deleteInBatch(Collections.singletonList(tag));
        } catch (Exception e) {
            return new JsonResponse(500, e.getMessage());
        }

        return new JsonResponse(200, "Tag deleted");
    }

    public Note editNote(Long noteId, String text) {
        Note note = getNoteById(noteId);
        note.setText(text);
        return noteRepository.save(note);
    }

    private Note getNoteById(Long id) {
        Optional<Note> note = noteRepository.findById(id);
        if (!note.isPresent()) {
            throw new EntityNotFoundException("Note with id " + id + " doesn't exist");
        }
        return note.get();
    }

    public JsonResponse deleteNote(Long id) {
        Note note = getNoteById(id);

        if (authService.isAdmin()) {
            noteRepository.deleteInBatch(Collections.singletonList(note));
        } else {
            if (!note.getUser().getId().equals(authService.getLoggedInUserName())) {
                return new JsonResponse(500, "You can only delete your own notes");
            }
            noteRepository.deleteInBatch(Collections.singletonList(note));
        }

        return new JsonResponse(200, "Note deleted");
    }

    public String getAvailability(Long physicalId) throws EntityNotFoundException {
        List<Borrowing> status = borrowingRepository
                .findByMediumAndReturnDateIsNull(physicalService.getPhysicalById(physicalId));

        if (status.isEmpty()) {
            return BorrowingStatus.FREE.toString();
        } else {
            long borrowed = status.stream().map(Borrowing::getStatus).filter(s -> s.toString().equals("BORROWED"))
                    .count();
            if (borrowed >= 1) {
                return BorrowingStatus.BORROWED.toString();
            }

            return status.get(0).getStatus().toString();
        }
    }

    /**
     * Helper Methods
     **/
    private void setCoverUrl(Medium medium) {
        if (medium.getCoverURL() == null & medium.getIsbn() != null) {
            try {
                medium.setCoverURL(getCoverURL(medium.getIsbn()));
            } catch (IOException | RuntimeException e) {
                // Throws exception if cover is not available, so not necessary to handle (no cover is ok)
            }
        }
    }

    private String getCoverURL(String isbn) throws IOException {
        String redirectedUrl;

        URL url = new URL(openCoverISBN + isbn + openCoverJpg);

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("HEAD");
        con.setInstanceFollowRedirects(false);

        redirectedUrl = con.getHeaderField("Location");

        if (redirectedUrl == null) {
            throw new RuntimeException("Cover is not available");
        }
        return redirectedUrl;
    }

    private boolean containsISBN(final List<Medium> list, final String isbn) {
        return list.stream().map(Medium::getIsbn).anyMatch(isbn::equals);
    }

    private Optional<Medium> getByISBN(final List<Medium> list, final String isbn) {
        return list.stream().filter(m -> m.getIsbn().equals(isbn)).findFirst();
    }
}
