package models.support;

/**
 * 状态的改变:
 * <pre>
 * * NEW -> TWO_MAIL: 这个表示一个新的 Ticket 已经达到了需要进行第二次进行警告的阶段, 需要自动扫描进入这个状态.
 * * NEW -> MAILED: 这个 Ticket 已经被处理, 已经发送了一封 Mail 出去正等待着客户的回信
 * -------------
 * * TWO_MAIL -> MAILED: 这个状态只能进入 MAILED 状态, 因为不允许这个 Ticket 不做回复.
 * -------------
 * * MAILED <-> NEW_MSG: 这两个状态的改变就是邮件回复的来回.
 * * MAILED -> NO_RESP: 当发邮件联系对方以后, 一直没有回复, 那么则自动进入 NO_RESP 状态, 可以一直处于这个状态, 在此状态如果有新邮件则自动回到 NEW_MSG.
 * -------------
 * * NO_RESP -> NEW_MSG: 在 NO_RESP 状态允许 Ticket 在收到新邮件之后回到 NEW_MSG 状态重新与客户进行交流.
 * * NO_RESP -> CLOSE: 如果确定这个 Ticket 不再做处理, 那么则直接标记到 CLOSE, 就算有新邮件也不会重新进入邮件回复流程(OsTicket 会有回信,但系统不再理睬).
 * -------------
 * * NEW_MSG/MAILED -> PRE_CLOSE/CLOSE: 在与客户的交流结束以后可以进入 PRE_CLOSE 以等待 Ticket 所关联的信息被客户修改, 或者直接进入 CLOSE 表示无法处理.
 * </pre>
 * User: wyattpan
 * Date: 7/26/12
 * Time: 1:33 PM
 */
public enum TicketState {
    /**
     * 这是一个全新创建的 Ticket. 1
     */
    NEW,
    /**
     * 需要发送第二封邮件去通知客户的 Ticket. 2
     */
    TWO_MAIL,

    /**
     * 我们发送了邮件的 Ticket. 3
     */
    MAILED,

    /**
     * 拥有客户端新邮件的 Ticket 状态. 4
     */
    NEW_MSG,

    /**
     * 无响应的 Ticket. 5
     */
    NO_RESP,

    /**
     * 离成功摸除负平只有一部的地方. 6
     */
    PRE_CLOSE,

    /**
     * 因为其他原因关闭 Ticket 的. 7
     */
    CLOSE
}
