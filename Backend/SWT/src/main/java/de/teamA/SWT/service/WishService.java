package de.teamA.SWT.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.teamA.SWT.entities.Wish;
import de.teamA.SWT.entities.reqres.JsonResponse;
import de.teamA.SWT.repository.WishRepository;

@Service
public class WishService {

    private WishRepository wishRepository;

    @Autowired
    public WishService(WishRepository wishRepository) {
        this.wishRepository = wishRepository;
    }

    public Wish getWishByIsbn(String isbn) {
        List<Wish> wishes = wishRepository.findByIsbn(isbn);
        if (wishes.isEmpty()) {
            return null;
        }

        return wishes.get(0);
    }

    /**
     * Methods for WishController getAllWishes(), saveWish(), upvoteWish(),
     * deleteWish()
     **/
    public Wish saveWish(Wish wish) {
        List<Wish> oldWishes = wishRepository.findByIsbn(wish.getIsbn());
        assert oldWishes.size() == 0 || oldWishes.size() == 1;

        if (oldWishes.size() == 0) {
            wish.setCounter(1);
            return wishRepository.save(wish);
        }

        // a wish for the same book already exists
        Wish oldWish = oldWishes.get(0);
        oldWish.incrementCounter();
        oldWish.setNote(oldWish.getNote() + ";\n" + wish.getNote());
        wishRepository.delete(oldWish);
        return wishRepository.save(oldWish);
    }

    public List<Wish> getAllWishes() {
        return wishRepository.findAllByOrderByRankAsc();
    }

    /**
     * Assigns a unique rank to each wish with 1 as the initial rank.
     */
    public JsonResponse updateWishlist(List<Wish> wishes) {
        int rank = 1;

        for (Wish wish : wishes) {
            wish.setRank(rank);
            rank++;
        }

        wishRepository.saveAll(wishes);

        return new JsonResponse(200, "Ranks of wishlist updated");
    }

    public JsonResponse deleteWish(long id) {
        if (!wishRepository.existsById(id)) {
            return new JsonResponse(500, "Wish with id " + id + " doesn't exist");
        }

        wishRepository.deleteById(id);
        return new JsonResponse(200, "Wish deleted");
    }
}
