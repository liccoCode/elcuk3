package models.product;

import play.db.jpa.Model;

import javax.persistence.*;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 15/10/28
 * Time: 10:49 AM
 */
@Entity
@Table(name = "Template_Attribute")
public class TemplateAttribute extends Model {
    @ManyToOne
    @JoinColumn(name = "templates_id")
    public Template template;

    @ManyToOne
    @JoinColumn(name = "attributes_id")
    public Attribute attribute;

    public boolean isDeclare;

    public TemplateAttribute() {
        this.isDeclare = false;
    }

    public TemplateAttribute(Template template, Attribute attribute) {
        this();
        this.template = template;
        this.attribute = attribute;
    }

    public static TemplateAttribute findByTemplateAndAttribute(Template template, Attribute attribute) {
        return TemplateAttribute.find("template=? AND attribute=?", template, attribute).first();
    }

    public boolean exists() {
        return TemplateAttribute.count("template=? AND attribute=?", this.template, this.attribute) > 0;
    }

    public void updateDeclare(boolean isDeclare) {
        this.isDeclare = isDeclare;
        this.save();
    }
}
