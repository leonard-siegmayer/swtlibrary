package de.teamA.SWT.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import de.teamA.SWT.entities.Medium;
import de.teamA.SWT.entities.Person;
import de.teamA.SWT.entities.Wish;
import de.teamA.SWT.entities.reqres.JsonResponse;
import de.teamA.SWT.service.LibraryService;
import de.teamA.SWT.service.PdfService;
import de.teamA.SWT.service.WishService;
import de.teamA.SWT.service.email.EmailService;
import de.teamA.SWT.service.email.EmailServiceException;

@RestController
@RequestMapping("api/wish")
@CrossOrigin
public class WishController {

    @Autowired
    WishService wishService;

    @Autowired
    LibraryService libraryService;

    @Autowired
    EmailService emailService;

    @Autowired
    PdfService pdfService;

    // is needed that you donnt get a update Notification when adding an Item
    boolean onlyOneEmail = true;

    @PreAuthorize("hasAnyRole('ROLE_STAFF', 'ROLE_ADMIN')")
    @RequestMapping(method = RequestMethod.POST)
    public Wish addWish(@RequestBody @Valid Wish wish) {
        try {
            emailService.sendWishListNewItems(wish);
            onlyOneEmail = false;
        } catch (EmailServiceException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        return wishService.saveWish(wish);
    }

    @PreAuthorize("hasAnyRole('ROLE_STAFF', 'ROLE_ADMIN')")
    @RequestMapping(method = RequestMethod.GET)
    public List<Wish> getAll() {
        return wishService.getAllWishes();
    }

    @PreAuthorize("hasAnyRole('ROLE_STAFF', 'ROLE_ADMIN')")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public JsonResponse update(@RequestBody @Valid List<Wish> wishes) {
        if (onlyOneEmail) {
            try {
                emailService.sendWishListUpdates(wishes);
            } catch (EmailServiceException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }
        onlyOneEmail = true;

        return wishService.updateWishlist(wishes);
    }

    @PreAuthorize("hasAnyRole('ROLE_STAFF', 'ROLE_ADMIN')")
    @RequestMapping(method = RequestMethod.DELETE)
    public JsonResponse deleteWish(@RequestParam long id) {
        return wishService.deleteWish(id);
    }

    @PreAuthorize("hasAnyRole('ROLE_STAFF', 'ROLE_ADMIN')")
    @RequestMapping(value = "/send", method = RequestMethod.POST)
    public JsonResponse sendWishesToUniversityLibrary(@RequestBody @Valid List<Wish> selectedWishes) {

        try {
            emailService.sendWishesToUniversityLibrary(selectedWishes);
        } catch (EmailServiceException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        return new JsonResponse(200, "Mail Successfully sent!");

    }

    @PreAuthorize("hasAnyRole('ROLE_STAFF', 'ROLE_ADMIN')")
    @RequestMapping(value = "/pdfExport", method = RequestMethod.POST)
    public ResponseEntity<byte[]> generatePdf(@RequestBody @Valid List<Wish> selectedWishes) {

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_PDF);

        byte[] pdf = pdfService.generateWishPdf(selectedWishes);

        return ResponseEntity.ok().headers(headers).body(pdf);

    }

    /**
     * This method is used, to get the data of a wish by its isbn. If a book with
     * this {@link isbn} doesn't exist in the database, it will try to get the data
     * from the OpenLibrary API. The retuned data is used to auto complete the add
     * wish mask in the frontend.
     *
     * @param isbn ISBN of the book, which data should be returned
     * @return wish object if a book with the given isbn exists, else null
     */

    @PreAuthorize("hasAnyRole('ROLE_STAFF', 'ROLE_ADMIN')")
    @RequestMapping(value = "/ISBN", method = RequestMethod.GET)
    public Wish requestData(@RequestParam(value = "isbn") String isbn) {
        Wish wish = wishService.getWishByIsbn(isbn);

        if (wish == null) {
            wish = new Wish();
            Medium medium = libraryService.getDataByISBN(isbn);
            // transforming the medium object into a wish object
            boolean first = true;
            for (Person author : medium.getAuthors()) {
                if (first) {
                    wish.setAuthor(author.getName());
                    first = false;
                } else {
                    wish.setAuthor(wish.getAuthor() + ", " + author.getName());
                }
            }
            wish.setBooktitle(medium.getBooktitle());
            wish.setEdtion(medium.getEdition());
            wish.setIsbn(medium.getIsbn());
            wish.setNote(medium.getNotes().toString());
            wish.setPublisher(medium.getPublisher());
            wish.setYear(medium.getYear());
        }

        return wish;
    }
}
