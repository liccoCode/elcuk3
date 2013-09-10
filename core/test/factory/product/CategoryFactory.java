package factory.product;

import factory.ModelFactory;
import models.product.Category;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 9/6/13
 * Time: 6:11 PM
 */
public class CategoryFactory extends ModelFactory<Category> {
    @Override
    public Category define() {
        Category cate = new Category();
        cate.categoryId = "71";
        cate.name = "717171";
        cate.settings.amazonCategory = "622979011/622978011/1285418011";
        return cate;
    }
}
