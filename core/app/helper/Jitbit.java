package helper;

/**
 * JitBit API
 * http://www.jitbit.com/web-helpdesk/helpdesk-API/
 * User: DyLanM
 * Date: 13-9-23
 * Time: 上午11:02
 *
 * @deprecated 准备废弃
 */
public class Jitbit {

    private Jitbit() {

    }

    /**
     * 分类
     */
    public enum Category {
        SOFTWARE {
            @Override
            public String value() {
                return "63513";
            }
        },

        REVIEW {
            @Override
            public String value() {
                return "64618";
            }
        },

        FEEDBACK {
            @Override
            public String value() {
                return "64617";
            }
        },

        MORE_THAN_ONE_LISTING {
            @Override
            public String value() {
                return "64629";
            }
        };

        public abstract String value();

    }

    /**
     * 向 JitBit系统创建一个 Ticket 并返回该 Ticket ID
     * param: submitterEmail   客户邮箱
     * param: username         客户名称
     * param: subject          邮件标题
     * param: body             邮件类型
     * param: category         邮件类型
     */
    public static String addTicket(String submitterEmail, String username, String subject, String body,
                                   Category category) {
        throw new UnsupportedOperationException("Jitbit 被放弃(Ticket)");
    }

    /**
     * 创建用户
     * <p/>
     * username 或者 邮箱 不能重复
     */
    public static String addUser(String submitterEmail, String username) {
        throw new UnsupportedOperationException("Jitbit 被放弃(User)");
    }
}
