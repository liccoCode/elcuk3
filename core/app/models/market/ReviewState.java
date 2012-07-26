package models.market;

import play.utils.FastRuntimeException;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 7/26/12
 * Time: 1:33 PM
 */
public enum ReviewState {

    /**
     * 新创建的 Review 都是这个状态
     */
    NEW {
        @Override
        public AmazonListingReview apply(AmazonListingReview review) {
            if(review.state == CLOSE || review.state == ACCEPT || review.state == ACCEPT_UPDATE || review.state == REFUSE)
                throw new FastRuntimeException("CLOSE, ACCEPT, ACCEPT_UPDATED, REFUSE 这些状态不允许回到 New");
            review.state = this;
            return review;
        }

        @Override
        public String explan() {
            return "所有新创建的 Review 都是此状态.";
        }
    },
    /**
     * 我们第一次回信, 没有回复
     */
    RP1 {
        @Override
        public AmazonListingReview apply(AmazonListingReview review) {
            if(review.state == CLOSE || review.state == ACCEPT || review.state == ACCEPT_UPDATE || review.state == REFUSE)
                throw new FastRuntimeException("CLOSE, ACCEPT, ACCEPT_UPDATED, REFUSE 这些状态不允许回到 RP1");

            return null;
        }

        @Override
        public String explan() {
            return "当第一次发送了邮件, 但客户还没有回复的时候为这个状态.";
        }
    },
    /**
     * 我们第二次回信, 没有回复
     */
    RP2 {
        @Override
        public AmazonListingReview apply(AmazonListingReview review) {
            if(review.state == CLOSE || review.state == ACCEPT || review.state == ACCEPT_UPDATE || review.state == REFUSE)
                throw new FastRuntimeException("CLOSE, ACCEPT, ACCEPT_UPDATED, REFUSE 这些状态不允许回到 RP2");

            return null;
        }

        @Override
        public String explan() {
            return "当第二次发送了邮件, 但客户还没有回复的时候为这个状态.";
        }
    },
    /**
     * 确定需要我们打电话处理
     */
    PHONE {
        @Override
        public AmazonListingReview apply(AmazonListingReview review) {
            return null;
        }

        @Override
        public String explan() {
            return "第三次, 我们需要打电话确定的时候,为这个状态.";
        }
    },
    /**
     * 正常处理
     */
    NORMAL {
        @Override
        public AmazonListingReview apply(AmazonListingReview review) {
            return null;
        }

        @Override
        public String explan() {
            return "已经与客户进行正常的交流了.";
        }
    },
    /**
     * 接受了我们的处理要求
     */
    ACCEPT {
        @Override
        public AmazonListingReview apply(AmazonListingReview review) {
            return null;
        }

        @Override
        public String explan() {
            return "客户同意接受我们的处理方式.";
        }
    },
    /**
     * 拒绝了我们处理要求
     */
    REFUSE {
        @Override
        public AmazonListingReview apply(AmazonListingReview review) {
            return null;
        }

        @Override
        public String explan() {
            return "客户拒绝接受我们的处理方式.";
        }
    },
    /**
     * 接受了并且更新了 Review
     */
    ACCEPT_UPDATE {
        @Override
        public AmazonListingReview apply(AmazonListingReview review) {
            return null;
        }

        @Override
        public String explan() {
            return "客户同意接受我们的处理方式并且已经对 Review 进行了修改.";
        }
    },
    /**
     * 直接关闭了
     */
    CLOSE {
        @Override
        public AmazonListingReview apply(AmazonListingReview review) {
            return null;
        }

        @Override
        public String explan() {
            return "无法处理这个 Review, 只能关闭, 或者其他无法处理的情况.";
        }
    };

    /**
     * 处理这个状态的某一个 Review
     *
     * @return
     */
    public abstract AmazonListingReview apply(AmazonListingReview review);

    public abstract String explan();
}
