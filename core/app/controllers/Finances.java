package controllers;

import helper.Constant;
import helper.Webs;
import models.finance.SaleFee;
import models.market.Account;
import models.market.M;
import models.view.Ret;
import org.apache.commons.io.FileUtils;
import play.Logger;
import play.data.validation.Error;
import play.mvc.Controller;
import play.mvc.With;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 与财务有关的操作
 * User: wyattpan
 * Date: 3/20/12
 * Time: 10:11 AM
 */
@With({GlobalExceptionHandler.class, Secure.class})
@Check("finances")
public class Finances extends Controller {


    public static void index() {
        render();
    }

}
