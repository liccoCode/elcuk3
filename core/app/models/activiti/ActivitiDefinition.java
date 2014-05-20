package models.activiti;

import com.google.gson.annotations.Expose;
import javax.persistence.*;
import play.db.jpa.Model;

/**
 * 所有费用的类型
 * User: wyattpan
 * Date: 3/19/12
 * Time: 10:21 AM
 */
@Entity
public class ActivitiDefinition extends Model {

    public ActivitiDefinition() {
    }


    /**
     * 菜单编码
     */
    @Expose
    public String menuCode;

    /**
     * 菜单名称
     */
    @Expose
    public String menuName;

    /**
     * 流程名称
     */
    @Expose
    public String processName;

    /**
     * 流程路径
     */
    @Expose
    public String processXml;

}
