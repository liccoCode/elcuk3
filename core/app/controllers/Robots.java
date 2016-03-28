package controllers;

import play.mvc.Controller;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 15-11-30
 * Time: 下午2:37
 */
public class Robots extends Controller {
    public static void index() {
        render("Robots/robots.txt");
    }
}
