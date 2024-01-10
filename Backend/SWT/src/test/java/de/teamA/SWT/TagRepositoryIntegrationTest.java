package de.teamA.SWT;

import de.teamA.SWT.entities.Tag;
import de.teamA.SWT.repository.TagRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TagRepositoryIntegrationTest {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void whenFindByName_thenReturnTag() {
        // given
        Tag tag = new Tag();
        tag.setName("Testing Tag");
        tag.setDescription("This tag is for testing purposes");
        entityManager.persist(tag);
        entityManager.flush();
        // when
        Tag found = tagRepository.findByName(tag.getName()).get(0);
        // then
        assertThat(found.getName().equals(tag.getName()) && found.getDescription().equals(tag.getDescription()));
    }

    @Test
    public void whenDeleteTag_TagDoesntExist() {
        // given
        Tag tag = new Tag();
        tag.setName("Testing Tag");
        tag.setDescription("This tag is for testing purposes");
        entityManager.persist(tag);
        entityManager.flush();

        // when
        Tag found = tagRepository.findByName(tag.getName()).get(0);
        tagRepository.delete(found);
        boolean exists = tagRepository.existsById(found.getId());
        // then
        assertFalse(exists);
    }
}
