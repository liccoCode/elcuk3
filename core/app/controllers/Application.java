package controllers;

import play.mvc.Controller;

import java.util.Arrays;

public class Application extends Controller {

    public static void index() {
        render();
    }

    public static void indexjson() {
        renderJSON(Arrays.asList("838", "dkfj", "2888", "1k2jk"));
    }

}