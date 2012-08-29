package models.support;

import play.db.jpa.GenericModel;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * OsTicket 系统中, 我们自己的回复
 * User: wyattpan
 * Date: 8/29/12
 * Time: 4:21 PM
 */
@Entity
public class TicketResponse extends GenericModel {

    @ManyToOne
    public Ticket ticket;

    @Id
    public int responseId;

    /**
     * OsTicket 系统内的 Id
     */
    public String ost_ticket_id;

    /**
     * 创建时间
     */
    public Date created;

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        TicketResponse that = (TicketResponse) o;

        if(responseId != that.responseId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + responseId;
        return result;
    }
}
