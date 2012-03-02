package notifiers;

import models.market.Orderr;
import play.Logger;
import play.db.DB;
import play.utils.FastRuntimeException;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 3/2/12
 * Time: 11:44 AM
 */
public class MailsHelper {

    /**
     * 与 Orderr 相关的邮件发送后需要将发送的成功否的信息同步回数据库, 所以需要有这样一个线程来进行 Future 的异步处理.
     */
    public static class MAIL_CALLBACK_1 implements Runnable {
        private Future<Boolean> future;
        private Orderr order;

        private int bit;
        private char c;


        /**
         *
         * @param future 获取返回结果的 Future
         * @param order 等待进行操作的 Orderr
         * @param bit 操作 Orderr.emailed 的第几位
         * @param c 操作 Orderr.emailed 的标识符(f/0)
         */
        public MAIL_CALLBACK_1(Future<Boolean> future, Orderr order, int bit, char c) {
            this.future = future;
            this.order = order;
            this.bit = bit;
            this.c = c;
        }

        @Override
        public void run() {
            try {
                // 如果没有发送成功则跳过后面操作
                if(!future.get(10, TimeUnit.SECONDS)) return;

                order.emailed(bit, c);
                // 如果发送成功则将 emailed 标志位的第一位致 'f' 标识已经发送成功, 并将标志位的改变同步到数据库.
                if(DB.datasource.getConnection().createStatement() // 因为在 Play! 中, 使用了新线程, 并且使用了 JPA 所以需要通过 datasource 来获取数据库操作链接(无事务)
                        .executeUpdate("UPDATE Orderr SET emailed=" + order.emailed + " WHERE orderId='" + order.orderId + "'") == 1)
                    Logger.debug("Order[" + order.orderId + "] email send success!");
                else
                    throw new FastRuntimeException("Update Mail Failed!");
            } catch(Exception e) {
                Logger.warn("Order[" + order.orderId + "] email send failed! {" + e.getMessage() + "}");
            }
        }
    }
}
