package models.support;

import helper.Dates;
import jobs.TicketStateSyncJob;
import org.apache.commons.lang.StringUtils;
import org.joda.time.Duration;
import play.libs.F;

import java.util.List;

/**
 * 状态的改变:
 * <pre>
 * * NEW -> TWO_MAIL: 这个表示一个新的 Ticket 已经达到了需要进行第二次进行警告的阶段, 需要自动扫描进入这个状态.
 * * NEW -> MAILED: 这个 Ticket 已经被处理, 已经发送了一封 Mail 出去正等待着客户的回信
 * * NEW -> NEW_MSG: 这个 Ticket 有了新的邮件;(虽然这种情况会很少...)
 * -------------
 * * TWO_MAIL -> MAILED: 这个状态只能进入 MAILED 状态, 因为不允许这个 Ticket 不做回复.
 * -------------
 * * MAILED <-> NEW_MSG: 这两个状态的改变就是邮件回复的来回.
 * * MAILED -> NO_RESP: 当发邮件联系对方以后, 一直没有回复, 那么则自动进入 NO_RESP 状态, 可以一直处于这个状态, 在此状态如果有新邮件则自动回到 NEW_MSG.
 * * MAILED -> TWO_MAIL: 当回复了的 Ticket 只拥有 1 个我方的回复的时候, 超过 5 天没有客户回复也还是需要进入需二次跟进状态.
 * * !MAILED -> PRE_CLOSE/CLOSE 这个需要操作人员手动进行操作!
 * -------------
 * * NO_RESP -> NEW_MSG: 在 NO_RESP 状态允许 Ticket 在收到新邮件之后回到 NEW_MSG 状态重新与客户进行交流.
 * * NO_RESP -> CLOSE: 如果确定这个 Ticket 不再做处理, 那么则直接标记到 CLOSE, 就算有新邮件也不会重新进入邮件回复流程(OsTicket 会有回信,但系统不再理睬).
 * -------------
 * * NEW_MSG/MAILED -> PRE_CLOSE/CLOSE: 在与客户的交流结束以后可以进入 PRE_CLOSE 以等待 Ticket 所关联的信息被客户修改, 或者直接进入 CLOSE 表示无法处理.
 * * PRE_CLOSE -> 进入 PRE_CLOSE 的 ticket 不会自动进入 CLOSE 状态, 一定需要人工进行确认才可以进入 CLOSE
 * * PRE_CLOSE -> NEW_MSG , 这个状态下的 Ticket, 如果有新回复, 还是需要进入 NEW_MSG 状态
 * </pre>
 * User: wyattpan
 * Date: 7/26/12
 * Time: 1:33 PM
 */
public enum TicketState {
    /**
     * 这是一个全新创建的 Ticket. 1
     */
    NEW {
        @Override
        public TicketState nextState(Ticket ticket, List<TicketStateSyncJob.OsMsg> msgs,
                                     List<TicketStateSyncJob.OsResp> resps) {
            // TO MAILED
            // TO TWO_MAIL

            TicketStateSyncJob.OsMsg newMsg = TicketStateSyncJob.OsMsg.lastestMsg(msgs);
            if(msgs != null && msgs.size() == 1) newMsg = null; // 系统自动创建的那个 Ticket msg 需要处理
            TicketStateSyncJob.OsResp newResp = TicketStateSyncJob.OsResp.lastestResp(resps);

            if(newResp != null) {
                // 在有最新邮件与最新回复的情况下, 客户的最新邮件时间大于最新回复时间则表示有真正的客户新回复来了, 进入 "有新邮件状态"
                if(TicketStateSyncJob.ishaveNewCustomerEmail(resps, msgs)._1)
                    return NEW_MSG;
                else if(TicketStateSyncJob.ishaveNewOperatorResponse(resps, msgs)._1)
                    return MAILED;
            } else if(newMsg == null) {
                //没有客户回复的情况下, 如果此 Ticket 的创建时间到现在已经超过 5 天了, 那么则进入 "二次邮件"
                Duration duration = new Duration(ticket.createAt.getTime(),
                        System.currentTimeMillis());
                if(duration.getStandardDays() >= 5)
                    return TWO_MAIL;
            }
            // 一般不会没有最新回复的, 因为系统向 OsTicket 开 Ticket 的时候就有一个回复了
            return this;
        }

        @Override
        public String explan() {
            return "新进来的 Review, 没有回复过.";
        }

        @Override
        public String toString() {
            return "新创建";
        }
    },
    /**
     * 需要发送第二封邮件去通知客户的 Ticket. 2
     */
    TWO_MAIL {
        @Override
        public TicketState nextState(Ticket ticket, List<TicketStateSyncJob.OsMsg> msgs,
                                     List<TicketStateSyncJob.OsResp> resps) {
            // TO MAILED
            F.T2<Boolean, TicketStateSyncJob.OsResp> isHaveNewResp = TicketStateSyncJob
                    .ishaveNewOperatorResponse(resps, msgs);
            if(isHaveNewResp._1) return MAILED;

            return this;
        }

        @Override
        public String explan() {
            return "此 Review 由于买家没有回信, 需要进行第二次邮件联系.";
        }

        @Override
        public String toString() {
            return "需要发送第二封邮件";
        }
    },

    /**
     * 我们发送了邮件的 Ticket. 3
     */
    MAILED {
        @Override
        public TicketState nextState(Ticket ticket, List<TicketStateSyncJob.OsMsg> msgs,
                                     List<TicketStateSyncJob.OsResp> resps) {
            //TO NEW_MSG
            //TO NO_RESP
            //TO TWO_MAIL


            // 必须要先检查 NEW_MSG, 要进入 NO_RESP 状态, 必须是在我们自己回了卖家的信才可以
            if(TicketStateSyncJob.ishaveNewCustomerEmail(resps, msgs)._1)
                return NEW_MSG;

            /**
             * 1. 我方回复数量没有达到两次
             * 2. 客户最后回复时间, 超过 5 天
             */
            TicketStateSyncJob.OsResp newResp = TicketStateSyncJob.OsResp.lastestResp(resps);
            if(resps.size() < 2 &&
                    new Duration(newResp.created.getTime(), System.currentTimeMillis())
                            .getStandardDays() >= 5) {
                return TWO_MAIL;
            }

            /**
             * 同时满足下面的条件:
             * 1. 必须是 MAILED/NEW_MSG 状态的 Ticket
             * 2. 当前日期与我们自己最近回复日期之间的有 15 天以上 或者 客户没有一次回复
             * 3. 我们自己回复的数量必须在两次以上(需要排除系统自己的那一次)
             */
            if((ticket.state == this || ticket.state == NEW_MSG) && resps.size() >= 2) {
                TicketStateSyncJob.OsMsg newMsg = TicketStateSyncJob.OsMsg.lastestMsg(msgs);
                Duration duration = new Duration(newMsg.created.getTime(),
                        System.currentTimeMillis());
                if(duration.getStandardDays() >= 15)
                    return NO_RESP;
                else if(resps.size() == 0)
                    return NO_RESP;
            }

            return this;
        }

        @Override
        public String explan() {
            return "我方已经发送邮件进行了联系.";
        }

        @Override
        public String toString() {
            return "已经发送了邮件";
        }
    },

    /**
     * 拥有客户端新邮件的 Ticket 状态. 4
     */
    NEW_MSG {
        @Override
        public TicketState nextState(Ticket ticket, List<TicketStateSyncJob.OsMsg> msgs,
                                     List<TicketStateSyncJob.OsResp> resps) {
            // TO MAILED
            // TO CLOSE(手动)

            F.T2<Boolean, TicketStateSyncJob.OsResp> isHaveNewResp = TicketStateSyncJob
                    .ishaveNewOperatorResponse(resps, msgs);
            if(isHaveNewResp._1) return MAILED;

            return this;
        }

        @Override
        public String explan() {
            return "客户有新邮件回复";
        }

        @Override
        public String toString() {
            return "有新回复";
        }
    },

    /**
     * 无响应的 Ticket. 5
     */
    NO_RESP {
        @Override
        public TicketState nextState(Ticket ticket, List<TicketStateSyncJob.OsMsg> msgs,
                                     List<TicketStateSyncJob.OsResp> resps) {
            // TO NEW_MSG
            // TO CLOSE(手动)
            if(TicketStateSyncJob.ishaveNewCustomerEmail(resps, msgs)._1)
                return NEW_MSG;

            return this;
        }

        @Override
        public String explan() {
            return "客户在 20 天内没有回复, 保留在此状态可以自动触发到 NEW_MSG 状态.";
        }

        @Override
        public String toString() {
            return "客户无回应";
        }
    },

    /**
     * 离成功摸除负平只有一部的地方. 6
     */
    PRE_CLOSE {
        @Override
        public TicketState nextState(Ticket ticket, List<TicketStateSyncJob.OsMsg> msgs,
                                     List<TicketStateSyncJob.OsResp> resps) {
            if(ticket.isSuccess) {
                if(ticket.type == Ticket.T.REVIEW) {
                    if(!StringUtils.contains(ticket.review.comment, "Success Change Review") &&
                            !ticket.review.lastRating.equals(ticket.review.rating)) {
                        ticket.review
                                .comment(String.format("Success Change Review at %s from %s to %s",
                                        Dates.date2DateTime(),
                                        ticket.review.lastRating ==
                                                null ? 0 : ticket.review.lastRating,
                                        ticket.review.rating == null ? 0 : ticket.review.rating));
                    }
                } else if(ticket.type == Ticket.T.FEEDBACK) {
                    if(!StringUtils.contains(ticket.feedback.memo, "Closed At")) {
                        ticket.feedback
                                .comment(String.format("Closed At %s", Dates.date2DateTime()));
                    }
                }
            }
            // 有新回复还是需要进入 NEW_MSG 状态,  无论是否自建的都需要
            if(TicketStateSyncJob.ishaveNewCustomerEmail(resps, msgs)._1)
                return NEW_MSG;
            return this;
        }

        @Override
        public String explan() {
            return "等待关闭前的一个状态, 一般用来等待客户更新 Review/Feedback 的评分.";
        }

        @Override
        public String toString() {
            return "等待关闭";
        }
    },

    /**
     * 因为其他原因关闭 Ticket 的. 7
     */
    CLOSE {
        @Override
        public TicketState nextState(Ticket ticket, List<TicketStateSyncJob.OsMsg> msgs,
                                     List<TicketStateSyncJob.OsResp> resps) {
            // CLOSE 不能自动变更状态.
            // 只能手动处理
            return this;
        }

        @Override
        public String explan() {
            return "关闭状态, 此 Ticket 彻底不予理睬.";
        }

        @Override
        public String toString() {
            return "关闭";
        }
    };

    /**
     * 根据此状态与 Ticket 来判断可以进入哪一个状态
     */
    public abstract TicketState nextState(Ticket ticket, List<TicketStateSyncJob.OsMsg> msgs,
                                          List<TicketStateSyncJob.OsResp> resps);

    public abstract String explan();
}
