package controllers;

import com.elcuk.model.crawl.Listing;
import play.*;
import play.db.DB;
import play.mvc.*;

import java.util.*;

import models.*;
import play.utils.Utils;

public class Application extends Controller {

    public static void index() {
        render();
    }

    public static void indexjson() {
        renderJSON(Arrays.asList("838", "dkfj", "2888", "1k2jk"));
    }

}