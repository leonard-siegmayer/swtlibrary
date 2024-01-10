package de.teamA.SWT.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.teamA.SWT.entities.Tag;
import de.teamA.SWT.entities.reqres.JsonResponse;
import de.teamA.SWT.service.LibraryService;

@RestController
@RequestMapping("api/tag")
@CrossOrigin
public class TagController {

    @Autowired
    LibraryService service;

    @PreAuthorize("hasAnyRole('ROLE_STAFF', 'ROLE_ADMIN')")
    @RequestMapping(method = RequestMethod.POST)
    public Tag saveTag(@RequestBody @Valid Tag tag) {
        return service.saveTag(tag);
    }

    @PreAuthorize("hasAnyRole('ROLE_STAFF', 'ROLE_ADMIN')")
    @RequestMapping(method = RequestMethod.DELETE)
    public JsonResponse deleteTag(@RequestParam(value = "id") long id) {
        return service.deleteTag(id);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<Tag> getTags() {
        return service.getTags();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Tag getTag(@PathVariable("id") long id) {
        return service.getTagById(id);
    }
}
