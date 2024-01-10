package de.teamA.SWT;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import de.teamA.SWT.entities.Tag;
import de.teamA.SWT.service.LibraryService;

public class TagControllerIntegrationTest extends Library {

    @MockBean
    private LibraryService service;

    @Test
    @WithMockUser(roles = "STAFF")
    public void whenPostTagAsStaff_thenCreateTag() throws Exception {
        // given
        Tag tag = new Tag();
        tag.setName("Testing Tag");
        tag.setDescription("This tag is for testing purposes");
        given(service.saveTag(Mockito.anyObject())).willReturn(tag);

        mockMvc.perform(post("/api/tag").contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(tag))).andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Testing Tag")));
        verify(service, VerificationModeFactory.times(1)).saveTag(Mockito.anyObject());
        reset(service);
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    public void whenPostTagAsStudent_thenDontCreateTag() throws Exception {
        // given
        Tag tag = new Tag();
        tag.setName("Testing Tag");
        tag.setDescription("This tag is for testing purposes");
        given(service.saveTag(Mockito.anyObject())).willReturn(tag);

        mockMvc.perform(post("/api/tag").contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(tag))).andExpect(status().isForbidden());
        verify(service, VerificationModeFactory.times(0)).saveTag(Mockito.anyObject());
        reset(service);
    }

}
