package models.embedded;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * 附属再 Category 身上的单独提取出来的字段(存储再 Category 表中)
 * User: wyattpan
 * Date: 6/6/12
 * Time: 10:02 AM
 */
@Embeddable
public class CategorySettings {
    /**
     * Amazon 允许的节点, 使用 , 分割
     */
    @Column(columnDefinition = "varchar(255) DEFAULT ''")
    public String amazonNode;

}
