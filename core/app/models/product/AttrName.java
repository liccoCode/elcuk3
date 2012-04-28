package models.product;

import play.db.jpa.Model;

import javax.persistence.Entity;

/**
 * 把各种各样的属性全部提取出来, 用来管理属性的名称
 * User: wyattpan
 * Date: 4/26/12
 * Time: 10:27 AM
 */
@Entity
public class AttrName extends Model {

    public String name;

    public String memo;
}
