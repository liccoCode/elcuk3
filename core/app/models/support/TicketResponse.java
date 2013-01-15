package models.support;

import jobs.TicketStateSyncJob;
import org.apache.commons.lang.math.NumberUtils;
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
    public TicketResponse() {
    }

    public TicketResponse(TicketStateSyncJob.OsResp resp) {
        this.created = resp.created;
        this.responseId = NumberUtils.toInt(resp.response_id);
        this.ost_ticket_id = resp.ticket_id;
    }

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

    public void updateAttrs(TicketResponse response) {
        if(response.created != null)
            this.created = response.created;
        if(response.ost_ticket_id != null)
            this.ost_ticket_id = response.ost_ticket_id;
        this.save();
    }

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
