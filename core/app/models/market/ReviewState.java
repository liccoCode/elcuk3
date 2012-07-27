package models.market;

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
        public String explan() {
            return "所有新创建的 Review 都是此状态.";
        }
    },
    NEW_OVERDUE {
        @Override
        public String explan() {
            return "新创建的 Review, 但是已经超时没有回复.";
        }
    },

    /**
     * 第一次回信后没有回复, 尝试第二次回复.
     */
    RP2 {
        @Override
        public String explan() {
            return "当第二次发送了邮件, 但客户还没有回复的时候为这个状态.";
        }
    },
    /**
     * 第二次回复后, 没有回复
     */
    RP2_OVERDUE {
        @Override
        public String explan() {
            return "当第一次发送了邮件, 但客户还没有回复的时候为这个状态.";
        }
    },
    NEW_COMMENT {
        @Override
        public String explan() {
            return "此 Review 有客户的新回信了.";
        }
    },

    ACCEPT_UPDATE {
        @Override
        public String explan() {
            return "客户接受进行 Review 的 update.";
        }
    },
    SUCC_UPDATE {
        @Override
        public String explan() {
            return "客户成功对 Review 完成了 Update";
        }
    },
    REFUSE_UPDATE {
        @Override
        public String explan() {
            return "客户拒绝对 Review 进行 Update";
        }
    },
    /**
     * 直接关闭了
     */
    CLOSE {
        @Override
        public String explan() {
            return "无法处理这个 Review, 只能关闭, 或者其他无法处理的情况.";
        }
    };

    public abstract String explan();
}
