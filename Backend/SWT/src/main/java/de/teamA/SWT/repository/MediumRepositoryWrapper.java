package de.teamA.SWT.repository;

import de.teamA.SWT.elastic.ElasticsearchException;
import de.teamA.SWT.elastic.ElasticsearchService;
import de.teamA.SWT.entities.Medium;
import de.teamA.SWT.entities.PhysicalMedium;
import de.teamA.SWT.entities.Reservation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class MediumRepositoryWrapper {

    private final MediumRepository mediumRepository;

    private final ElasticsearchService elasticsearchService;

    private final PhysicalRepository physicalRepository;

    private final ReservationRepository reservationRepository;

    @Autowired
    public MediumRepositoryWrapper(MediumRepository mediumRepository, ElasticsearchService elasticsearchService,
            ReservationRepository reservationRepository, PhysicalRepository physicalRepository)
            throws ElasticsearchException {
        this.mediumRepository = mediumRepository;
        this.elasticsearchService = elasticsearchService;
        this.physicalRepository = physicalRepository;
        this.reservationRepository = reservationRepository;
    }

    public Medium save(Medium medium) throws MediumRepositoryWrapperException {
        Medium mediumSavedToDatabase = mediumRepository.save(medium);
        if (mediumSavedToDatabase == null) {
            throw new MediumRepositoryWrapperException("Couldn't save '" + medium + "' to the SQL database");
        }

        try {
            elasticsearchService.addMedium(mediumSavedToDatabase);
        } catch (ElasticsearchException e) {
            mediumRepository.delete(mediumSavedToDatabase);
            throw new MediumRepositoryWrapperException(e);
        }

        return mediumSavedToDatabase;
    }

    public void save(List<Medium> media) throws MediumRepositoryWrapperException {
        Iterable<Medium> mediaSavedToDatabase = mediumRepository.saveAll(media);
        if (mediaSavedToDatabase == null) {
            throw new MediumRepositoryWrapperException("No media was saved to the SQL database.");
        }
        for (Medium medium : mediaSavedToDatabase) {
            if (medium == null) {
                throw new MediumRepositoryWrapperException("Not all media could be indexed into the SQL database.");
            }
        }
        try {
            elasticsearchService.addMedia(mediaSavedToDatabase);
        } catch (ElasticsearchException e) {
            throw new MediumRepositoryWrapperException(e);
        }

    }

    public Medium update(Medium medium) throws MediumRepositoryWrapperException {
        Medium mediumUpdatedInDatabase = mediumRepository.save(medium);
        if (mediumUpdatedInDatabase == null) {
            throw new MediumRepositoryWrapperException("Couldn't update '" + medium + "' in the SQL database");
        }

        try {
            elasticsearchService.addMedium(mediumUpdatedInDatabase);
        } catch (ElasticsearchException e) {
            throw new MediumRepositoryWrapperException(e);
        }
        return mediumUpdatedInDatabase;
    }

    public Optional<Medium> get(long id) {
        return mediumRepository.findById(id);
    }

    public List<Medium> getAll() {
        List<Medium> result = new ArrayList<>();
        for (Medium medium : mediumRepository.findAll()) {
            result.add(medium);
        }
        return result;
    }

    public void delete(Medium medium) throws MediumRepositoryWrapperException {

        // delete all corresponding physicals and reservations
        List<PhysicalMedium> physicals = physicalRepository.findAllByMedium(medium);
        for (PhysicalMedium p : physicals) {
            List<Reservation> r = reservationRepository.findByPhysicalID(p.getId());
            reservationRepository.deleteInBatch(r);
        }
        physicalRepository.deleteInBatch(physicals);

        // delete the medium itself
        mediumRepository.delete(medium);
        try {
            elasticsearchService.deleteMedium(medium.getId());
        } catch (ElasticsearchException e) {
            throw new MediumRepositoryWrapperException(e);
        }
    }

    public void delete(List<Medium> media) throws MediumRepositoryWrapperException {
        for (Medium medium : media) {
            this.delete(medium);
        }
    }

    public void deleteAll() throws MediumRepositoryWrapperException {
        Iterable<Medium> media = mediumRepository.findAll();
        for (Medium medium : media) {
            this.delete(medium);
        }
        try {
            elasticsearchService.deleteIndex();
            elasticsearchService.createIndex();
        } catch (ElasticsearchException e) {
            throw new MediumRepositoryWrapperException(e);
        }
    }

    public boolean isbnExists(String isbn) {
        return mediumRepository.existsByIsbn(isbn);
    }

    public Medium findByIsbn(String isbn) {
        return mediumRepository.findByIsbn(isbn);
    }
}
