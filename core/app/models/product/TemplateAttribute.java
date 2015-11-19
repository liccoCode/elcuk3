package models.product;

import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by licco on 15/10/28.
 */
@Entity
@Table(name = "Template_Attribute")
public class TemplateAttribute extends Model{

    @Column(name = "templates_id")
    public Template template;

    @Column(name = "attributes_id")
    public Attribute attribute;

    public boolean isDeclare;

}
