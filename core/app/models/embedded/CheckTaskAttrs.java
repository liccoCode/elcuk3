package models.embedded;

import helper.Webs;
import org.apache.commons.lang.StringUtils;

import javax.persistence.Embeddable;
import javax.persistence.Lob;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-5-21
 * Time: AM11:03
 */
@Embeddable
public class CheckTaskAttrs implements Serializable {
    /**
     * 检测要求
     */
    @Lob
    public String qcRequires;

    @Transient
    public List<String> qcRequire = new ArrayList<String>();

    /**
     * 检测方法
     */
    @Lob
    public String qcWays;

    @Transient
    public List<String> qcWay = new ArrayList<String>();

    public enum T {
        ARRAY_TO_STR,
        STR_TO_ARRAY
    }

    public CheckTaskAttrs() {
        this.qcRequires = "";
        this.qcWays = " ";
    }

    public void arryParamSetUP(T flag) {
        if(flag.equals(T.ARRAY_TO_STR)) {
            this.qcRequires = StringUtils.join(this.qcRequire, Webs.SPLIT);
            this.qcWays = StringUtils.join(this.qcWay, Webs.SPLIT);
        } else {
            this.qcRequire = new ArrayList<String>();
            this.qcWay = new ArrayList<String>();

            String temp[] = StringUtils.splitByWholeSeparator(this.qcRequires, Webs.SPLIT);
            if(temp != null) Collections.addAll(this.qcRequire, temp);

            temp = StringUtils.splitByWholeSeparator(this.qcWays, Webs.SPLIT);
            if(temp != null) Collections.addAll(this.qcWay, temp);

            if(StringUtils.isBlank(this.qcRequires)) {
                this.qcRequires = " ";
            }
            if(StringUtils.isBlank(this.qcWays)) {
                this.qcWays = " ";
            }
        }
    }
}
