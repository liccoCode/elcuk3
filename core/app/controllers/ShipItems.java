package controllers;

import models.procure.ShipItem;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

@With({GlobalExceptionHandler.class, Secure.class})
public class ShipItems extends Controller {

    public static void index() {
        List<ShipItem> shipItems = ShipItem.findAll();
    }
}
