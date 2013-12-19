package factory.product;

import factory.ModelFactory;
import models.product.Attach;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 12/19/13
 * Time: 2:35 PM
 */
public class AttachFactory extends ModelFactory<Attach> {
    @Override
    public Attach define() {
        Attach attach = new Attach();
        attach.fileName = "filenam";
        attach.outName = "outname";
        attach.location = "file location";
        attach.originName = "originName";
        return attach;
    }
}
