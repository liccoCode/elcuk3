package controllers;

import models.ElcukRecord;
import play.mvc.Controller;
import play.mvc.With;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 1/14/13
 * Time: 3:27 PM
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class Elcuk extends Controller {
    public static void index(Date from, Date to) {
        List<Map<String, List<Integer>>> lines = ElcukRecord.emailOverView(from, to);
        renderJSON(lines);
    }

    public static void orders(File file) {
        renderText("准备中..");
    }
}
