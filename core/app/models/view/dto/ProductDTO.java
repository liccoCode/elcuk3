package models.view.dto;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-4-15
 * Time: PM5:12
 */
public class ProductDTO {
    /**
     * 编号
     */
    public Integer id;

    /**
     * 标题
     */
    public String title;

    /**
     * 内容
     */
    public String content;

    public ProductDTO() {
    }

    public ProductDTO(Integer id) {
        this.id = id;
    }
}
