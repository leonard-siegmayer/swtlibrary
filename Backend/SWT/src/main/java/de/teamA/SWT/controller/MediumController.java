package de.teamA.SWT.controller;

import java.util.List;

import javax.validation.Valid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import de.teamA.SWT.elastic.ElasticsearchService;
import de.teamA.SWT.entities.Medium;
import de.teamA.SWT.entities.Note;
import de.teamA.SWT.entities.Tag;
import de.teamA.SWT.entities.reqres.JsonResponse;
import de.teamA.SWT.repository.MediumRepositoryWrapperException;
import de.teamA.SWT.service.BorrowingService;
import de.teamA.SWT.service.LibraryService;
import de.teamA.SWT.service.PhysicalService;

@RestController
@RequestMapping("api/media")
@CrossOrigin
public class MediumController {

    @Autowired
    ElasticsearchService elasticsearch;

    @Autowired
    LibraryService libraryService;

    @Autowired
    BorrowingService borrowingService;

    @Autowired
    PhysicalService physicalService;

    /**
     * This method is used, to get the data of a medium by its isbn. If a book with
     * this {@link isbn} doesn't exist in the database, it will try to get the data
     * from the OpenLibrary API. The retuned data is used to auto complete the add
     * medium mask in the frontend.
     *
     * @param isbn ISBN of the book, which data should be returned
     * @return medium object if a book with the given isbn exists, else null
     */
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_STAFF', 'ROLE_ADMIN')")
    @RequestMapping(value = "/ISBN", method = RequestMethod.GET)
    public Medium requestData(@RequestParam(value = "isbn") String isbn) {

        Medium medium = null;

        medium = libraryService.getMediumByISBN(isbn);

        if (medium == null) {
            medium = libraryService.getDataByISBN(isbn);
        }

        return medium;
    }

    /**
     * Returns logical medium with the id {@link mediumID}.
     *
     * @param mediumID id of the logical medium, which shall be returned.
     * @return
     */
    @Transactional
    @RequestMapping(value = "/get/{id}", method = RequestMethod.GET)
    public Medium get(@PathVariable("id") long mediumID) {
        return libraryService.getMediumById(mediumID);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_STAFF', 'ROLE_ADMIN')")
    @RequestMapping(method = RequestMethod.POST)
    public Medium saveBook(@RequestBody @Valid Medium medium) {
        Medium result;
        try {
            result = libraryService.saveMedium(medium);
        } catch (MediumRepositoryWrapperException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
        return result;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_STAFF', 'ROLE_ADMIN')")
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    public JsonResponse importMedia(@RequestBody @Valid List<Medium> media) {
        try {
            return libraryService.importMedia(media);
        } catch (MediumRepositoryWrapperException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_STAFF', 'ROLE_ADMIN')")
    @RequestMapping(method = RequestMethod.DELETE)
    public JsonResponse deleteBook(@RequestParam(value = "id") Long id) {
        try {
            libraryService.deleteMedium(id);
            return new JsonResponse(200, "Book succesfully deleted");
        } catch (Exception e) {
            e.printStackTrace();
            return new JsonResponse(500, e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/deleteAll", method = RequestMethod.DELETE)
    public JsonResponse deleteAll() {
        try {
            libraryService.deleteAll();
            return new JsonResponse(200, "Books successfully deleted, reset of index");
        } catch (Exception e) {
            return new JsonResponse(500, "Error while deleting book" + e.getMessage());
        }
    }

    @RequestMapping("/export")
    public String getAll() {
        Iterable<Medium> allMedia = libraryService.getAllMedia();

        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(allMedia);
        } catch (JsonProcessingException e) {
            return new JsonResponse(500, "error").toString();
        }
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public String search(@RequestBody String jsonQueryString) {
        return elasticsearch.search(jsonQueryString);
    }

    @PreAuthorize("hasAnyRole('ROLE_STAFF', 'ROLE_ADMIN')")
    @RequestMapping(value = "/tag", method = RequestMethod.POST)
    public String addTag(@RequestParam(value = "id") Long id, @RequestBody @Valid Tag tag) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(tag);
        } catch (Exception e) {
            return new JsonResponse(500, e.getMessage()).toString();
        }
    }

    @RequestMapping(value = "/isAvailable", method = RequestMethod.GET)
    public String isAvailable(@RequestParam(value = "medium") String mediumId) {
        return libraryService.getAvailability(Long.valueOf(mediumId));
    }

    @PreAuthorize("hasAnyRole('ROLE_STAFF', 'ROLE_ADMIN')")
    @RequestMapping(value = "/note", method = RequestMethod.POST)
    public Note saveNote(@RequestBody Note note, @RequestParam(value = "medium") Long mediumId) {
        try {
            if (note.getId() == null) {
                return libraryService.addNote(mediumId, note.getNote());
            } else {
                return libraryService.editNote(note.getId(), note.getNote());
            }

        } catch (Exception e) {
            // return new JsonResponse(500, e.getMessage()).toString();
            return null;
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_STAFF', 'ROLE_ADMIN')")
    @RequestMapping(value = "/note", method = RequestMethod.DELETE)
    public JsonResponse deleteNote(@RequestParam(value = "note") long noteId) {
        return libraryService.deleteNote(noteId);
    }

    @PreAuthorize("hasAnyRole('ROLE_STAFF', 'ROLE_ADMIN')")
    @RequestMapping(value = "/physical", method = RequestMethod.DELETE)
    public JsonResponse deletePhysical(@RequestParam(value = "physical") String physicalId) {
        return physicalService.deletePhysical(Long.valueOf(physicalId));
    }
}