package kg.kadyrbekov.services;

import kg.kadyrbekov.dto.*;
import kg.kadyrbekov.exception.NotFoundException;
import kg.kadyrbekov.model.User;
import kg.kadyrbekov.model.entity.Cars;
import kg.kadyrbekov.model.entity.Review;
import kg.kadyrbekov.repositories.CarsRepository;
import kg.kadyrbekov.repositories.ReviewRepository;
import kg.kadyrbekov.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    private final UserRepository userRepository;

    private final CarsRepository carsRepository;

    public ReviewResponse create(ReviewRequest request, Long carsID) throws NotFoundException {
        User user = getAuthenticatedUser();
        Cars cars = carsRepository.findById(carsID)
                .orElseThrow(() -> new NotFoundException("Car with id not found"));
        Review review = mapToEntity(request);
        review.setCar(cars);
        review.setCarID(carsID);
        review.setUser(user);
        reviewRepository.save(review);
        return response(review);

    }

    public ReviewResponse update(ReviewRequest request, Long id) throws NotFoundException {
        Review review = findByIdReview(id);
        review.setComments(request.getComments());
        review.setStarsRating(request.getStarsRating());
        reviewRepository.save(review);
        return mapToResponse(review);
    }

    public void deleteById(Long id) throws NotFoundException {
        Review review = findByIdReview(id);
        reviewRepository.delete(review);
    }

    public List<Review> getAll() {
        return reviewRepository.findAll();
    }


    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found!"));
    }


    public Review findByIdReview(Long id) throws NotFoundException {
        return reviewRepository.findById(id).orElseThrow(()
                -> new NotFoundException(String.format("Review with id not found", id)));
    }

    public ReviewResponse getById(Long id) {
        Review review = reviewRepository.findById(id).get();
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setComments(review.getComments());
        response.setStarRating(review.getStarsRating());
        response.setCarsID(review.getCar().getId());

        return response;
    }


    public CarsResponseReview getByIdCarsReview(Long id) {
        Cars cars = carsRepository.findById(id).orElseThrow(() -> new NotFoundException("Car with id not found " + id));

        CarsResponseReview carsResponseReview = new CarsResponseReview();
        carsResponseReview.setId(cars.getId());


        List<Review> carReviews = cars.getReviews();
        List<ReviewResponse> reviewResponses = new ArrayList<>();

        for (Review review : carReviews) {
            ReviewResponse reviewResponse = new ReviewResponse();
            reviewResponse.setId(review.getId());
            reviewResponse.setComments(review.getComments());
            reviewResponse.setStarRating(review.getStarsRating());
            reviewResponse.setCarsID(review.getCar().getId());
            reviewResponse.setUserId(review.getUser().getId());
            reviewResponses.add(reviewResponse);
        }

        carsResponseReview.setReviewResponse(reviewResponses);

        return carsResponseReview;
    }


    public Review mapToEntity(ReviewRequest request) {
        Review review = new Review();
        BeanUtils.copyProperties(request, review);
//        review.setUser(request.getUser());
//        review.setCar(request.getCars());
        return review;
    }


    public ReviewResponse response(Review review) {
        if (review == null) {
            return null;
        }
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setComments(review.getComments());
        response.setStarRating(review.getStarsRating());
        response.setCarsID(review.getCar() != null ? review.getCar().getId() : null);
        response.setCarsID(review.getCar().getId());
        response.setUserId(review.getUser().getId());
        return response;
    }

    public ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .starRating(review.getStarsRating())
                .comments(review.getComments())
                .carsID(review.getCar().getId())
                .build();
    }

}
