package controllers;

import play.libs.Crypto;
import play.mvc.Controller;
import play.mvc.With;

@With(Secure.class)
public class Application extends Controller {

    public static void index() {
        render();
    }

    public static void pwd(String p) {
        renderText(Crypto.encryptAES(p));
    }

}