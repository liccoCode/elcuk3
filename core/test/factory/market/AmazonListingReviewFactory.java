package factory.market;

import factory.FactoryBoy;
import factory.ModelFactory;
import models.market.AmazonListingReview;
import models.market.Listing;
import models.market.Orderr;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 9/27/13
 * Time: 3:21 PM
 */
public class AmazonListingReviewFactory extends ModelFactory<AmazonListingReview> {
    @Override
    public AmazonListingReview define() {
        AmazonListingReview review = new AmazonListingReview();
        review.listing = FactoryBoy.lastOrCreate(Listing.class);
        review.listingId = review.listing.listingId;
        review.orderr = FactoryBoy.lastOrCreate(Orderr.class);
        review.alrId = "B001OQOK5U_AMAZON.CO.UK_R1WHYKMCXFMSW8";
        review.helpClick = 1;
        review.helpUp = 1;
        review.lastRating = 2f;
        review.review = "Like xland, I found this to be very low quality. Cheap, light and plasticky, with the lens not set squarely in its retainer. It isn't remotely worth £9.20. Save your money and choose something else";
        review.title = "Appallingly over-priced";
        review.userid = "A156FZ2PHL0WHM";
        review.username = "S. Cryer";
        review.reviewId = "R1WHYKMCXFMSW8";
        return review;
    }
}
