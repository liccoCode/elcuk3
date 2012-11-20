package controllers.api;

import helper.Webs;
import models.support.Ticket;
import models.view.Ret;
import play.data.binding.As;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 8/10/12
 * Time: 4:09 PM
 */
@With({APIChecker.class})
public class Tickets extends Controller {
    public static void create(String ticketId, @As("yyyy-MM-dd HH:mm:ss") Date createAt, String title) {
        Ticket t = new Ticket(ticketId, createAt, title);
        validation.valid(t);
        if(Validation.hasErrors()) {
            renderJSON(new Ret(Webs.V(Validation.errors())));
        }
        t.save();
        renderJSON(new Ret());
    }
}
